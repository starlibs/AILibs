package ai.libs.jaicore.basic;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import org.api4.java.common.attributedobjects.IObjectEvaluator;
import org.api4.java.common.attributedobjects.ObjectEvaluationFailedException;

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
	public V evaluate(final T object) throws InterruptedException, ObjectEvaluationFailedException {
		if (this.successfullInvocations.contains(this.numCalls.incrementAndGet())) {
			return this.valueToReturn;
		}
		throw new ObjectEvaluationFailedException("Intentional Exception. Call number " + this.numCalls.get() + " is not among set of successful invocations " + this.successfullInvocations, null);
	}

}
