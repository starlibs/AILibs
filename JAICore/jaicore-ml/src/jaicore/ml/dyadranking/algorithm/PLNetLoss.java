package jaicore.ml.dyadranking.algorithm;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.activations.IActivation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.nd4j.linalg.lossfunctions.ILossFunction;
import org.nd4j.linalg.ops.transforms.Transforms;
import org.nd4j.linalg.primitives.Pair;

import jaicore.ml.dyadranking.Dyad;
import jaicore.ml.dyadranking.dataset.IDyadRankingInstance;

/**
 * Implements the negative log likelihood loss function for PL networks as described in [1].
 * 
 * WARNING: Contains heavy misuse of interfaces since the NLL loss for PLNet is not defined on single inputs and outputs of 
 * a neural network. Should be used with extreme caution outside of the context of PLNet.
 * 
 * [1]: Dirk Schäfer, Eyke Hüllermeier (2018). Dyad ranking using Plackett-Luce models based on joint feature representations
 * 
 * @author michael
 *
 */

public class PLNetLoss implements ILossFunction {
	
	private INDArray plNetOutputs;
	// The index of the dyad w.r.t. which to compute the gradient. Assumed to run from 1 to M_n
	private int k;
	private MultiLayerNetwork plNet;
	
	public void preComputeOutputs(IDyadRankingInstance dyadRanking) {
		int numDyads = dyadRanking.length();
		double[] outputs = new double[numDyads];
		int i = 0;
		for (Dyad dyad : dyadRanking) {
			INDArray instance = Nd4j.create(dyad.getInstance().asArray());
			INDArray alternative = Nd4j.create(dyad.getAlternative().asArray());
			INDArray plNetInput = Nd4j.hstack(instance, alternative);
			outputs[i] = plNet.output(plNetInput).getDouble(0);
			i++;
		}
		plNetOutputs = Nd4j.create(outputs);
	}
	
	/**
	 * Computes negative log likelihood based on pre-computed outputs. Ignores actual inputs.
	 */
	@Override
	public double computeScore(INDArray labels, INDArray preOutput, IActivation activationFn, INDArray mask,
			boolean average) {
		double score = 0;
		long dyadRankingLength = plNetOutputs.size(1);
		for (int m = 0; m <= dyadRankingLength - 2; m++) {
			INDArray innerSumSlice = plNetOutputs.get(NDArrayIndex.interval(m, dyadRankingLength));
			innerSumSlice = Transforms.exp(innerSumSlice);
			score += Transforms.log(innerSumSlice.sum(1)).getDouble(0);
		}
		score -= plNetOutputs.get(NDArrayIndex.interval(0, dyadRankingLength - 1)).sum(0).getDouble(0);
		return score;
	}

	@Override
	public INDArray computeScoreArray(INDArray labels, INDArray preOutput, IActivation activationFn, INDArray mask) {
        return Nd4j.create(new double[] {computeScore(labels, preOutput, activationFn, mask, false)});
	}
	
	/**
	 * Computes the gradient of the negative log likelihood of the pre-computed outputs w.r.t. the k-th output 
	 * as set by {@link #setK(int)}. Ignores actual inputs.
	 * 
	 * @return
	 */
	@Override
	public INDArray computeGradient(INDArray labels, INDArray preOutput, IActivation activationFn, INDArray mask) {
		long dyadRankingLength = plNetOutputs.size(1);
		double errorGradient = 0;
		for (int m = 0; m <= k - 1; m++) {
			INDArray innerSumSlice = plNetOutputs.get(NDArrayIndex.interval(m, dyadRankingLength));
			innerSumSlice = Transforms.exp(innerSumSlice);
			double innerSum = innerSumSlice.sum(1).getDouble(0);
			errorGradient += Math.exp(plNetOutputs.getDouble(k - 1)) / innerSum;
		}
		errorGradient -= 1;
		return Nd4j.create(new double[] {errorGradient});
	}

	@Override
	public Pair<Double, INDArray> computeGradientAndScore(INDArray labels, INDArray preOutput, IActivation activationFn,
			INDArray mask, boolean average) {
		return new Pair<>(
                computeScore(labels, preOutput, activationFn, mask, average),
                computeGradient(labels, preOutput, activationFn, mask));
	}

	@Override
	public String name() {
		return "PLNetLoss";
	}

	public int getK() {
		return k;
	}
	/**
	 * Set the index of the dyad w.r.t. which to compute the gradient. Assumed to run from 1 to M_n.
	 * @param k
	 */
	public void setK(int k) {
		this.k = k;
	}

	public MultiLayerNetwork getPlNet() {
		return plNet;
	}

	public void setPlNet(MultiLayerNetwork plNet) {
		this.plNet = plNet;
	}

}
