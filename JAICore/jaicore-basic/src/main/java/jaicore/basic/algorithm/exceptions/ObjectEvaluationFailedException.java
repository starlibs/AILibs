package jaicore.basic.algorithm.exceptions;

@SuppressWarnings("serial")
public class ObjectEvaluationFailedException extends Exception {
	
	public ObjectEvaluationFailedException(String message) {
		super(message);
	}
	
	public ObjectEvaluationFailedException(Throwable cause, String message) {
		super(message, cause);
	}
}
