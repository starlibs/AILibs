package jaicore.ml.intervaltree;

public class PredictionFailedException extends RuntimeException {

	/**
	 *
	 */
	private static final long serialVersionUID = -802385780037700995L;

	public PredictionFailedException(final String msg) {
		super(msg);
	}

	public PredictionFailedException(final String msg, final Throwable cause) {
		super(msg, cause);
	}

	public PredictionFailedException(final Throwable cause) {
		super(cause);
	}

}
