package jaicore.basic.algorithm.exceptions;

import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;

@SuppressWarnings("serial")
public class DelayedCancellationCheckException extends DelayedTerminationCheckException {
	private final AlgorithmExecutionCanceledException exception;

	public DelayedCancellationCheckException(AlgorithmExecutionCanceledException exception, long delay) {
		super("Cancellation was triggered with a delay of " + delay, delay);
		this.exception = exception;
	}

	public AlgorithmExecutionCanceledException getException() {
		return exception;
	}
}
