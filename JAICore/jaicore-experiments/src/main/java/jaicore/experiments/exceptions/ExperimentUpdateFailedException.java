package jaicore.experiments.exceptions;

public class ExperimentUpdateFailedException extends ExperimentDBInteractionFailedException {
	public ExperimentUpdateFailedException(final Exception e) {
		super(e);
	}
}
