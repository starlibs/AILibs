package jaicore.ml.dyadranking.algorithm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.aeonbits.owner.ConfigFactory;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration.ListBuilder;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.gradient.Gradient;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.nn.workspace.LayerWorkspaceMgr;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.primitives.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.FileUtil;
import jaicore.ml.core.exception.ConfigurationException;
import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.core.exception.TrainingException;
import jaicore.ml.core.predictivemodel.ICertaintyProvider;
import jaicore.ml.core.predictivemodel.IOnlineLearner;
import jaicore.ml.core.predictivemodel.IPredictiveModelConfiguration;
import jaicore.ml.dyadranking.Dyad;
import jaicore.ml.dyadranking.dataset.DyadRankingDataset;
import jaicore.ml.dyadranking.dataset.DyadRankingInstance;
import jaicore.ml.dyadranking.dataset.IDyadRankingInstance;

/**
 * A dyad ranker based on a Plackett-Luce network.
 *
 *
 * All the provided algorithms are implementations of the PLModel introduced in
 * [1].
 *
 * [1] Schäfer, D., & Hüllermeier, E. (2018). Dyad ranking using Plackett--Luce
 * models based on joint feature representations. Machine Learning, 107(5),
 * 903–941. https://doi.org/10.1007/s10994-017-5694-9
 *
 * @author Helena Graf, Jonas Hanselle, Michael Braun
 *
 */
