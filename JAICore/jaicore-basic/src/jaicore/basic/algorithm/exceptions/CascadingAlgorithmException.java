package jaicore.basic.algorithm.exceptions;

@SuppressWarnings("serial")
public class CascadingAlgorithmException extends AlgorithmException {
	private final Throwable cause;
	
	public CascadingAlgorithmException(Throwable cause, String message) {
		super(message);
		this.cause = cause;
	}

	public Throwable getCause() {
		return cause;
	}
}
