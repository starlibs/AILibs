package jaicore.ml.dyadranking.algorithm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import org.nd4j.linalg.learning.config.Sgd;
import org.nd4j.linalg.primitives.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.FileUtil;
import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.core.exception.ConfigurationException;
import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.core.exception.TrainingException;
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
public class PLNetDyadRanker extends APLDyadRanker implements IOnlineLearner<IDyadRankingInstance> {

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
	
	public void train(IDataset dataset, int maxEpochs, double earlyStoppingTrainRatio) throws TrainingException {
		if (!(dataset instanceof DyadRankingDataset)) {
			throw new IllegalArgumentException(
					"Can only train the Plackett-Luce net dyad ranker with a dyad ranking dataset!");
		}
		DyadRankingDataset drDataset = (DyadRankingDataset) dataset;

		List<IInstance> drTrain = (List<IInstance>) drDataset.subList(0,
				(int) (earlyStoppingTrainRatio * drDataset.size()));
		List<IInstance> drTest = (List<IInstance>) drDataset
				.subList((int) (earlyStoppingTrainRatio * drDataset.size()), drDataset.size());

		if (this.plNet == null) {
			int dyadSize = ((IDyadRankingInstance) drDataset.get(0)).getDyadAtPosition(0).getInstance().length()
					+ ((IDyadRankingInstance) drDataset.get(0)).getDyadAtPosition(0).getAlternative().length();
			this.plNet = createNetwork(dyadSize);
			this.plNet.init();
		}

		currentBestScore = Double.POSITIVE_INFINITY;
		currentBestModel = this.plNet;
		epoch = 0;
		iteration = 0;
		int patience = 0;
		int earlyStoppingCounter = 0;

		while ((patience < configuration.plNetEarlyStoppingPatience() || configuration.plNetEarlyStoppingPatience() <= 0) && (epoch < maxEpochs || maxEpochs == 0)) {
			// Iterate through training data
			int miniBatchSize = configuration.plNetMiniBatchSize();
			List<IInstance> miniBatch = new ArrayList<>(miniBatchSize);
			for (IInstance dyadRankingInstance : drTrain) {
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
			if (earlyStoppingCounter == configuration.plNetEarlyStoppingInterval()
					&& earlyStoppingTrainRatio < 1.0) {
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
	
	@Override
	public void train(IDataset dataset) throws TrainingException {
		train(dataset, configuration.plNetMaxEpochs(), configuration.plNetEarlyStoppingTrainRatio());
		if (configuration.plNetEarlyStoppingRetrain()) {
			int maxEpochs = epoch;
			this.plNet = null;
			train(dataset, maxEpochs, 1.0);
		}
	}
	
	/**
	 * Computes the gradient of the plNets' error function for a given instance.
	 * The returned gradient is already scaled by the updater.
	 * The update procedure is  based on algorithm 2 in [1].
	 * 
	 * @param instance	The instance to compute the scaled gradient for.
	 * @return			The gradient for the given instance, multiplied by the updater's learning rate.
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
	 * Updates this {@link PLNetDyadRanker} based on a given mini batch of {@link IInstance}s
	 * which need to be {@link IDyadRankingInstance}s
	 * 
	 * @param minibatch	A mini batch consisting of a {@link List} of {@link IDyadRankingInstance}.
	 */
	private void updateWithMinibatch(List<IInstance> minibatch) {
		double actualMiniBatchSize = minibatch.size();
		INDArray cumulativeDeltaW = Nd4j.zeros(plNet.params().length());
		for (IInstance instance : minibatch) {
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
		INDArray deltaW = computeScaledGradient(instance);
		plNet.params().subi(deltaW);
		iteration++;
	}

	@Override
	public void update(Set<IInstance> instances) throws TrainingException {
		for (IInstance instance : instances)
			this.update(instance);
	}

	@Override
	public IDyadRankingInstance predict(IInstance instance) throws PredictionException {
		if (!(instance instanceof IDyadRankingInstance)) {
			throw new IllegalArgumentException(
					"Can only make prediction for dyad ranking instances using the Plackett-Luce net dyad ranker!");
		}
		IDyadRankingInstance drInstance = (IDyadRankingInstance) instance;
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
	private double computeAvgError(List<IInstance> drTest) {
		DescriptiveStatistics stats = new DescriptiveStatistics();
		for (IInstance dyadRankingInstance : drTest) {
			IDyadRankingInstance drInstance = (IDyadRankingInstance) dyadRankingInstance;
			INDArray dyadMatrix = dyadRankingToMatrix(drInstance);
			INDArray outputs = plNet.output(dyadMatrix);
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
				// Gradient descent updater: SGD
				.updater(new Sgd(configuration.plNetLearningRate())).list();

		// Build hidden layers
		String activation = configuration.plNetActivationFunction();
		int inputsFirstHiddenLayer = configuration.plNetHiddenNodes().get(0);
		configBuilder.layer(0, new DenseLayer.Builder().nIn(numInputs).nOut(inputsFirstHiddenLayer)
				.weightInit(WeightInit.XAVIER).activation(Activation.fromString(activation)).hasBias(true).build());
		List<Integer> hiddenNodes = configuration.plNetHiddenNodes();

		for (int i = 0; i < hiddenNodes.size() - 1; i++) {
			int numIn = hiddenNodes.get(i);
			int numOut = hiddenNodes.get(i + 1);
			configBuilder.layer(i + 1, new DenseLayer.Builder().nIn(numIn).nOut(numOut).weightInit(WeightInit.XAVIER)
					.activation(Activation.fromString(activation)).hasBias(true).build());
		}

		// Build output layer. Since we are using an external error for training,
		// this is a regular layer instead of an OutputLayer
		configBuilder.layer(hiddenNodes.size(), new DenseLayer.Builder().nIn(hiddenNodes.get(hiddenNodes.size() - 1))
				.nOut(1).weightInit(WeightInit.XAVIER).activation(Activation.IDENTITY).hasBias(true).build());

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
			// normalize dyad vectors
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
	
	/**
	 * Pretty prints the PLNet's configuration.
	 */
	public void printConfig() {
		StringBuilder output = new StringBuilder();
		output.append("PLNet config:")
			  .append("\n\tLearning rate:\t\t\t").append(configuration.plNetLearningRate())
			  .append("\n\tHidden nodes:\t\t\t").append(configuration.plNetHiddenNodes())
			  .append("\n\tSeed:\t\t\t\t").append(configuration.plNetSeed())
			  .append("\n\tActivation function:\t\t").append(configuration.plNetActivationFunction())
			  .append("\n\tMax epochs:\t\t\t").append(configuration.plNetMaxEpochs())
			  .append("\n\tMini batch size:\t\t").append(configuration.plNetMiniBatchSize())
			  .append("\n\tEarly stopping interval:\t").append(configuration.plNetEarlyStoppingInterval())
			  .append("\n\tEarly stopping patience:\t").append(configuration.plNetEarlyStoppingPatience())
			  .append("\n\tEarly stopping train ratio:\t").append(configuration.plNetEarlyStoppingTrainRatio())
			  .append("\n\tEarly stopping retrain:\t\t").append(configuration.plNetEarlyStoppingRetrain())
			  .append("\n");
		System.out.print(output.toString());
	}
}
