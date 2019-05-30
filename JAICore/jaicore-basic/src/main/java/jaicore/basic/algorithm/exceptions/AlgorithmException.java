package jaicore.basic.algorithm.exceptions;

@SuppressWarnings("serial")
public class AlgorithmException extends Exception {
	public AlgorithmException(String message) {
		super(message);
	}
	
	public AlgorithmException(Throwable cause, String message) {
		super(message, cause);
	}
}
