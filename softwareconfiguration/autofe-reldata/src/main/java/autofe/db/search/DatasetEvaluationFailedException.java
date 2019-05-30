package autofe.db.search;

import jaicore.search.algorithms.standard.bestfirst.exceptions.NodeEvaluationException;

public class DatasetEvaluationFailedException extends NodeEvaluationException {

	/**
	 *
	 */
	private static final long serialVersionUID = 5297889490100358566L;

	public DatasetEvaluationFailedException(final String msg, final Throwable cause) {
		super(cause, msg);
	}

}
