package jaicore.search.algorithms.standard.mcts;

@SuppressWarnings("serial")
public class ActionPredictionFailedException extends Exception {
	public ActionPredictionFailedException(final Exception e) {
		super(e);
	}

	public ActionPredictionFailedException(final String message, final Exception e) {
		super(message, e);
	}
}
