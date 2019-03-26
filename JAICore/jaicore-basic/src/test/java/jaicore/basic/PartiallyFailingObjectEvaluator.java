package jaicore.basic;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import jaicore.basic.algorithm.exceptions.AlgorithmTimeoutedException;
import jaicore.basic.algorithm.exceptions.ObjectEvaluationFailedException;

/**
 * This object evaluator simulates failures. It is possible to specify the runs for which a result (null) should be returned.
 * In the other cases, an exception is thrown.
 *
 * @author fmohr
 *
 * @param <T>
 * @param <V>
 */
public class PartiallyFailingObjectEvaluator<T, V extends Comparable<V>> implements IObjectEvaluator<T, V> {

	private final V valueToReturn;
	private final AtomicInteger numCalls = new AtomicInteger(0);
	private final Collection<Integer> successfullInvocations;

	public PartiallyFailingObjectEvaluator(final Collection<Integer> successfullInvocations, final V valueToReturn) {
		super();
		this.successfullInvocations = successfullInvocations;
		this.valueToReturn = valueToReturn;
	}


	@Override
	public V evaluate(final T object) throws AlgorithmTimeoutedException, InterruptedException, ObjectEvaluationFailedException {
		if (this.successfullInvocations.contains(this.numCalls.incrementAndGet())) {
			return this.valueToReturn;
		}
		throw new ObjectEvaluationFailedException(null, "Exception for test purposes.");
	}

}
