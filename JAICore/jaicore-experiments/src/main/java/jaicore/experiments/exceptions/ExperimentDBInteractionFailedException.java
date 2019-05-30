package jaicore.experiments.exceptions;

public class ExperimentDBInteractionFailedException extends Exception {
	public ExperimentDBInteractionFailedException(final Exception e) {
		super(e);
	}
}
