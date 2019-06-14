package ai.libs.jaicore.experiments.exceptions;

@SuppressWarnings("serial")
public class ExperimentEvaluationFailedException extends Exception {
	public ExperimentEvaluationFailedException(final Exception e) {
		super(e);
	}

	public ExperimentEvaluationFailedException(final String message, final Exception e) {
		super(message, e);
	}
}
