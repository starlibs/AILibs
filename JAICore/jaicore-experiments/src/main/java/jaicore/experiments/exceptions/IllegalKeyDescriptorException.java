package jaicore.experiments.exceptions;

public class IllegalKeyDescriptorException extends IllegalExperimentSetupException {
	public IllegalKeyDescriptorException(final String message) {
		super(message);

	}

	public IllegalKeyDescriptorException(final Exception e) {
		super(e);
	}
}
