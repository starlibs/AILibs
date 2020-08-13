package ai.libs.jaicore.ml.core.evaluation.evaluator;

import org.api4.java.common.attributedobjects.IObjectEvaluator;
import org.api4.java.common.attributedobjects.ObjectEvaluationFailedException;

public interface IMultiFidelityObjectEvaluator<T, V extends Comparable<V>> extends IObjectEvaluator<T, V> {

	public double getMaxBudget();

	public double getMinBudget();

	public V evaluate(T t, double budget) throws InterruptedException, ObjectEvaluationFailedException;

}
