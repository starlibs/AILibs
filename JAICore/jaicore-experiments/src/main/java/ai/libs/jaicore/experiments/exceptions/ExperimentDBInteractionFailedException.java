package ai.libs.jaicore.experiments.exceptions;

public class ExperimentDBInteractionFailedException extends Exception {

	public ExperimentDBInteractionFailedException(final String message, final Exception e) {
		super(message, e);
	}

	public ExperimentDBInteractionFailedException(final Exception e) {
		super(e);
	}
}
