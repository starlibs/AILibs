package jaicore.experiments.exceptions;

public class IllegalExperimentSetupException extends Exception {
	public IllegalExperimentSetupException(final String message) {
		super(message);
	}

	public IllegalExperimentSetupException(final Exception e) {
		super(e);
	}
}
