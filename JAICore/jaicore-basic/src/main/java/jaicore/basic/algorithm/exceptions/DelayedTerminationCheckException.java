package jaicore.basic.algorithm.exceptions;

/**
 * The purpose of this exception is to indicate that the checkTermination method
 * of AAlgorithm was invoked too late. Too late means that the algorithm has
 * been interrupted, timeouted or canceled at least 100ms prior to the
 * invocation of the check.
 *
 * The motivation is that it is difficult to track time leaks, i.e. which code
 * is responsible that the control is not returned in time after an interruption
 * or timeout. This exception is thrown by checkTermination in order to enforce
 * developers to handle this particular case and to ease debugging.
 *
 * @author fmohr
 *
 */
@SuppressWarnings("serial")
public abstract class DelayedTerminationCheckException extends Exception {

	private final long delay;

	public DelayedTerminationCheckException(final String message, final long delay) {
		super(message);
		this.delay = delay;
	}

	public long getDelay() {
		return this.delay;
	}

}
