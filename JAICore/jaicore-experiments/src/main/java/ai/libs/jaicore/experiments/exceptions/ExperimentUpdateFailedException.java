package ai.libs.jaicore.experiments.exceptions;

public class ExperimentUpdateFailedException extends ExperimentDBInteractionFailedException {

	private static final long serialVersionUID = 662503302081674486L;

	public ExperimentUpdateFailedException(final String message, final Exception e) {
		super(message, e);
	}

	public ExperimentUpdateFailedException(final Exception e) {
		super(e);
	}
}
