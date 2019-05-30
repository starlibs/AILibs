package jaicore.basic.algorithm.exceptions;

@SuppressWarnings("serial")
public class ObjectEvaluationFailedException extends Exception {
	
	public ObjectEvaluationFailedException(String message) {
		super(message);
	}
	
	public ObjectEvaluationFailedException(String message, Throwable cause) {
		super(message, cause);
	}
}
