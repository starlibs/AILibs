package ai.libs.jaicore.ml.core.evaluation.evaluator;

import org.api4.java.common.attributedobjects.IObjectEvaluator;
import org.api4.java.common.attributedobjects.ObjectEvaluationFailedException;

/**
 * A multi-fidelity object evaluator allows for specifying a certain amount of an evaluation resource.
 *
 * @author mwever
 *
 * @param <T> The type of object to be evaluated.
 * @param <V> The comparable evaluation value.
 */
public interface IMultiFidelityObjectEvaluator<T, V extends Comparable<V>> extends IObjectEvaluator<T, V> {

	/**
	 * @return The maximum allocable budget.
	 */
	public double getMaxBudget();

	/**
	 * @return The minimum allocable budget.
	 */
	public double getMinBudget();

	/**
	 * Evaluate the object t with the specified budget.
	 *
	 * @param t The object to be evaluated.
	 * @param budget The budget assigned for the evaluation of t.
	 * @return The evaluation score for the object t.
	 * @throws InterruptedException Thrown if the evaluation routine is interrupted.
	 * @throws ObjectEvaluationFailedException Thrown, if the object t cannot be successfully evaluated.
	 */
	public V evaluate(T t, double budget) throws InterruptedException, ObjectEvaluationFailedException;

	@Override
	default V evaluate(final T t) throws InterruptedException, ObjectEvaluationFailedException {
		// Whenever no budget is specified, evaluated with the maximum budget.
		return this.evaluate(t, this.getMaxBudget());
	}

	/**
	 * Evaluate the object t with minimal resources.
	 * @param t The object to be evaluated.
	 * @return The evaluation score for the object t.
	 * @throws InterruptedException Thrown, if the evaluation routine is interrupted.
	 * @throws ObjectEvaluationFailedException Thrown, if the object t cannot be successfully evaluated.
	 */
	default V evaluateMinimal(final T t) throws InterruptedException, ObjectEvaluationFailedException {
		return this.evaluate(t, this.getMinBudget());
	}

	/**
	 * Evaluate the object t with maximal resources.
	 * @param t The object to be evaluated.
	 * @return The evaluation score for the object t.
	 * @throws InterruptedException Thrown, if the evaluation routine is interrupted.
	 * @throws ObjectEvaluationFailedException Thrown, if the object t cannot be successfully evaluated.
	 */
	default V evaluateMaximal(final T t) throws InterruptedException, ObjectEvaluationFailedException {
		return this.evaluate(t, this.getMaxBudget());
	}
}
