package ai.libs.jaicore.experiments.exceptions;

public class ExperimentDecodingException extends Exception {

	private static final long serialVersionUID = 6800781691160306725L;

	public ExperimentDecodingException(final String message) {
		super(message);
	}

	public ExperimentDecodingException(final String message, final Exception e) {
		super(message, e);
	}

	public ExperimentDecodingException(final Exception e) {
		super(e);
	}
}
