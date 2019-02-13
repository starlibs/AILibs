package jaicore.basic;

import java.util.concurrent.TimeoutException;

import jaicore.basic.algorithm.exceptions.ObjectEvaluationFailedException;

/**
 * This is a universal object evaluator that will never return a value in reasonable time but is interruptible.
 * The class serves for testing purposes.
 * 
 * @author fmohr
 *
 * @param <T>
 * @param <V>
 */
public class BusyObjectEvaluator<T, V extends Comparable<V>> implements IObjectEvaluator<T, V> {

	@Override
	public V evaluate(T object) throws TimeoutException, InterruptedException, ObjectEvaluationFailedException {
		int x = 0;
		for (int i = 0; i < 1.0E15; i++) {
			for (int j = 0; j < i; j++) {
				if (Thread.currentThread().isInterrupted())
					throw new InterruptedException("Busy evaluator has been interrupted!");
				x += i * j;
			}
		}
		return x > 0 ? null : null;
	}

}
