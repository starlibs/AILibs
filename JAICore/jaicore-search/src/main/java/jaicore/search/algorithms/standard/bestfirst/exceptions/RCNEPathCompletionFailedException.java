package jaicore.search.algorithms.standard.bestfirst.exceptions;

public class RCNEPathCompletionFailedException extends Exception {
	public RCNEPathCompletionFailedException(Exception e) {
		super(e);
	}
	
	public RCNEPathCompletionFailedException(String message) {
		super(message);
	}
	
	public RCNEPathCompletionFailedException(String message, Exception e) {
		super(message, e);
	}
}
