package ai.libs.jaicore.ml.core.evaluation.evaluator.splitevaluation;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.evaluation.loss.ILossFunction;

/**
 * Connection between an Evaluator (e.g. MCC) and a loss Function. Able to evaluate instances based on training data and validation data.
 * This bridge may modify this process, for example by using a cache.
 *
 * @author mirko, mwever
 *
 * @param <I> the input type
 * @param <V> the output type
 */
public abstract class AbstractSplitBasedClassifierEvaluator<V, I extends ILabeledInstance, D extends ILabeledDataset<I>> implements ISplitBasedClassifierEvaluator<Double, I, D> {

	private ILossFunction<V> basicEvaluator;

	public AbstractSplitBasedClassifierEvaluator(final ILossFunction<V> basicEvaluator) {
		this.basicEvaluator = basicEvaluator;
	}

	/**
	 * @return The basic evaluator that is currently used in this measure bridge.
	 */
	public ILossFunction<V> getBasicEvaluator() {
		return this.basicEvaluator;
	}

	/**
	 * @param basicEvaluator The new basic evaluator to be used in this measure bridge.
	 */
	public void setBasicEvaluator(final ILossFunction<V> basicEvaluator) {
		this.basicEvaluator = basicEvaluator;
	}

}
