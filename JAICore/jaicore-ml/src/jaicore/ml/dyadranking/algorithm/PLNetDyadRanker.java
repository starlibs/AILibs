package jaicore.ml.dyadranking.algorithm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.aeonbits.owner.ConfigFactory;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.gradient.DefaultGradient;
import org.deeplearning4j.nn.gradient.Gradient;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.nn.workspace.LayerWorkspaceMgr;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.primitives.Pair;

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
import jaicore.ml.core.predictivemodel.IPredictiveModelConfiguration;
import jaicore.ml.dyadranking.dataset.IDyadRankingInstance;

/**
 * A dyad ranker based on a Plackett-Luce network.
 * 
 * @author Helena Graf, Jonas Hanselle
 *
 */
public class PLNetDyadRanker extends APLDyadRanker implements IOnlineLearner<IDyadRankingInstance> {

	private MultiLayerNetwork plNet;
	private IPLNetDyadRankerConfiguration configuration;
	private int epoch;
	private int iteration;

	/**
	 * Constructor using a {@link IPLNetDyadRankerConfiguration} to create the
	 * {@link PLNetDyadRanker}.
	 * 
	 * @param configuration
	 */
	public PLNetDyadRanker(int numInputs, int numHiddenNodes, long seed) {
		this.configuration = ConfigFactory.create(IPLNetDyadRankerConfiguration.class);
		this.plNet = createNetwork(numInputs, numHiddenNodes, seed);
//		this.plNet = createNetworkFromConfigFile(configuration.plNetConfig());
	}

	@Override
	public void train(IDataset dataset) throws TrainingException {
		if (!(dataset instanceof DyadRankingDataset)) {
			throw new IllegalArgumentException(
					"Can only train the Plackett-Luce net dyad ranker with a dyad ranking dataset!");
		}
		DyadRankingDataset drDataset = (DyadRankingDataset) dataset;
		for (IInstance dyadRankingInstance : drDataset) {
			this.update(dyadRankingInstance);
		}
		epoch++;
	}

	@Override
	public void update(IInstance instance) throws TrainingException {
		if (!(instance instanceof IDyadRankingInstance)) {
			throw new IllegalArgumentException(
					"Can only update the Plackett-Luce net dyad ranker with a dyad ranking instance!");
		}

		DyadRankingInstance drInstance = (DyadRankingInstance) instance;
		// init weight update vector
		INDArray dyadMatrix;
		List<INDArray> dyadList = new ArrayList<INDArray>(drInstance.length());
		for (Dyad dyad : drInstance) {
			INDArray instanceOfDyad = Nd4j.create(dyad.getInstance().asArray());
			INDArray alternativeOfDyad = Nd4j.create(dyad.getAlternative().asArray());
			INDArray dyadVector = Nd4j.hstack(instanceOfDyad, alternativeOfDyad);
			dyadList.add(dyadVector);
		}
		dyadMatrix = Nd4j.vstack(dyadList);

		List<INDArray> activations = plNet.feedForward(dyadMatrix);
		INDArray output = activations.get(activations.size() - 1);
		System.out.println();
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
			deltaW.add(deltaWk.gradient());
		}
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
		DyadRankingInstance drInstance = (DyadRankingInstance) instance;
		List<Pair<Dyad, Double>> dyadUtilityPairs = new ArrayList<Pair<Dyad, Double>>(drInstance.length());
		for (Dyad dyad : drInstance) {
			INDArray instanceOfDyad = Nd4j.create(dyad.getInstance().asArray());
			INDArray alternativeOfDyad = Nd4j.create(dyad.getAlternative().asArray());
			INDArray plNetInput = Nd4j.hstack(instanceOfDyad, alternativeOfDyad);
			double plNetOutput = plNet.output(plNetInput).getDouble(0);
			dyadUtilityPairs.add(new Pair<Dyad, Double>(dyad, plNetOutput));
		}
		// sort the instance in descending order of utility values
		Collections.sort(dyadUtilityPairs, Comparator.comparing(p -> p.getRight()));
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
	 * @param numInputs
	 * @param numHiddenNodes
	 * @param seed
	 * @return New {@link MultiLayerNetwork}
	 */
	private MultiLayerNetwork createNetwork(int numInputs, int numHiddenNodes, long seed) {
		int numOutputs = 1;
		MultiLayerConfiguration config = new NeuralNetConfiguration.Builder().seed(seed).list()
				.layer(0,
						new DenseLayer.Builder().nIn(numInputs).nOut(numHiddenNodes).weightInit(WeightInit.XAVIER)
								.activation(Activation.SIGMOID).build())
				.layer(1, new DenseLayer.Builder().weightInit(WeightInit.XAVIER).activation(Activation.IDENTITY)
						.weightInit(WeightInit.XAVIER).nIn(numHiddenNodes).nOut(numOutputs).build())
				.build();

		return new MultiLayerNetwork(config);
	}

	/**
	 * Creates a simple feed-forward {@link MultiLayerNetwork} using the json
	 * representation of a {@link MultiLayerConfiguration} in the file .
	 * 
	 * @param configFile {@link File} containing the json representation of the
	 *                   {@link MultiLayerConfiguration}
	 * @return a new {@link MultiLayerNetwork} based on the
	 *         {@link MultiLayerConfiguration}
	 */
	private MultiLayerNetwork createNetworkFromConfigFile(File configFile) {
		String json = "";
		try {
			json = FileUtil.readFileAsString(configFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		MultiLayerConfiguration config = MultiLayerConfiguration.fromJson(json);
		MultiLayerNetwork network = new MultiLayerNetwork(config);

		return network;
	}
}
