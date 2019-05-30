package jaicore.basic;

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

	private int x = 0;

	@Override
	public V evaluate(final T object) throws InterruptedException, ObjectEvaluationFailedException {
		for (int i = 0; i < 1.0E15; i++) {
			for (int j = 0; j < i; j++) {
				if (Thread.interrupted()) { // reset interrupted flag since we throw an exception now
					throw new InterruptedException("Busy evaluator has been interrupted!");
				}
				this.x += i * j;
			}
		}
		return null;
	}

	public int getX() {
		return this.x;
	}
}
