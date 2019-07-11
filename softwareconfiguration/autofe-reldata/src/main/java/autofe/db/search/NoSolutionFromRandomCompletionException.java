package autofe.db.search;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.PathEvaluationException;

public class NoSolutionFromRandomCompletionException extends PathEvaluationException {

	/**
	 *
	 */
	private static final long serialVersionUID = -1006432360208247761L;

	public NoSolutionFromRandomCompletionException(final String msg) {
		super(msg);
	}

	public NoSolutionFromRandomCompletionException(final String msg, final Throwable cause) {
		super(msg, cause);
	}

}
