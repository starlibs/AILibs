package ai.libs.jaicore.timing;

import java.util.concurrent.ExecutionException;

import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.api4.java.common.attributedobjects.IObjectEvaluator;
import org.api4.java.common.attributedobjects.ObjectEvaluationFailedException;

public abstract class TimedObjectEvaluator<T, V extends Comparable<V>> implements IObjectEvaluator<T, V> {

	public abstract V evaluateSupervised(T item) throws InterruptedException, ObjectEvaluationFailedException;

	public abstract long getTimeout(T item);

	public abstract String getMessage(T item);

	@Override
	public final V evaluate(final T object) throws InterruptedException, ObjectEvaluationFailedException {
		try {
			return TimedComputation.compute(() -> this.evaluateSupervised(object), this.getTimeout(object), this.getMessage(object));
		} catch (InterruptedException e) { // re-throw interrupts
			assert !Thread.currentThread().isInterrupted() : "The interrupt-flag should not be true when an InterruptedException is thrown! Stack trace of the InterruptedException is \n\t";
			throw e;
		} catch (AlgorithmTimeoutedException e) {
			throw new ObjectEvaluationFailedException("Timed object evaluation failed", e);
		} catch (ExecutionException e) {
			if (e.getCause() instanceof ObjectEvaluationFailedException) {
				throw (ObjectEvaluationFailedException) e.getCause();
			}
			throw new ObjectEvaluationFailedException("Evaluation of composition failed as the component instantiation could not be built.", e.getCause());
		}
	}
}
