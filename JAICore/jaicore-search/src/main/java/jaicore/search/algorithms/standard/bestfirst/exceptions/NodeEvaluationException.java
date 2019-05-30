package jaicore.search.algorithms.standard.bestfirst.exceptions;

import jaicore.basic.algorithm.exceptions.AlgorithmException;

@SuppressWarnings("serial")
public class NodeEvaluationException extends AlgorithmException {
	
	public NodeEvaluationException(String message) {
		super(message);
	}
	
	public NodeEvaluationException(Throwable cause, String message) {
		super(cause, message);
	}
}
