package jaicore.ml.dyadranking.inputoptimization;

import org.deeplearning4j.nn.gradient.Gradient;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.primitives.Pair;

import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.exception.TrainingException;
import jaicore.ml.dyadranking.algorithm.PLNetDyadRanker;
import jaicore.ml.dyadranking.general.DyadRankingInstanceSupplier;

public class PLNetInputOptimizer {
	
	private PLNetDyadRanker plNet;
	
	public PLNetInputOptimizer(PLNetDyadRanker plNet) {
		super();
		this.plNet = plNet;
	}

	public INDArray computeInputDerivative(INDArray input) {
		MultiLayerNetwork net = plNet.getPlNet();

		INDArray output = net.output(input);
		net.setInput(input);
		net.feedForward(false, false);
		Pair<Gradient, INDArray> p = net.backpropGradient(output, null);
		INDArray grad = p.getSecond();
		
		return grad;
	}
	
	public static void main(String... args) throws TrainingException {
		PLNetDyadRanker testnet = new PLNetDyadRanker();
		IDataset train = DyadRankingInstanceSupplier.getInputOptTestSet(5, 200);
		testnet.train(train);
		PLNetInputOptimizer inpopt = new PLNetInputOptimizer(testnet);
		MultiLayerNetwork net = testnet.getPlNet();
		INDArray testinp = Nd4j.ones(net.layerInputSize(0));
		INDArray grad = inpopt.computeInputDerivative(testinp);
		for(int i = 0; i < 20; i++) {
			grad.muli(0.1);
			testinp.subi(grad);
			testinp = inpopt.computeInputDerivative(testinp);
			System.out.println(testinp);
		}
	}
	
}
