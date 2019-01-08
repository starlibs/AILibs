package jaicore.basic;

import java.util.concurrent.TimeoutException;

import jaicore.basic.algorithm.exceptions.ObjectEvaluationFailedException;

public interface IObjectEvaluator<T,V extends Comparable<V>> {
	public V evaluate(T object) throws TimeoutException, InterruptedException, ObjectEvaluationFailedException;
}
