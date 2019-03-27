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
import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.IInstance;
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
public class PLNetDyadRanker extends APLDyadRanker
		implements IOnlineLearner<IDyadRankingInstance>, ICertaintyProvider<IDyadRankingInstance> {

	private static final Logger log = LoggerFactory.getLogger(PLNetDyadRanker.class);

	private MultiLayerNetwork plNet;
	private IPLNetDyadRankerConfiguration configuration;
	private int epoch;
	private int iteration;
	private double currentBestScore;
	private MultiLayerNetwork currentBestModel;

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
	 * @param config Configuration for the {@link PLNetDyadRanker}.
	 */
	public PLNetDyadRanker(IPLNetDyadRankerConfiguration config) {
		this.configuration = config;
	}

	@Override
	public void train(IDataset dataset) throws TrainingException {
		if (!(dataset instanceof DyadRankingDataset)) {
			throw new IllegalArgumentException(
					"Can only train the Plackett-Luce net dyad ranker with a dyad ranking dataset!");
		}
		DyadRankingDataset drDataset = (DyadRankingDataset) dataset;
		train(drDataset.toND4j());
	}

	public void train(List<INDArray> dataset) throws TrainingException {
		train(dataset, configuration.plNetMaxEpochs(), configuration.plNetEarlyStoppingTrainRatio());
		if (configuration.plNetEarlyStoppingRetrain()) {
			int maxEpochs = epoch;
			this.plNet = null;
			train(dataset, maxEpochs, 1.0);
		}
	}

	public void train(DyadRankingDataset dataset, int maxEpochs, double earlyStoppingTrainRatio)
			throws TrainingException {
		train(dataset.toND4j(), maxEpochs, earlyStoppingTrainRatio);
	}

	public void train(List<INDArray> dataset, int maxEpochs, double earlyStoppingTrainRatio) throws TrainingException {
		List<INDArray> drTrain = dataset.subList(0, (int) (earlyStoppingTrainRatio * dataset.size()));
		List<INDArray> drTest = dataset.subList((int) (earlyStoppingTrainRatio * dataset.size()), dataset.size());

		if (this.plNet == null) {
			int dyadSize = dataset.get(0).columns();
			this.plNet = createNetwork(dyadSize);
			this.plNet.init();
		}

		currentBestScore = Double.POSITIVE_INFINITY;
		currentBestModel = this.plNet;
		epoch = 0;
		iteration = 0;
		int patience = 0;
		int earlyStoppingCounter = 0;

		while ((patience < configuration.plNetEarlyStoppingPatience()
				|| configuration.plNetEarlyStoppingPatience() <= 0) && (epoch < maxEpochs || maxEpochs == 0)) {
			// Iterate through training data
			int miniBatchSize = configuration.plNetMiniBatchSize();
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
			log.debug("plNet params: {}", plNet.params().toString());
			earlyStoppingCounter++;
			// Compute validation error
			if (earlyStoppingCounter == configuration.plNetEarlyStoppingInterval() && earlyStoppingTrainRatio < 1.0) {
				double avgScore = computeAvgError(drTest);
				if (avgScore < currentBestScore) {
					currentBestScore = avgScore;
					currentBestModel = plNet.clone();
					log.debug("current best score: {}", currentBestScore);
					patience = 0;
				} else {
					patience++;
				}
				earlyStoppingCounter = 0;
			}
			epoch++;
		}
		plNet = currentBestModel;
	}

	private INDArray computeScaledGradient(INDArray dyadMatrix) {
		int dyadRankingLength = dyadMatrix.rows();
		List<INDArray> activations = plNet.feedForward(dyadMatrix);
		INDArray output = activations.get(activations.size() - 1);
		output = output.transpose();
		INDArray deltaW = Nd4j.zeros(plNet.params().length());
		Gradient deltaWk = null;
		MultiLayerNetwork plNetClone = plNet.clone();
		for (int k = 0; k < dyadRankingLength; k++) {
			// compute derivative of loss w.r.t. k
			plNetClone.setInput(dyadMatrix.getRow(k));
			plNetClone.feedForward(true, false);
			INDArray lossGradient = PLNetLoss.computeLossGradient(output, k);
			// compute backprop gradient for weight updates w.r.t. k
			Pair<Gradient, INDArray> p = plNetClone.backpropGradient(lossGradient, null);
			deltaWk = p.getFirst();
			plNet.getUpdater().update(plNet, deltaWk, iteration, epoch, 1, LayerWorkspaceMgr.noWorkspaces());
			deltaW.addi(deltaWk.gradient());
		}

		return deltaW;
	}

	/**
	 * Computes the gradient of the plNets' error function for a given instance. The
	 * returned gradient is already scaled by the updater. The update procedure is
	 * based on algorithm 2 in [1].
	 * 
	 * @param instance The instance to compute the scaled gradient for.
	 * @return The gradient for the given instance, multiplied by the updater's
	 *         learning rate.
	 */
	private INDArray computeScaledGradient(IInstance instance) {
		if (!(instance instanceof IDyadRankingInstance)) {
			throw new IllegalArgumentException(
					"Can only update the Plackett-Luce net dyad ranker with a dyad ranking instance!");
		}

		IDyadRankingInstance drInstance = (IDyadRankingInstance) instance;
		// init weight update vector
		INDArray dyadMatrix;
		List<INDArray> dyadList = new ArrayList<INDArray>(drInstance.length());
		for (Dyad dyad : drInstance) {
			INDArray dyadVector = dyadToVector(dyad);
			dyadList.add(dyadVector);
		}
		dyadMatrix = dyadRankingToMatrix(drInstance);
		List<INDArray> activations = plNet.feedForward(dyadMatrix);
		INDArray output = activations.get(activations.size() - 1);
		output = output.transpose();
		INDArray deltaW = Nd4j.zeros(plNet.params().length());
		Gradient deltaWk = null;
		MultiLayerNetwork plNetClone = plNet.clone();
		for (int k = 0; k < drInstance.length(); k++) {
			// compute derivative of loss w.r.t. k
			plNetClone.setInput(dyadList.get(k));
			plNetClone.feedForward(true, false);
			INDArray lossGradient = PLNetLoss.computeLossGradient(output, k);
			// compute backprop gradient for weight updates w.r.t. k
			Pair<Gradient, INDArray> p = plNetClone.backpropGradient(lossGradient, null);
			deltaWk = p.getFirst();
			plNet.getUpdater().update(plNet, deltaWk, iteration, epoch, 1, LayerWorkspaceMgr.noWorkspaces());
			deltaW.addi(deltaWk.gradient());
		}

		return deltaW;
	}

	/**
	 * Updates this {@link PLNetDyadRanker} based on a given mini batch of
	 * {@link INDarray}s representing dyad rankings.
	 * 
	 * @param minibatch A mini batch consisting of a {@link List} of
	 *                  {@link INDarray}.
	 */
	private void updateWithMinibatch(List<INDArray> minibatch) {
		double actualMiniBatchSize = minibatch.size();
		INDArray cumulativeDeltaW = Nd4j.zeros(plNet.params().length());
		for (INDArray instance : minibatch) {
			cumulativeDeltaW.addi(computeScaledGradient(instance));
		}
		cumulativeDeltaW.muli(1 / actualMiniBatchSize);
		plNet.params().subi(cumulativeDeltaW);
		iteration++;
	}

	/**
	 * Updates this {@link PLNetDyadRanker} based on the given {@link IInstance},
	 * which needs to be an {@link IDyadRankingInstance}. The update procedure is
	 * based on algorithm 2 in [1].
	 * 
	 * 
	 * @param instances The {@link IInstance} the update should be based on. Needs
	 *                  to be a {@link IDyadRankingInstance}.
	 * @throws TrainingException If something fails during the update process.
	 */
	@Override
	public void update(IInstance instance) throws TrainingException {
		if (!(instance instanceof IDyadRankingInstance)) {
			throw new IllegalArgumentException(
					"Can only train the Plackett-Luce net dyad ranker with a dyad ranking instances!");
		}
		IDyadRankingInstance drInstance = (IDyadRankingInstance) instance;
		if (this.plNet == null) {
			int dyadSize = (drInstance.getDyadAtPosition(0).getInstance().length())
					+ (drInstance.getDyadAtPosition(0).getAlternative().length());
			this.plNet = createNetwork(dyadSize);
			this.plNet.init();
		}
		INDArray deltaW = computeScaledGradient(instance);
		plNet.params().subi(deltaW);
		iteration++;
	}

	@Override
	public void update(Set<IInstance> instances) throws TrainingException {

		List<INDArray> minibatch = new ArrayList<INDArray>(instances.size());
		for (IInstance instance : instances) {
			if (!(instance instanceof IDyadRankingInstance)) {
				throw new IllegalArgumentException(
						"Can only train the Plackett-Luce net dyad ranker with dyad ranking instances!");
			}
			IDyadRankingInstance drInstance = ((IDyadRankingInstance) instance);
			if (this.plNet == null) {
				int dyadSize = (drInstance.getDyadAtPosition(0).getInstance().length())
						+ (drInstance.getDyadAtPosition(0).getAlternative().length());
				this.plNet = createNetwork(dyadSize);
				this.plNet.init();
			}
			minibatch.add(drInstance.toMatrix());
		}
		this.updateWithMinibatch(minibatch);
	}

	@Override
	public IDyadRankingInstance predict(IInstance instance) throws PredictionException {
		if (!(instance instanceof IDyadRankingInstance)) {
			throw new IllegalArgumentException(
					"Can only make prediction for dyad ranking instances using the Plackett-Luce net dyad ranker!");
		}

		IDyadRankingInstance drInstance = (IDyadRankingInstance) instance;

		if (this.plNet == null) {
			int dyadSize = (drInstance.getDyadAtPosition(0).getInstance().length())
					+ (drInstance.getDyadAtPosition(0).getAlternative().length());
			this.plNet = createNetwork(dyadSize);
			this.plNet.init();
		}

		List<Pair<Dyad, Double>> dyadUtilityPairs = new ArrayList<Pair<Dyad, Double>>(drInstance.length());
		for (Dyad dyad : drInstance) {
			INDArray plNetInput = dyadToVector(dyad);
			double plNetOutput = plNet.output(plNetInput).getDouble(0);
			dyadUtilityPairs.add(new Pair<Dyad, Double>(dyad, plNetOutput));
		}
		// sort the instance in descending order of utility values
		Collections.sort(dyadUtilityPairs, Comparator.comparing(p -> -p.getRight()));
		List<Dyad> ranking = new ArrayList<Dyad>();

		for (Pair<Dyad, Double> pair : dyadUtilityPairs)
			ranking.add(pair.getLeft());
		return new DyadRankingInstance(ranking);
	}

	@Override
	public List<IDyadRankingInstance> predict(IDataset dataset) throws PredictionException {
		if (!(dataset instanceof DyadRankingDataset)) {
			throw new IllegalArgumentException(
					"Can only make predictions for dyad ranking datasets using the Plackett-Luce net dyad ranker!");
		}
		DyadRankingDataset drDataset = (DyadRankingDataset) dataset;
		List<IDyadRankingInstance> results = new ArrayList<IDyadRankingInstance>(dataset.size());
		for (IInstance instance : drDataset) {
			results.add(this.predict(instance));
		}
		return results;
	}

	/**
	 * Computes the average error on a set of dyad rankings in terms on the negative
	 * log likelihood (NLL).
	 * 
	 * @param drTest Test data on which the error should be computed given as a
	 *               {@link List} of {@link IDyadRankingInstance}
	 * @return Average error on the given test data
	 */
	private double computeAvgError(List<INDArray> drTest) {
		DescriptiveStatistics stats = new DescriptiveStatistics();
		for (INDArray dyadRankingInstance : drTest) {
			// IDyadRankingInstance drInstance = (IDyadRankingInstance) dyadRankingInstance;
			// INDArray dyadMatrix = dyadRankingToMatrix(drInstance);
			INDArray outputs = plNet.output(dyadRankingInstance);
			outputs = outputs.transpose();
			double score = PLNetLoss.computeLoss(outputs).getDouble(0);
			stats.addValue(score);
		}
		return stats.getMean();
	}

	@Override
	public void setConfiguration(IPredictiveModelConfiguration configuration) throws ConfigurationException {
		if (!(configuration instanceof IPLNetDyadRankerConfiguration)) {
			throw new IllegalArgumentException("The configuration is no PLNetDyadRankerConfiguration!");
		}
		this.configuration = (IPLNetDyadRankerConfiguration) configuration;
	}

	@Override
	public IPredictiveModelConfiguration getConfiguration() {
		return configuration;
	}

	/**
	 * Creates a simple feed-forward {@link MultiLayerNetwork} that can be used as a
	 * PLNet for dyad-ranking.
	 * 
	 * @param numInputs The number of inputs to the network, i.e. the number of
	 *                  features of a dyad.
	 * @return New {@link MultiLayerNetwork}
	 */
	private MultiLayerNetwork createNetwork(int numInputs) {
		if (this.configuration.plNetHiddenNodes().isEmpty())
			throw new IllegalArgumentException(
					"There must be at least one hidden layer in specified in the config file!");
		ListBuilder configBuilder = new NeuralNetConfiguration.Builder().seed(configuration.plNetSeed())
				// Gradient descent updater: Adam
				.updater(new Adam(configuration.plNetLearningRate())).list();

		// Build hidden layers
		String activation = configuration.plNetActivationFunction();
		int inputsFirstHiddenLayer = configuration.plNetHiddenNodes().get(0);
		configBuilder.layer(0,
				new DenseLayer.Builder().nIn(numInputs).nOut(inputsFirstHiddenLayer)
						.weightInit(WeightInit.SIGMOID_UNIFORM).activation(Activation.fromString(activation))
						.hasBias(true).build());
		List<Integer> hiddenNodes = configuration.plNetHiddenNodes();

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
	 * @param dyad The dyad to convert.
	 * @return The dyad in {@link INDArray} row vector form.
	 */
	private INDArray dyadToVector(Dyad dyad) {
		INDArray instanceOfDyad = Nd4j.create(dyad.getInstance().asArray());
		INDArray alternativeOfDyad = Nd4j.create(dyad.getAlternative().asArray());
		INDArray dyadVector = Nd4j.hstack(instanceOfDyad, alternativeOfDyad);
		return dyadVector;
	}

	/**
	 * Converts a dyad ranking to a {@link INDArray} matrix where each row
	 * corresponds to a dyad.
	 * 
	 * @param drInstance The dyad ranking to convert to a matrix.
	 * @return The dyad ranking in {@link INDArray} matrix form.
	 */
	private INDArray dyadRankingToMatrix(IDyadRankingInstance drInstance) {
		List<INDArray> dyadList = new ArrayList<INDArray>(drInstance.length());
		for (Dyad dyad : drInstance) {
			INDArray dyadVector = dyadToVector(dyad);
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
	 * @param configFile {@link File} containing the json representation of the
	 *                   {@link MultiLayerConfiguration}
	 */
	public void createNetworkFromDl4jConfigFile(File configFile) {
		String json = "";
		try {
			json = FileUtil.readFileAsString(configFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		MultiLayerConfiguration config = MultiLayerConfiguration.fromJson(json);
		MultiLayerNetwork network = new MultiLayerNetwork(config);
		this.plNet = network;
	}

	/**
	 * Save a trained model at a given file path. Note that the produced file is a
	 * zip file and a ".zip" ending is added.
	 * 
	 * @param filePath The file path to save to.
	 * @throws IOException
	 */
	public void saveModelToFile(String filePath) throws IOException {
		if (plNet == null) {
			throw new IllegalStateException("Cannot save untrained model.");
		}
		File locationToSave = new File(filePath + ".zip");
		ModelSerializer.writeModel(plNet, locationToSave, true);
	}

	/**
	 * Restore a trained model from a given file path. Warning: does not check
	 * whether the loaded model is a valid PLNet or conforms to the configuration of
	 * the object.
	 * 
	 * @param filePath The file to load from.
	 * @throws IOException
	 */
	public void loadModelFromFile(String filePath) throws IOException {
		MultiLayerNetwork restored = ModelSerializer.restoreMultiLayerNetwork(filePath);
		plNet = restored;
	}

	public MultiLayerNetwork getPlNet() {
		return plNet;
	}

	public int getEpoch() {
		return epoch;
	}

	@Override
	public double getCertainty(IInstance queryInstance) {
		if (!(queryInstance instanceof IDyadRankingInstance)) {
			throw new IllegalArgumentException("Can only provide certainty for dyad ranking instances!");
		}
		IDyadRankingInstance drInstance = (IDyadRankingInstance) queryInstance;

		if (drInstance.length() != 2) {
			throw new IllegalArgumentException("Can only provide certainty for pairs of dyads!");
		}
		List<Pair<Dyad, Double>> dyadUtilityPairs = new ArrayList<Pair<Dyad, Double>>(drInstance.length());
		for (Dyad dyad : drInstance) {
			INDArray plNetInput = dyadToVector(dyad);
			double plNetOutput = plNet.output(plNetInput).getDouble(0);
			dyadUtilityPairs.add(new Pair<Dyad, Double>(dyad, plNetOutput));
		}
		return Math.abs(dyadUtilityPairs.get(0).getRight() - dyadUtilityPairs.get(1).getRight());
	}

	/**
	 * Returns the pair of {@link Dyad}s for which the model is least certain.
	 * 
	 * @param drInstance Ranking for which certainty should be assessed.
	 * @return The pair of {@link Dyad}s for which the model is least certain.
	 */
	public DyadRankingInstance getPairWithLeastCertainty(IDyadRankingInstance drInstance) {

		if (this.plNet == null) {
			int dyadSize = (drInstance.getDyadAtPosition(0).getInstance().length())
					+ (drInstance.getDyadAtPosition(0).getAlternative().length());
			this.plNet = createNetwork(dyadSize);
			this.plNet.init();
		}

		if (drInstance.length() < 2) {
			throw new IllegalArgumentException("The query instance must contain at least 2 dyads!");
		}
		List<Pair<Dyad, Double>> dyadUtilityPairs = new ArrayList<Pair<Dyad, Double>>(drInstance.length());
		for (Dyad dyad : drInstance) {
			INDArray plNetInput = dyadToVector(dyad);
			double plNetOutput = plNet.output(plNetInput).getDouble(0);
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
		List<Dyad> leastCertainDyads = new LinkedList<Dyad>();
		leastCertainDyads.add(dyadUtilityPairs.get(indexOfPairWithLeastCertainty).getLeft());
		leastCertainDyads.add(dyadUtilityPairs.get(indexOfPairWithLeastCertainty + 1).getLeft());
		DyadRankingInstance leastCertainPair = new DyadRankingInstance(leastCertainDyads);
		return leastCertainPair;
	}

	/**
	 * Returns the probablity of the top ranking for a given
	 * {@link IDyadRankingInstance} under the Plackett Luce model parametrized by
	 * the latent skill values predicted by the PLNet. This may be useful as the
	 * probability of a particular ranking diminishes drastically with increasing
	 * length of the ranking.
	 * 
	 * @param drInstance {@link IDyadRankingInstance} for which the probability is
	 *                   computed.
	 * @return Probablity of the top ranking for a given
	 *         {@link IDyadRankingInstance} given the Plackett Luce model
	 *         parametrized by the skill values predicted by the PLNet.
	 */
	public double getProbabilityOfTopRanking(IDyadRankingInstance drInstance) {
		return getProbabilityOfTopKRanking(drInstance, drInstance.length());
	}

	public double getProbabilityOfTopKRanking(IDyadRankingInstance drInstance, int k) {

		if (this.plNet == null) {
			int dyadSize = (drInstance.getDyadAtPosition(0).getInstance().length())
					+ (drInstance.getDyadAtPosition(0).getAlternative().length());
			this.plNet = createNetwork(dyadSize);
			this.plNet.init();
		}

		List<Pair<Dyad, Double>> dyadUtilityPairs = new ArrayList<Pair<Dyad, Double>>(drInstance.length());
		for (Dyad dyad : drInstance) {
			INDArray plNetInput = dyadToVector(dyad);
			double plNetOutput = plNet.output(plNetInput).getDouble(0);
			dyadUtilityPairs.add(new Pair<Dyad, Double>(dyad, plNetOutput));
		}
		// sort the instance in descending order of utility values
		// TODO use top k selection
		Collections.sort(dyadUtilityPairs, Comparator.comparing(p -> -p.getRight()));
		// compute the probability of this ranking according to the Plackett-Luce model
		double currentProbability = 1;
		for (int i = 0; i < Integer.min(k, dyadUtilityPairs.size()); i++) {
			double sumOfRemainingSkills = 0;
			for (int j = i; j < Integer.min(k, dyadUtilityPairs.size()); j++) {
				sumOfRemainingSkills += Math.exp(dyadUtilityPairs.get(j).getRight());
			}
			currentProbability *= (Math.exp(dyadUtilityPairs.get(i).getRight()) / sumOfRemainingSkills);
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
	 * @param drInstance {@link IDyadRankingInstance} for which the probability is
	 *                   computed.
	 * @return Log of the probablity of the top ranking for a given
	 *         {@link IDyadRankingInstance} given the Plackett Luce model
	 *         parametrized by the skill values predicted by the PLNet.
	 */
	public double getLogProbabilityOfTopRanking(IDyadRankingInstance drInstance) {

		if (this.plNet == null) {
			int dyadSize = (drInstance.getDyadAtPosition(0).getInstance().length())
					+ (drInstance.getDyadAtPosition(0).getAlternative().length());
			this.plNet = createNetwork(dyadSize);
			this.plNet.init();
		}

		List<Pair<Dyad, Double>> dyadUtilityPairs = new ArrayList<Pair<Dyad, Double>>(drInstance.length());
		for (Dyad dyad : drInstance) {
			INDArray plNetInput = dyadToVector(dyad);
			double plNetOutput = plNet.output(plNetInput).getDouble(0);
			dyadUtilityPairs.add(new Pair<Dyad, Double>(dyad, plNetOutput));
		}
		// sort the instance in descending order of utility values
		// TODO use top k selection
		Collections.sort(dyadUtilityPairs, Comparator.comparing(p -> -p.getRight()));
		// compute the probability of this ranking according to the Plackett-Luce model
		double currentProbability = 0;
		for (int i = 0; i < dyadUtilityPairs.size(); i++) {
			double sumOfRemainingSkills = 0;
			for (int j = i; j < dyadUtilityPairs.size(); j++) {
				sumOfRemainingSkills += Math.exp(dyadUtilityPairs.get(j).getRight());
			}
			currentProbability += (dyadUtilityPairs.get(i).getRight() - Math.log(sumOfRemainingSkills));
		}
		return currentProbability;
	}

	/**
	 * Returns the log of the probablity of the top k of a given
	 * {@link IDyadRankingInstance} under the Plackett Luce model parametrized by
	 * the latent skill values predicted by the PLNet. This may be useful as the
	 * probability of a particular ranking diminishes drastically with increasing
	 * length of the ranking.
	 * 
	 * @param drInstance {@link IDyadRankingInstance} for which the probability is
	 *                   computed.
	 * @param k          Number of top dyads to be considered.
	 * @return Log of the probablity of the top k of a the given
	 *         {@link IDyadRankingInstance} given the Plackett Luce model
	 *         parametrized by the skill values predicted by the PLNet.
	 */
	public double getLogProbabilityOfTopKRanking(IDyadRankingInstance drInstance, int k) {

		if (this.plNet == null) {
			int dyadSize = (drInstance.getDyadAtPosition(0).getInstance().length())
					+ (drInstance.getDyadAtPosition(0).getAlternative().length());
			this.plNet = createNetwork(dyadSize);
			this.plNet.init();
		}

		List<Pair<Dyad, Double>> dyadUtilityPairs = new ArrayList<Pair<Dyad, Double>>(drInstance.length());
		for (Dyad dyad : drInstance) {
			INDArray plNetInput = dyadToVector(dyad);
			double plNetOutput = plNet.output(plNetInput).getDouble(0);
			dyadUtilityPairs.add(new Pair<Dyad, Double>(dyad, plNetOutput));
		}
		// sort the instance in descending order of utility values
		// TODO use top k selection
		Collections.sort(dyadUtilityPairs, Comparator.comparing(p -> -p.getRight()));
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
	 * @param drInstance {@link IDyadRankingInstance} for which the probability is
	 *                   computed.
	 * @return Probability of the given {@link IDyadRankingInstance} given the
	 *         Plackett Luce model parametrized by the skill values predicted by the
	 *         PLNet.
	 */
	public double getProbabilityRanking(IDyadRankingInstance drInstance) {

		if (this.plNet == null) {
			int dyadSize = (drInstance.getDyadAtPosition(0).getInstance().length())
					+ (drInstance.getDyadAtPosition(0).getAlternative().length());
			this.plNet = createNetwork(dyadSize);
			this.plNet.init();
		}

		List<Pair<Dyad, Double>> dyadUtilityPairs = new ArrayList<Pair<Dyad, Double>>(drInstance.length());
		for (Dyad dyad : drInstance) {
			INDArray plNetInput = dyadToVector(dyad);
			double plNetOutput = plNet.output(plNetInput).getDouble(0);
			dyadUtilityPairs.add(new Pair<Dyad, Double>(dyad, plNetOutput));
		}

		// compute the probability of this ranking according to the Plackett-Luce model
		double currentProbability = 1;
		for (int i = 0; i < dyadUtilityPairs.size(); i++) {
			double sumOfRemainingSkills = 0;
			for (int j = i; j < dyadUtilityPairs.size(); j++) {
				sumOfRemainingSkills += Math.exp(dyadUtilityPairs.get(j).getRight());
			}
			currentProbability *= (Math.exp(dyadUtilityPairs.get(i).getRight()) / sumOfRemainingSkills);
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
	public double getLogProbabilityRanking(IDyadRankingInstance drInstance) {

		if (this.plNet == null) {
			int dyadSize = (drInstance.getDyadAtPosition(0).getInstance().length())
					+ (drInstance.getDyadAtPosition(0).getAlternative().length());
			this.plNet = createNetwork(dyadSize);
			this.plNet.init();
		}

		List<Pair<Dyad, Double>> dyadUtilityPairs = new ArrayList<Pair<Dyad, Double>>(drInstance.length());
		for (Dyad dyad : drInstance) {
			INDArray plNetInput = dyadToVector(dyad);
			double plNetOutput = plNet.output(plNetInput).getDouble(0);
			dyadUtilityPairs.add(new Pair<Dyad, Double>(dyad, plNetOutput));
		}

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
	 * @param dyad {@link Dyad} for which the skill is to be predicted.
	 * @return Skill of the given {@link Dyad}.
	 */
	public double getSkillForDyad(Dyad dyad) {
		if (plNet == null)
			return Double.NaN;
		INDArray plNetInput = dyadToVector(dyad);
		double plNetOutput = plNet.output(plNetInput).getDouble(0);
		return plNetOutput;
	}

}