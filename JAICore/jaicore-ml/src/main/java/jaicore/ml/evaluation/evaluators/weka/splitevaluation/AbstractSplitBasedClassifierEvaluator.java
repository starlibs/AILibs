package jaicore.ml.evaluation.evaluators.weka.splitevaluation;

import jaicore.ml.core.evaluation.measure.IMeasure;

/**
 * Connection between an Evaluator (e.g. MCC) and a loss Function. Able to evaluate instances based on training data and validation data.
 * This bridge may modify this process, for example by using a cache.
 *
 * @author mirko, mwever
 *
 * @param <I> the input type
 * @param <O> the output type
 */
public abstract class AbstractSplitBasedClassifierEvaluator<I, O> implements ISplitBasedClassifierEvaluator<O> {

	private IMeasure<I, O> basicEvaluator;

	public AbstractSplitBasedClassifierEvaluator(final IMeasure<I, O> basicEvaluator) {
		this.basicEvaluator = basicEvaluator;
	}

	/**
	 * @return The basic evaluator that is currently used in this measure bridge.
	 */
	public IMeasure<I, O> getBasicEvaluator() {
		return this.basicEvaluator;
	}

	/**
	 * @param basicEvaluator The new basic evaluator to be used in this measure bridge.
	 */
	public void setBasicEvaluator(final IMeasure<I, O> basicEvaluator) {
		this.basicEvaluator = basicEvaluator;
	}

}
