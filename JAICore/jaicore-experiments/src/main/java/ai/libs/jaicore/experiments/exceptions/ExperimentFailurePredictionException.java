package ai.libs.jaicore.experiments.exceptions;

public class ExperimentFailurePredictionException extends Exception {

	public ExperimentFailurePredictionException() {
		super();
	}

	public ExperimentFailurePredictionException(final String msg) {
		super(msg);
	}

	public ExperimentFailurePredictionException(final String msg, final Throwable t) {
		super(msg, t);
	}

	public ExperimentFailurePredictionException(final Throwable t) {
		super(t);
	}
}
