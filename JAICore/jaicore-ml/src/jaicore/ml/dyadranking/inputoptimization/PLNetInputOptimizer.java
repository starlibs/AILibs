package jaicore.ml.dyadranking.inputoptimization;

import org.deeplearning4j.nn.gradient.Gradient;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.nd4j.linalg.primitives.Pair;

import de.upb.isys.linearalgebra.DenseDoubleVector;
import de.upb.isys.linearalgebra.Vector;
import jaicore.ml.dyadranking.Dyad;
import jaicore.ml.dyadranking.algorithm.PLNetDyadRanker;
import jaicore.ml.dyadranking.general.DyadRankingInstanceSupplier;

public class PLNetInputOptimizer {
	
	private PLNetDyadRanker plNet;
	
	public PLNetInputOptimizer(PLNetDyadRanker plNet) {
		super();
		this.plNet = plNet;
	}
	
	public INDArray optimizeInput(INDArray input, InputOptimizerLoss loss, double learningRate, int numSteps, Pair<Integer, Integer> indexRange) {
		INDArray mask;
		if (indexRange != null) {
			mask = Nd4j.zeros(input.length());
			mask.get(NDArrayIndex.interval(indexRange.getFirst(), indexRange.getSecond())).assign(1.0);
		} else {
			mask = Nd4j.ones(input.length());
		}
		
		return optimizeInput(input, loss, learningRate, numSteps, mask);
	}
	
	public INDArray optimizeInput(INDArray input, InputOptimizerLoss loss, double learningRate, int numSteps, INDArray inputMask) {
		INDArray inp = input.dup();
		System.out.print(inp);
		Dyad testinpDyad = ndArrayToDyad(inp, 2, 2);
		System.out.print("PLNet output: " + plNet.getPlNet().output(inp) + " ");
		System.out.println(" input score: " + DyadRankingInstanceSupplier.inputOptimizerTestScore(testinpDyad));
		for(int i = 0; i < numSteps; i++) {
			INDArray grad = computeInputDerivative(inp, loss);
			grad.muli(inputMask);
			System.out.println("Gradient: " + grad);
			grad.muli(learningRate);
			inp.subi(grad);
			System.out.print(inp);
			System.out.print("PLNet output: " + plNet.getPlNet().output(inp) + " ");
			testinpDyad = ndArrayToDyad(inp, 2, 2);
			System.out.println(" input score: " + DyadRankingInstanceSupplier.inputOptimizerTestScore(testinpDyad));
		}
		
		return inp;
	}

	private INDArray computeInputDerivative(INDArray input, InputOptimizerLoss loss) {
		MultiLayerNetwork net = plNet.getPlNet();
		
		INDArray output = net.output(input);
		INDArray lossGradient = Nd4j.create(new double[] {loss.lossGradient(output)});
		net.setInput(input);
		net.feedForward(false, false);
		Pair<Gradient, INDArray> p = net.backpropGradient(lossGradient, null);
		INDArray grad = p.getSecond();
		
		return grad;
	}
	
	private static Dyad ndArrayToDyad(INDArray arr, int instSize, int altSize) {
		INDArray instSlice = arr.get(NDArrayIndex.interval(0, instSize));
		INDArray altSlice = arr.get(NDArrayIndex.interval(instSize, instSize + altSize));
		Vector instVector = new DenseDoubleVector(instSlice.toDoubleVector());
		Vector altVector = new DenseDoubleVector(altSlice.toDoubleVector());
		
		return new Dyad(instVector, altVector);
	}
}
