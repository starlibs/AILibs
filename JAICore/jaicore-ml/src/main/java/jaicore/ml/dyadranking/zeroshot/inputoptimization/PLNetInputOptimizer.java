package jaicore.ml.dyadranking.zeroshot.inputoptimization;

import org.deeplearning4j.nn.gradient.Gradient;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.BooleanIndexing;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.nd4j.linalg.indexing.conditions.Conditions;
import org.nd4j.linalg.primitives.Pair;

import jaicore.ml.dyadranking.algorithm.PLNetDyadRanker;
import jaicore.ml.dyadranking.zeroshot.util.InputOptListener;

/**
 * Optimizes a given loss function ({@link InputOptimizerLoss}) with respect to the input of a PLNet using gradient descent.
 * Assumes the PLNet was trained on normalized training data (i.e. scaled to intervals of 0 to 1 using {@link DyadMinMaxScaler})
 * and ensures that the optimized inputs will be within this range.
 * 
 * @author Michael Braun
 *
 */
public class PLNetInputOptimizer {

	private InputOptListener listener;

	
	/**
	 * Optimizes the given loss function with respect to a given PLNet's inputs using gradient descent. Ensures the outcome will be within the range of 0 and 1.
	 * Performs gradient descent for a given number of steps starting at a given input, using a static learning rate.
	 * The inputs that should be optimized can be specified using an index range in the form of a {@link Pair}} of integers.
	 * @param plNet					PLNet whose inputs to optimize.
	 * @param input					Initial inputs to start the gradient descent procedure from.
	 * @param loss					The loss to be minimized.
	 * @param learningRate			The initial learning rate.
	 * @param numSteps				The number of steps to perform gradient descent for.
	 * @param indexRange			Pair of indices (inclusive) specifying the parts of the input that should be optimized.
	 * @return						The input optimized with respect to the given loss.
	 */
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
	
	/**
	 * Optimizes the given loss function with respect to a given PLNet's inputs using gradient descent. Ensures the outcome will be within the range of 0 and 1.
	 * Performs gradient descent for a given number of steps starting at a given input, using a linearly decaying learning rate.
	 * The inputs that should be optimized can be specified using an index range in the form of a {@link Pair}} of integers.
	 * @param plNet					PLNet whose inputs to optimize.
	 * @param input					Initial inputs to start the gradient descent procedure from.
	 * @param loss					The loss to be minimized.
	 * @param initialLearningRate	The initial learning rate.
	 * @param finalLearningRate		The value the learning rate should decay to.
	 * @param numSteps				The number of steps to perform gradient descent for.
	 * @param indexRange			Pair of indices (inclusive) specifying the parts of the input that should be optimized.
	 * @return						The input optimized with respect to the given loss.
	 */
	public INDArray optimizeInput(PLNetDyadRanker plNet, INDArray input, InputOptimizerLoss loss, double initialLearningRate, double finalLearningRate, int numSteps, Pair<Integer, Integer> indexRange) {
		INDArray mask;
		if (indexRange != null) {
			mask = Nd4j.zeros(input.length());
			mask.get(NDArrayIndex.interval(indexRange.getFirst(), indexRange.getSecond())).assign(1.0);
		} else {
			mask = Nd4j.ones(input.length());
		}

		return optimizeInput(plNet, input, loss, initialLearningRate, finalLearningRate, numSteps, mask);
	}
	
	/**
	 * Optimizes the given loss function with respect to a given PLNet's inputs using gradient descent. Ensures the outcome will be within the range of 0 and 1.
	 * Performs gradient descent for a given number of steps starting at a given input, using a static learning rate.
	 * The inputs that should be optimized can be specified using a 0,1-vector
	 * @param plNet					PLNet whose inputs to optimize.
	 * @param input					Initial inputs to start the gradient descent procedure from.
	 * @param loss					The loss to be minimized.
	 * @param learningRate			The initial learning rate.
	 * @param numSteps				The number of steps to perform gradient descent for.
	 * @param inputMask				0,1 vector specifying the inputs to optimize, i.e. should have a 1 at the index of any input that should be optimized and a 0 elsewhere.
	 * @return						The input optimized with respect to the given loss.
	 */
	public INDArray optimizeInput(PLNetDyadRanker plNet, INDArray input, InputOptimizerLoss loss, double learningRate, int numSteps, INDArray inputMask) {
		return optimizeInput(plNet, input, loss, learningRate, learningRate, numSteps, inputMask);
	}
	
	/**
	 * Optimizes the given loss function with respect to a given PLNet's inputs using gradient descent. Ensures the outcome will be within the range of 0 and 1.
	 * Performs gradient descent for a given number of steps starting at a given input, using a linearly decaying learning rate.
	 * The inputs that should be optimized can be specified using a 0,1-vector
	 * @param plNet					PLNet whose inputs to optimize.
	 * @param input					Initial inputs to start the gradient descent procedure from.
	 * @param loss					The loss to be minimized.
	 * @param initialLearningRate	The initial learning rate.
	 * @param finalLearningRate		The value the learning rate should decay to.
	 * @param numSteps				The number of steps to perform gradient descent for.
	 * @param inputMask				0,1 vector specifying the inputs to optimize, i.e. should have a 1 at the index of any input that should be optimized and a 0 elsewhere.
	 * @return						The input optimized with respect to the given loss.
	 */
	public INDArray optimizeInput(PLNetDyadRanker plNet, INDArray input, InputOptimizerLoss loss, double initialLearningRate, double finalLearningRate, int numSteps,
			INDArray inputMask) {
		INDArray inp = input.dup();
		INDArray alphas = Nd4j.zeros(inp.shape());
		INDArray betas = Nd4j.zeros(inp.shape());
		INDArray ones = Nd4j.ones(inp.shape());
		double output = plNet.getPlNet().output(inp).getDouble(0);
		double incumbentOutput = output;
		INDArray incumbent = inp.dup();
		for (int i = 0; i < numSteps; i++) {
			double lrDecayTerm = (double) i / (double) numSteps;
			double learningRate = (1 - lrDecayTerm) * initialLearningRate + lrDecayTerm * finalLearningRate;
			// Gradient of PLNet
			INDArray grad = computeInputDerivative(plNet, inp, loss);
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
			if (listener != null) {
				listener.reportOptimizationStep(inp, output);
			}

			INDArray incCheck = inp.dup().muli(inputMask);
			if (output > incumbentOutput && BooleanIndexing.and(incCheck, Conditions.greaterThanOrEqual(0.0d)) && BooleanIndexing.and(incCheck, Conditions.lessThanOrEqual(1.0d))) {
				incumbent = inp.dup();
				incumbentOutput = output;
			}
		}

		return incumbent;
	}

	private static INDArray computeInputDerivative(PLNetDyadRanker plNet, INDArray input, InputOptimizerLoss loss) {
		MultiLayerNetwork net = plNet.getPlNet();

		INDArray output = net.output(input);
		INDArray lossGradient = Nd4j.create(new double[] { loss.lossGradient(output) });
		net.setInput(input);
		net.feedForward(false, false);
		Pair<Gradient, INDArray> p = net.backpropGradient(lossGradient, null);

		return p.getSecond();
	}
	
	/**
	 * Set an {@link InputOptListener} to record the intermediate steps of the optimization procedure.
	 * @param listener
	 */
	public void setListener(InputOptListener listener) {
		this.listener = listener;
	}
}
