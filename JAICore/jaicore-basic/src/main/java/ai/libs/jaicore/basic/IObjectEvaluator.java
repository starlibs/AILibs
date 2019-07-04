package ai.libs.jaicore.basic;

import ai.libs.jaicore.basic.algorithm.exceptions.ObjectEvaluationFailedException;

public interface IObjectEvaluator<T,V extends Comparable<V>> {
	public V evaluate(T object) throws InterruptedException, ObjectEvaluationFailedException;
}
