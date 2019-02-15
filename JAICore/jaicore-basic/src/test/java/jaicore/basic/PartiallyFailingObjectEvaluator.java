package jaicore.basic;

import java.util.Collection;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

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
	
	public PartiallyFailingObjectEvaluator(Collection<Integer> successfullInvocations, V valueToReturn) {
		super();
		this.successfullInvocations = successfullInvocations;
		this.valueToReturn = valueToReturn;
	}


	@Override
	public V evaluate(T object) throws TimeoutException, InterruptedException, ObjectEvaluationFailedException {
		System.out.println((numCalls.get() + 1) + "/" + successfullInvocations);
		if (successfullInvocations.contains(numCalls.incrementAndGet()))
			return valueToReturn;
		throw new ObjectEvaluationFailedException(null, "Exception for test purposes.");
	}

}
