package jaicore.ml.dyadranking.zeroshot.inputoptimization;

import org.deeplearning4j.nn.gradient.Gradient;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.BooleanIndexing;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.nd4j.linalg.indexing.conditions.Conditions;
import org.nd4j.linalg.primitives.Pair;

import de.upb.isys.linearalgebra.DenseDoubleVector;
import de.upb.isys.linearalgebra.Vector;
import jaicore.ml.dyadranking.Dyad;
import jaicore.ml.dyadranking.algorithm.PLNetDyadRanker;
import jaicore.ml.dyadranking.zeroshot.util.InputOptListener;

public class PLNetInputOptimizer {
	
	private InputOptListener listener;
	
	public INDArray optimizeInput(PLNetDyadRanker plNet, INDArray input, InputOptimizerLoss loss, double learningRate, int numSteps, Pair<Integer, Integer> indexRange) {
		INDArray mask;
		if (indexRange != null) {
			mask = Nd4j.zeros(input.length());
			mask.get(NDArrayIndex.interval(indexRange.getFirst(), indexRange.getSecond())).assign(1.0);
		} else {
			mask = Nd4j.ones(input.length());
		}
		
		return optimizeInput(plNet, input, loss, learningRate, numSteps, mask);
	}
	
	public INDArray optimizeInput(PLNetDyadRanker plNet, INDArray input, InputOptimizerLoss loss, double learningRate, int numSteps, INDArray inputMask) {
		INDArray inp = input.dup();
		INDArray alphas = Nd4j.zeros(inp.shape());
		INDArray betas = Nd4j.zeros(inp.shape());
		INDArray ones = Nd4j.ones(inp.shape());
		double lambda = 0.0;
		//System.out.println(inp);
		double output = plNet.getPlNet().output(inp).getDouble(0);
		double incumbentOutput = output;
		INDArray incumbent = inp.dup();
		//System.out.println("PLNet output: " + output + " ");
		for(int i = 0; i < numSteps; i++) {
			// Gradient of PLNet
			INDArray grad = computeInputDerivative(plNet, inp, loss);
			// Gradient of L2 norm
			INDArray l2grad = inp.dup().muli(2);
			l2grad.muli(lambda);
			grad.addi(l2grad);
			// Gradient of KKT term
			grad.subi(alphas);
			grad.addi(betas);
			// Apply gradient to alphas and betas
			alphas.subi(inp);
			betas.addi(inp.sub(ones));
			BooleanIndexing.replaceWhere(alphas, 0.0d, Conditions.lessThan(0.0d));
			BooleanIndexing.replaceWhere(betas, 0.0d, Conditions.lessThan(0.0d));
			grad.muli(inputMask);
			grad.muli(learningRate);
			inp.subi(grad);
			
			output = plNet.getPlNet().output(inp).getDouble(0);
			//System.out.print("inps: " + inp.getDouble(input.length() - 2) + ", " + inp.getDouble(input.length() - 1));
			//System.out.print("  alphas: " + alphas.getDouble(input.length() - 2) + ", " + alphas.getDouble(input.length() - 1));
			//System.out.println("  betas: " + betas.getDouble(input.length() - 2) + ", " + betas.getDouble(input.length() - 1));
			//System.out.println("PLNet output: " + output + " ");
			if (listener != null) {
				listener.reportOptimizationStep(inp, output);
			}
			if (output > incumbentOutput) {
				incumbent = inp.dup();
				incumbentOutput = output;
				//System.out.println("Found new incumbent.");
			}
		}
		
		return incumbent;
	}

	private static INDArray computeInputDerivative(PLNetDyadRanker plNet, INDArray input, InputOptimizerLoss loss) {
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
	
	public void setListener(InputOptListener listener) {
		this.listener = listener;
	}
}
