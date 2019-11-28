package ai.libs.jaicore.ml.core.filter.sampling.inmemory.casecontrol;

public class ThresholdComputationFailedException extends Exception {

	public ThresholdComputationFailedException(final String message) {
		super(message);
	}

	public ThresholdComputationFailedException(final String message, final Exception e) {
		super(message, e);
	}

	public ThresholdComputationFailedException(final Exception e) {
		super(e);
	}
}