public class PLNetDyadRanker
implements IPLDyadRanker, IOnlineLearner<IDyadRankingInstance, IDyadRankingInstance, DyadRankingDataset>, ICertaintyProvider<IDyadRankingInstance, IDyadRankingInstance, DyadRankingDataset> {

	private static final Logger log = LoggerFactory.getLogger(PLNetDyadRanker.class);

	private MultiLayerNetwork plNet;
	private IPLNetDyadRankerConfiguration configuration;
	private int epoch;
	private int iteration;

	/**
	 * Constructs a new {@link PLNetDyadRanker} using the default
	 * {@link IPLNetDyadRankerConfiguration}.
	 *
	 */
	public PLNetDyadRanker() {
		this.configuration = ConfigFactory.create(IPLNetDyadRankerConfiguration.class);
	}

	/**
	 * Constructs a new {@link PLNetDyadRanker} using the given
	 * {@link IPLNetDyadRankerConfiguration}.
	 *
	 * @param config
	 *            Configuration for the {@link PLNetDyadRanker}.
	 */
	public PLNetDyadRanker(final IPLNetDyadRankerConfiguration config) {
		this.configuration = config;
	}

	@Override
	public void train(final DyadRankingDataset dataset) throws TrainingException {
		this.train(dataset.toND4j());
	}

	public void train(final List<INDArray> dataset) {
		this.train(dataset, this.configuration.plNetMaxEpochs(), this.configuration.plNetEarlyStoppingTrainRatio());
		if (this.configuration.plNetEarlyStoppingRetrain()) {
			int maxEpochs = this.epoch;
			this.plNet = null;
			this.train(dataset, maxEpochs, 1.0);
		}
	}

	public void train(final DyadRankingDataset dataset, final int maxEpochs, final double earlyStoppingTrainRatio) {
		this.train(dataset.toND4j(), maxEpochs, earlyStoppingTrainRatio);
	}

	public void train(final List<INDArray> dataset, final int maxEpochs, final double earlyStoppingTrainRatio) {
		List<INDArray> drTrain = dataset.subList(0, (int) (earlyStoppingTrainRatio * dataset.size()));
		List<INDArray> drTest = dataset.subList((int) (earlyStoppingTrainRatio * dataset.size()), dataset.size());

		if (this.plNet == null) {
			int dyadSize = dataset.get(0).columns();
			this.plNet = this.createNetwork(dyadSize);
			this.plNet.init();
		}

		double currentBestScore = Double.POSITIVE_INFINITY;
		MultiLayerNetwork currentBestModel = this.plNet;
		this.epoch = 0;
		this.iteration = 0;
		int patience = 0;
		int earlyStoppingCounter = 0;

		while ((patience < this.configuration.plNetEarlyStoppingPatience()
				|| this.configuration.plNetEarlyStoppingPatience() <= 0) && (this.epoch < maxEpochs || maxEpochs == 0)) {
			// Iterate through training data
			this.tryUpdatingWithMinibatch(drTrain);

			log.debug("plNet params: {}", this.plNet.params());
			earlyStoppingCounter++;
			// Compute validation error
			if (earlyStoppingCounter == this.configuration.plNetEarlyStoppingInterval() && earlyStoppingTrainRatio < 1.0) {
				double avgScore = this.computeAvgError(drTest);
				if (avgScore < currentBestScore) {
					currentBestScore = avgScore;
					currentBestModel = this.plNet.clone();
					log.debug("current best score: {}", currentBestScore);
					patience = 0;
				} else {
					patience++;
				}
				earlyStoppingCounter = 0;
			}
			this.epoch++;
		}
		this.plNet = currentBestModel;
	}

	private void tryUpdatingWithMinibatch(final List<INDArray> drTrain) {
		int miniBatchSize = this.configuration.plNetMiniBatchSize();
		List<INDArray> miniBatch = new ArrayList<>(miniBatchSize);
		for (INDArray dyadRankingInstance : drTrain) {
			miniBatch.add(dyadRankingInstance);
			if (miniBatch.size() == miniBatchSize) {
				this.updateWithMinibatch(miniBatch);
				miniBatch.clear();
			}
		}
		if (!miniBatch.isEmpty()) {
			this.updateWithMinibatch(miniBatch);
			miniBatch.clear();
		}
	}

	private INDArray computeScaledGradient(final INDArray dyadMatrix) {
		int dyadRankingLength = dyadMatrix.rows();
		List<INDArray> activations = this.plNet.feedForward(dyadMatrix);
		INDArray output = activations.get(activations.size() - 1);
		output = output.transpose();
		INDArray deltaW = Nd4j.zeros(this.plNet.params().length());
		Gradient deltaWk = null;
		MultiLayerNetwork plNetClone = this.plNet.clone();
		for (int k = 0; k < dyadRankingLength; k++) {
			// compute derivative of loss w.r.t. k
			plNetClone.setInput(dyadMatrix.getRow(k));
			plNetClone.feedForward(true, false);
			INDArray lossGradient = PLNetLoss.computeLossGradient(output, k);
			// compute backprop gradient for weight updates w.r.t. k
			Pair<Gradient, INDArray> p = plNetClone.backpropGradient(lossGradient, null);
			deltaWk = p.getFirst();
			this.plNet.getUpdater().update(this.plNet, deltaWk, this.iteration, this.epoch, 1, LayerWorkspaceMgr.noWorkspaces());
			deltaW.addi(deltaWk.gradient());
		}

		return deltaW;
	}

	/**
	 * Computes the gradient of the plNets' error function for a given instance. The
	 * returned gradient is already scaled by the updater. The update procedure is
	 * based on algorithm 2 in [1].
	 *
	 * @param instance
	 *            The instance to compute the scaled gradient for.
	 * @return The gradient for the given instance, multiplied by the updater's
	 *         learning rate.
	 */
	private INDArray computeScaledGradient(final IDyadRankingInstance instance) {
		// init weight update vector
		INDArray dyadMatrix;
		List<INDArray> dyadList = new ArrayList<>(instance.length());
		for (Dyad dyad : instance) {
			INDArray dyadVector = this.dyadToVector(dyad);
			dyadList.add(dyadVector);
		}
		dyadMatrix = this.dyadRankingToMatrix(instance);
		List<INDArray> activations = this.plNet.feedForward(dyadMatrix);
		INDArray output = activations.get(activations.size() - 1);
		output = output.transpose();
		INDArray deltaW = Nd4j.zeros(this.plNet.params().length());
		Gradient deltaWk = null;
		MultiLayerNetwork plNetClone = this.plNet.clone();
		for (int k = 0; k < instance.length(); k++) {
			// compute derivative of loss w.r.t. k
			plNetClone.setInput(dyadList.get(k));
			plNetClone.feedForward(true, false);
			INDArray lossGradient = PLNetLoss.computeLossGradient(output, k);
			// compute backprop gradient for weight updates w.r.t. k
			Pair<Gradient, INDArray> p = plNetClone.backpropGradient(lossGradient, null);
			deltaWk = p.getFirst();
			this.plNet.getUpdater().update(this.plNet, deltaWk, this.iteration, this.epoch, 1, LayerWorkspaceMgr.noWorkspaces());
			deltaW.addi(deltaWk.gradient());
		}

		return deltaW;
	}

	/**
	 * Updates this {@link PLNetDyadRanker} based on a given mini batch of
	 * {@link INDarray}s representing dyad rankings.
	 *
	 * @param minibatch
	 *            A mini batch consisting of a {@link List} of {@link INDarray}.
	 */
	private void updateWithMinibatch(final List<INDArray> minibatch) {
		double actualMiniBatchSize = minibatch.size();
		INDArray cumulativeDeltaW = Nd4j.zeros(this.plNet.params().length());
		for (INDArray instance : minibatch) {
			cumulativeDeltaW.addi(this.computeScaledGradient(instance));
		}
		cumulativeDeltaW.muli(1 / actualMiniBatchSize);
		this.plNet.params().subi(cumulativeDeltaW);
		this.iteration++;
	}

	/**
	 * Updates this {@link PLNetDyadRanker} based on the given {@link IInstance},
	 * which needs to be an {@link IDyadRankingInstance}. The update procedure is
	 * based on algorithm 2 in [1].
	 *
	 *
	 * @param instances
	 *            The {@link IInstance} the update should be based on. Needs to be a
	 *            {@link IDyadRankingInstance}.
	 * @throws TrainingException
	 *             If something fails during the update process.
	 */
	@Override
	public void update(final IDyadRankingInstance instance) throws TrainingException {
		if (this.plNet == null) {
			int dyadSize = (instance.getDyadAtPosition(0).getInstance().length())
					+ (instance.getDyadAtPosition(0).getAlternative().length());
			this.plNet = this.createNetwork(dyadSize);
			this.plNet.init();
		}
		INDArray deltaW = this.computeScaledGradient(instance);
		this.plNet.params().subi(deltaW);
		this.iteration++;
	}

	@Override
	public void update(final Set<IDyadRankingInstance> instances) throws TrainingException {

		List<INDArray> minibatch = new ArrayList<>(instances.size());
		for (IDyadRankingInstance instance : instances) {
			if (this.plNet == null) {
				int dyadSize = (instance.getDyadAtPosition(0).getInstance().length())
						+ (instance.getDyadAtPosition(0).getAlternative().length());
				this.plNet = this.createNetwork(dyadSize);
				this.plNet.init();
			}
			minibatch.add(instance.toMatrix());
		}
		this.updateWithMinibatch(minibatch);
	}

	@Override
	public IDyadRankingInstance predict(final IDyadRankingInstance instance) throws PredictionException {
		if (this.plNet == null) {
			int dyadSize = (instance.getDyadAtPosition(0).getInstance().length())
					+ (instance.getDyadAtPosition(0).getAlternative().length());
			this.plNet = this.createNetwork(dyadSize);
			this.plNet.init();
		}

		List<Pair<Dyad, Double>> dyadUtilityPairs = new ArrayList<>(instance.length());
		for (Dyad dyad : instance) {
			INDArray plNetInput = this.dyadToVector(dyad);
			double plNetOutput = this.plNet.output(plNetInput).getDouble(0);
			dyadUtilityPairs.add(new Pair<Dyad, Double>(dyad, plNetOutput));
		}
		// sort the instance in descending order of utility values
		Collections.sort(dyadUtilityPairs, Comparator.comparing(p -> -p.getRight()));
		List<Dyad> ranking = new ArrayList<>();

		for (Pair<Dyad, Double> pair : dyadUtilityPairs) {
			ranking.add(pair.getLeft());
		}
		return new DyadRankingInstance(ranking);
	}

	@Override
	public List<IDyadRankingInstance> predict(final DyadRankingDataset dataset) throws PredictionException {
		List<IDyadRankingInstance> results = new ArrayList<>(dataset.size());
		for (IDyadRankingInstance instance : dataset) {
			results.add(this.predict(instance));
		}
		return results;
	}

	/**
	 * Computes the average error on a set of dyad rankings in terms on the negative
	 * log likelihood (NLL).
	 *
	 * @param drTest
	 *            Test data on which the error should be computed given as a
	 *            {@link List} of {@link IDyadRankingInstance}
	 * @return Average error on the given test data
	 */
	private double computeAvgError(final List<INDArray> drTest) {
		DescriptiveStatistics stats = new DescriptiveStatistics();
		for (INDArray dyadRankingInstance : drTest) {
			INDArray outputs = this.plNet.output(dyadRankingInstance);
			outputs = outputs.transpose();
			double score = PLNetLoss.computeLoss(outputs).getDouble(0);
			stats.addValue(score);
		}
		return stats.getMean();
	}

	@Override
	public void setConfiguration(final IPredictiveModelConfiguration configuration) throws ConfigurationException {
		if (!(configuration instanceof IPLNetDyadRankerConfiguration)) {
			throw new IllegalArgumentException("The configuration is no PLNetDyadRankerConfiguration!");
		}
		this.configuration = (IPLNetDyadRankerConfiguration) configuration;
	}

	@Override
	public IPredictiveModelConfiguration getConfiguration() {
		return this.configuration;
	}

	/**
	 * Creates a simple feed-forward {@link MultiLayerNetwork} that can be used as a
	 * PLNet for dyad-ranking.
	 *
	 * @param numInputs
	 *            The number of inputs to the network, i.e. the number of features
	 *            of a dyad.
	 * @return New {@link MultiLayerNetwork}
	 */
	private MultiLayerNetwork createNetwork(final int numInputs) {
		if (this.configuration.plNetHiddenNodes().isEmpty()) {
			throw new IllegalArgumentException(
					"There must be at least one hidden layer in specified in the config file!");
		}
		ListBuilder configBuilder = new NeuralNetConfiguration.Builder().seed(this.configuration.plNetSeed())
				// Gradient descent updater: Adam
				.updater(new Adam(this.configuration.plNetLearningRate())).list();

		// Build hidden layers
		String activation = this.configuration.plNetActivationFunction();
		int inputsFirstHiddenLayer = this.configuration.plNetHiddenNodes().get(0);
		configBuilder.layer(0,
				new DenseLayer.Builder().nIn(numInputs).nOut(inputsFirstHiddenLayer)
				.weightInit(WeightInit.SIGMOID_UNIFORM).activation(Activation.fromString(activation))
				.hasBias(true).build());
		List<Integer> hiddenNodes = this.configuration.plNetHiddenNodes();

		for (int i = 0; i < hiddenNodes.size() - 1; i++) {
			int numIn = hiddenNodes.get(i);
			int numOut = hiddenNodes.get(i + 1);
			configBuilder.layer(i + 1,
					new DenseLayer.Builder().nIn(numIn).nOut(numOut).weightInit(WeightInit.SIGMOID_UNIFORM)
					.activation(Activation.fromString(activation)).hasBias(true).build());
		}

		// Build output layer. Since we are using an external error for training,
		// this is a regular layer instead of an OutputLayer
		configBuilder.layer(hiddenNodes.size(), new DenseLayer.Builder().nIn(hiddenNodes.get(hiddenNodes.size() - 1))
				.nOut(1).weightInit(WeightInit.UNIFORM).activation(Activation.IDENTITY).hasBias(true).build());

		MultiLayerConfiguration multiLayerConfig = configBuilder.build();
		return new MultiLayerNetwork(multiLayerConfig);
	}

	/**
	 * Converts a dyad to a {@link INDArray} row vector consisting of a
	 * concatenation of the instance and alternative features.
	 *
	 * @param dyad
	 *            The dyad to convert.
	 * @return The dyad in {@link INDArray} row vector form.
	 */
	private INDArray dyadToVector(final Dyad dyad) {
		INDArray instanceOfDyad = Nd4j.create(dyad.getInstance().asArray());
		INDArray alternativeOfDyad = Nd4j.create(dyad.getAlternative().asArray());
		return Nd4j.hstack(instanceOfDyad, alternativeOfDyad);
	}

	/**
	 * Converts a dyad ranking to a {@link INDArray} matrix where each row
	 * corresponds to a dyad.
	 *
	 * @param drInstance
	 *            The dyad ranking to convert to a matrix.
	 * @return The dyad ranking in {@link INDArray} matrix form.
	 */
	private INDArray dyadRankingToMatrix(final IDyadRankingInstance drInstance) {
		List<INDArray> dyadList = new ArrayList<>(drInstance.length());
		for (Dyad dyad : drInstance) {
			INDArray dyadVector = this.dyadToVector(dyad);
			dyadList.add(dyadVector);
		}
		INDArray dyadMatrix;
		dyadMatrix = Nd4j.vstack(dyadList);
		return dyadMatrix;
	}

	/**
	 * Creates a simple feed-forward {@link MultiLayerNetwork} using the json
	 * representation of a {@link MultiLayerConfiguration} in the file .
	 *
	 * @param configFile
	 *            {@link File} containing the json representation of the
	 *            {@link MultiLayerConfiguration}
	 */
	public void createNetworkFromDl4jConfigFile(final File configFile) {
		String json = "";
		try {
			json = FileUtil.readFileAsString(configFile);
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		MultiLayerConfiguration config = MultiLayerConfiguration.fromJson(json);
		MultiLayerNetwork network = new MultiLayerNetwork(config);
		this.plNet = network;
	}

	/**
	 * Save a trained model at a given file path. Note that the produced file is a
	 * zip file and a ".zip" ending is added.
	 *
	 * @param filePath
	 *            The file path to save to.
	 * @throws IOException
	 */
	public void saveModelToFile(final String filePath) throws IOException {
		if (this.plNet == null) {
			throw new IllegalStateException("Cannot save untrained model.");
		}
		File locationToSave = new File(filePath + ".zip");
		ModelSerializer.writeModel(this.plNet, locationToSave, true);
	}

	/**
	 * Restore a trained model from a given file path. Warning: does not check
	 * whether the loaded model is a valid PLNet or conforms to the configuration of
	 * the object.
	 *
	 * @param filePath
	 *            The file to load from.
	 * @throws IOException
	 */
	public void loadModelFromFile(final String filePath) throws IOException {
		MultiLayerNetwork restored = ModelSerializer.restoreMultiLayerNetwork(filePath);
		this.plNet = restored;
	}

	public MultiLayerNetwork getPlNet() {
		return this.plNet;
	}

	public int getEpoch() {
		return this.epoch;
	}

	@Override
	public double getCertainty(final IDyadRankingInstance queryInstance) {
		if (queryInstance.length() != 2) {
			throw new IllegalArgumentException("Can only provide certainty for pairs of dyads!");
		}
		List<Pair<Dyad, Double>> dyadUtilityPairs = new ArrayList<>(queryInstance.length());
		for (Dyad dyad : queryInstance) {
			INDArray plNetInput = this.dyadToVector(dyad);
			double plNetOutput = this.plNet.output(plNetInput).getDouble(0);
			dyadUtilityPairs.add(new Pair<Dyad, Double>(dyad, plNetOutput));
		}
		return Math.abs(dyadUtilityPairs.get(0).getRight() - dyadUtilityPairs.get(1).getRight());
	}

	/**
	 * Returns the pair of {@link Dyad}s for which the model is least certain.
	 *
	 * @param drInstance
	 *            Ranking for which certainty should be assessed.
	 * @return The pair of {@link Dyad}s for which the model is least certain.
	 */
	public IDyadRankingInstance getPairWithLeastCertainty(final IDyadRankingInstance drInstance) {

		if (this.plNet == null) {
			int dyadSize = (drInstance.getDyadAtPosition(0).getInstance().length())
					+ (drInstance.getDyadAtPosition(0).getAlternative().length());
			this.plNet = this.createNetwork(dyadSize);
			this.plNet.init();
		}

		if (drInstance.length() < 2) {
			throw new IllegalArgumentException("The query instance must contain at least 2 dyads!");
		}
		List<Pair<Dyad, Double>> dyadUtilityPairs = new ArrayList<>(drInstance.length());
		for (Dyad dyad : drInstance) {
			INDArray plNetInput = this.dyadToVector(dyad);
			double plNetOutput = this.plNet.output(plNetInput).getDouble(0);
			dyadUtilityPairs.add(new Pair<Dyad, Double>(dyad, plNetOutput));
		}
		// sort the instance in descending order of utility values
		Collections.sort(dyadUtilityPairs, Comparator.comparing(p -> -p.getRight()));
		int indexOfPairWithLeastCertainty = 0;
		double currentlyLowestCertainty = Double.MAX_VALUE;
		for (int i = 0; i < dyadUtilityPairs.size() - 1; i++) {
			double currentCertainty = Math
					.abs(dyadUtilityPairs.get(i).getRight() - dyadUtilityPairs.get(i + 1).getRight());
			if (currentCertainty < currentlyLowestCertainty) {
				currentlyLowestCertainty = currentCertainty;
				indexOfPairWithLeastCertainty = i;
			}
		}
		List<Dyad> leastCertainDyads = new LinkedList<>();
		leastCertainDyads.add(dyadUtilityPairs.get(indexOfPairWithLeastCertainty).getLeft());
		leastCertainDyads.add(dyadUtilityPairs.get(indexOfPairWithLeastCertainty + 1).getLeft());
		return new DyadRankingInstance(leastCertainDyads);
	}

	/**
	 * Returns the probablity of the top ranking for a given
	 * {@link IDyadRankingInstance} under the Plackett Luce model parametrized by
	 * the latent skill values predicted by the PLNet. This may be useful as the
	 * probability of a particular ranking diminishes drastically with increasing
	 * length of the ranking.
	 *
	 * @param drInstance
	 *            {@link IDyadRankingInstance} for which the probability is
	 *            computed.
	 * @return Probablity of the top ranking for a given
	 *         {@link IDyadRankingInstance} given the Plackett Luce model
	 *         parametrized by the skill values predicted by the PLNet.
	 */
	public double getProbabilityOfTopRanking(final IDyadRankingInstance drInstance) {
		return this.getProbabilityOfTopKRanking(drInstance, drInstance.length());
	}

	private List<Pair<Dyad, Double>> getDyadUtilityPairsForInstance(final IDyadRankingInstance drInstance) {
		if (this.plNet == null) {
			int dyadSize = (drInstance.getDyadAtPosition(0).getInstance().length())
					+ (drInstance.getDyadAtPosition(0).getAlternative().length());
			this.plNet = this.createNetwork(dyadSize);
			this.plNet.init();
		}

		List<Pair<Dyad, Double>> dyadUtilityPairs = new ArrayList<>(drInstance.length());
		for (Dyad dyad : drInstance) {
			INDArray plNetInput = this.dyadToVector(dyad);
			double plNetOutput = this.plNet.output(plNetInput).getDouble(0);
			dyadUtilityPairs.add(new Pair<Dyad, Double>(dyad, plNetOutput));
		}
		return dyadUtilityPairs;
	}

	private List<Pair<Dyad, Double>> getSortedDyadUtilityPairsForInstance(final IDyadRankingInstance drInstance) {
		List<Pair<Dyad, Double>> dyadUtilityPairs = this.getDyadUtilityPairsForInstance(drInstance);
		Collections.sort(dyadUtilityPairs, Comparator.comparing(p -> -p.getRight()));
		return dyadUtilityPairs;
	}

	public double getProbabilityOfTopKRanking(final IDyadRankingInstance drInstance, final int k) {

		List<Pair<Dyad, Double>> dyadUtilityPairs = this.getSortedDyadUtilityPairsForInstance(drInstance);

		// compute the probability of this ranking according to the Plackett-Luce model
		double currentProbability = 1;
		for (int i = 0; i < Integer.min(k, dyadUtilityPairs.size()); i++) {
			double sumOfRemainingSkills = 0;
			for (int j = i; j < Integer.min(k, dyadUtilityPairs.size()); j++) {
				sumOfRemainingSkills += Math.exp(dyadUtilityPairs.get(j).getRight());
			}
			if (sumOfRemainingSkills != 0) {
				currentProbability *= (Math.exp(dyadUtilityPairs.get(i).getRight()) / sumOfRemainingSkills);
			} else {
				currentProbability = Double.NaN;
			}
		}
		return currentProbability;
	}

	/**
	 * Returns the the log of the probablity of the top ranking for a given
	 * {@link IDyadRankingInstance} under the Plackett Luce model parametrized by
	 * the latent skill values predicted by the PLNet. This may be useful as the
	 * probability of a particular ranking diminishes drastically with increasing
	 * length of the ranking.
	 *
	 * @param drInstance
	 *            {@link IDyadRankingInstance} for which the probability is
	 *            computed.
	 * @return Log of the probablity of the top ranking for a given
	 *         {@link IDyadRankingInstance} given the Plackett Luce model
	 *         parametrized by the skill values predicted by the PLNet.
	 */
	public double getLogProbabilityOfTopRanking(final IDyadRankingInstance drInstance) {
		return this.getLogProbabilityOfTopKRanking(drInstance, Integer.MAX_VALUE);
	}

	/**
	 * Returns the log of the probablity of the top k of a given
	 * {@link IDyadRankingInstance} under the Plackett Luce model parametrized by
	 * the latent skill values predicted by the PLNet. This may be useful as the
	 * probability of a particular ranking diminishes drastically with increasing
	 * length of the ranking.
	 *
	 * @param drInstance
	 *            {@link IDyadRankingInstance} for which the probability is
	 *            computed.
	 * @param k
	 *            Number of top dyads to be considered.
	 * @return Log of the probablity of the top k of a the given
	 *         {@link IDyadRankingInstance} given the Plackett Luce model
	 *         parametrized by the skill values predicted by the PLNet.
	 */
	public double getLogProbabilityOfTopKRanking(final IDyadRankingInstance drInstance, final int k) {
		List<Pair<Dyad, Double>> dyadUtilityPairs = this.getSortedDyadUtilityPairsForInstance(drInstance);

		// compute the probability of this ranking according to the Plackett-Luce model
		double currentProbability = 0;
		for (int i = 0; i < Integer.min(k, dyadUtilityPairs.size()); i++) {
			double sumOfRemainingSkills = 0;
			for (int j = i; j < Integer.min(k, dyadUtilityPairs.size()); j++) {
				sumOfRemainingSkills += Math.exp(dyadUtilityPairs.get(j).getRight());
			}
			currentProbability += (dyadUtilityPairs.get(i).getRight() - Math.log(sumOfRemainingSkills));
		}
		return currentProbability;
	}

	/**
	 * Returns the probablity of a given {@link IDyadRankingInstance} under the
	 * Plackett Luce model parametrized by the latent skill values predicted by the
	 * PLNet.
	 *
	 * @param drInstance
	 *            {@link IDyadRankingInstance} for which the probability is
	 *            computed.
	 * @return Probability of the given {@link IDyadRankingInstance} given the
	 *         Plackett Luce model parametrized by the skill values predicted by the
	 *         PLNet.
	 */
	public double getProbabilityRanking(final IDyadRankingInstance drInstance) {

		List<Pair<Dyad, Double>> dyadUtilityPairs = this.getDyadUtilityPairsForInstance(drInstance);

		// compute the probability of this ranking according to the Plackett-Luce model
		double currentProbability = 1;
		for (int i = 0; i < dyadUtilityPairs.size(); i++) {
			double sumOfRemainingSkills = 0;
			for (int j = i; j < dyadUtilityPairs.size(); j++) {
				sumOfRemainingSkills += Math.exp(dyadUtilityPairs.get(j).getRight());
			}
			if (sumOfRemainingSkills != 0) {
				currentProbability *= (Math.exp(dyadUtilityPairs.get(i).getRight()) / sumOfRemainingSkills);
			} else {
				currentProbability = Double.NaN;
			}
		}
		return currentProbability;
	}

	/**
	 * Computes the logarithmic probability for a particular ranking according to
	 * the log Placket-Luce model.
	 *
	 * @param drInstance
	 * @return Logarithmic probability of the given ranking.
	 */
	public double getLogProbabilityRanking(final IDyadRankingInstance drInstance) {
		List<Pair<Dyad, Double>> dyadUtilityPairs = this.getDyadUtilityPairsForInstance(drInstance);

		// compute the probability of this ranking according to the Plackett-Luce model
		double currentProbability = 0;
		for (int i = 0; i < dyadUtilityPairs.size(); i++) {
			double sumOfRemainingSkills = 0;
			for (int j = i; j < dyadUtilityPairs.size(); j++) {
				sumOfRemainingSkills += dyadUtilityPairs.get(j).getRight();
			}
			currentProbability += (dyadUtilityPairs.get(i).getRight() - sumOfRemainingSkills);
		}
		return currentProbability;
	}

	/**
	 * Returns the latent skill value predicted by the PLNet for a given
	 * {@link Dyad}.
	 *
	 * @param dyad
	 *            {@link Dyad} for which the skill is to be predicted.
	 * @return Skill of the given {@link Dyad}.
	 */
	public double getSkillForDyad(final Dyad dyad) {
		if (this.plNet == null) {
			return Double.NaN;
		}
		INDArray plNetInput = this.dyadToVector(dyad);
		return this.plNet.output(plNetInput).getDouble(0);
	}

}