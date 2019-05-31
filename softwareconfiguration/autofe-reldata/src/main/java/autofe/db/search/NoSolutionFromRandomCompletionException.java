package autofe.db.search;

import ai.libs.jaicore.search.algorithms.standard.bestfirst.exceptions.NodeEvaluationException;

public class NoSolutionFromRandomCompletionException extends NodeEvaluationException {

	/**
	 *
	 */
	private static final long serialVersionUID = -1006432360208247761L;

	public NoSolutionFromRandomCompletionException(final String msg) {
		super(msg);
	}

	public NoSolutionFromRandomCompletionException(final String msg, final Throwable cause) {
		super(cause, msg);
	}

}
