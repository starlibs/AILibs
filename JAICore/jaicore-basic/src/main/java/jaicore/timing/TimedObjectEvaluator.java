package jaicore.timing;

import java.util.concurrent.ExecutionException;

import jaicore.basic.IObjectEvaluator;
import jaicore.basic.algorithm.exceptions.AlgorithmTimeoutedException;
import jaicore.basic.algorithm.exceptions.ObjectEvaluationFailedException;

public abstract class TimedObjectEvaluator<T, V extends Comparable<V>> implements IObjectEvaluator<T, V> {

	public abstract V evaluateSupervised(T item) throws InterruptedException, ObjectEvaluationFailedException;

	public abstract long getTimeout(T item);

	public abstract String getMessage(T item);

	@Override
	public final V evaluate(T object) throws InterruptedException, ObjectEvaluationFailedException {
		try {
			return TimedComputation.compute(() -> evaluateSupervised(object), getTimeout(object), getMessage(object));
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
