package ai.libs.jaicore.search.algorithms.standard.bestfirst.exceptions;

import org.api4.java.algorithm.exceptions.AlgorithmException;

@SuppressWarnings("serial")
public class NodeEvaluationException extends AlgorithmException {

	public NodeEvaluationException(final String message) {
		super(message);
	}

	public NodeEvaluationException(final Throwable cause, final String message) {
		super(cause, message);
	}
}
