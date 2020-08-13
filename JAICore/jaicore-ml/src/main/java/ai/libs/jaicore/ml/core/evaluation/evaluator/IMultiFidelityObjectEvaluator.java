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

	public double getMaxBudget();

	public double getMinBudget();

	public V evaluate(T t, double budget) throws InterruptedException, ObjectEvaluationFailedException;

}
