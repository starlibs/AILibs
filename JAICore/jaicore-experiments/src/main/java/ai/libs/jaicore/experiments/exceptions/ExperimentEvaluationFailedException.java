package ai.libs.jaicore.experiments.exceptions;

@SuppressWarnings("serial")
public class ExperimentEvaluationFailedException extends Exception {
	public ExperimentEvaluationFailedException(final String s) {
		super(s);
	}

	public ExperimentEvaluationFailedException(final Throwable e) {
		super(e);
	}

	public ExperimentEvaluationFailedException(final String message, final Throwable e) {
		super(message, e);
	}
}
