package jaicore.basic.algorithm.exceptions;

import java.util.concurrent.TimeoutException;

@SuppressWarnings("serial")
public class DelayedTimeoutCheckException extends DelayedTerminationCheckException {
	private final TimeoutException exception;

	public DelayedTimeoutCheckException(TimeoutException exception, long delay) {
		super("Timeout was triggered with a delay of " + delay + "ms.", delay);
		this.exception = exception;
	}

	public TimeoutException getException() {
		return exception;
	}
}
