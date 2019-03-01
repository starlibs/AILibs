package jaicore.basic.algorithm.exceptions;

import java.util.concurrent.TimeoutException;

@SuppressWarnings("serial")
public class AlgorithmTimeoutedException extends TimeoutException {
	private final long delay;

	public AlgorithmTimeoutedException(long delay) {
		super("Timeout was triggered with a delay of " + delay + "ms.");
		this.delay = delay;
	}

	public long getDelay() {
		return delay;
	}
}
