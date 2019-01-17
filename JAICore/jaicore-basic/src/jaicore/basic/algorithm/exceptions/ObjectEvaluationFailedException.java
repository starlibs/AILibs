package jaicore.basic.algorithm.exceptions;

@SuppressWarnings("serial")
public class ObjectEvaluationFailedException extends Exception {
	public ObjectEvaluationFailedException(Throwable cause, String message) {
		super(message, cause);
	}
}
