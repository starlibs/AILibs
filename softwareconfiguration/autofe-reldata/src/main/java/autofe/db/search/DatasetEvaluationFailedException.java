package autofe.db.search;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.PathEvaluationException;

public class DatasetEvaluationFailedException extends PathEvaluationException {

	/**
	 *
	 */
	private static final long serialVersionUID = 5297889490100358566L;

	public DatasetEvaluationFailedException(final String msg, final Throwable cause) {
		super(msg, cause);
	}

}
