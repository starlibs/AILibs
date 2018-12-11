package jaicore.ml.dyadranking.algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.gradient.Gradient;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.ILossFunction;
import org.nd4j.linalg.primitives.Pair;

import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.core.exception.TrainingException;
import jaicore.ml.dyadranking.Dyad;
import jaicore.ml.dyadranking.dataset.DyadRankingDataset;
import jaicore.ml.dyadranking.dataset.DyadRankingInstance;
import jaicore.ml.dyadranking.dataset.IDyadRankingInstance;

/**
 * A dyad ranker based on a Plackett-Luce network.
 * 
 * @author Helena Graf, Jonas Hanselle
 *
 */
public class PLNetDyadRanker extends APLDyadRanker {

	private MultiLayerNetwork plNet;
	private PLNetLoss plNetLoss;
	private double learningRate;
	
	/**
	 * 
	 * @param learningRate
	 */
	public PLNetDyadRanker(double learningRate, int numInputs, int numHiddenNodes, long seed) {
		this.learningRate = learningRate;
		this.plNetLoss = new PLNetLoss();
		this.plNet = this.createNetwork(numInputs, numHiddenNodes, seed, this.plNetLoss);
		this.plNetLoss.setPlNet(this.plNet);
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
	}

	public void update(IInstance instance) throws TrainingException {
		if (!(instance instanceof IDyadRankingInstance)) {
			throw new IllegalArgumentException(
					"Can only update the Plackett-Luce net dyad ranker with a dyad ranking instance!");
		}

		// TODO

		DyadRankingInstance drInstance = (DyadRankingInstance) instance;
		// init weight update vector
		INDArray deltaW = Nd4j.zeros(plNet.numParams());
		plNetLoss.preComputeOutputs(drInstance);
		for (int k = 0; k < drInstance.length(); k++) {
			plNetLoss.setK(k);
			// compute derivative of loss w.r.t. k
			INDArray currentOutputGradient = plNetLoss.computeGradient(null, null, null, null);
			// compute backprop gradient for weight updates w.r.t. k
			Pair<Gradient, INDArray> p = plNet.backpropGradient(currentOutputGradient, null);
			Gradient gradient = p.getFirst();
			plNet.getUpdater().update(plNet, gradient, 0, 0, 0, null);
			INDArray deltaWk = gradient.gradient();
			// add it
			deltaW.add(deltaWk);
		}
		//
		INDArray update = deltaW.mul(this.learningRate);
		plNet.params().subi(update);
	}

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

	/**
	 * Creates a simple feed-forward network that can be used as a PLNet for
	 * dyad-ranking.
	 * 
	 * @param numInputs
	 * @param numHiddenNodes
	 * @param seed
	 * @return
	 */
	private MultiLayerNetwork createNetwork(int numInputs, int numHiddenNodes, long seed, ILossFunction lossFunction) {
		int numOutputs = 1;
		MultiLayerConfiguration config = new NeuralNetConfiguration.Builder().seed(seed).list()
				.layer(0,
						new DenseLayer.Builder().nIn(numInputs).nOut(numHiddenNodes).weightInit(WeightInit.XAVIER)
								.activation(Activation.SIGMOID).build())
				.layer(1,
						new OutputLayer.Builder().lossFunction(lossFunction).weightInit(WeightInit.XAVIER)
								.activation(Activation.IDENTITY).weightInit(WeightInit.XAVIER).nIn(numHiddenNodes)
								.nOut(numOutputs).build())
				.build();

		return new MultiLayerNetwork(config);
	}

}
