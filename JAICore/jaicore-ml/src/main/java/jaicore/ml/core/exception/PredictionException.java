package jaicore.ml.core.exception;

/**
 * The {@link PredictionException} indicates that an error occurred during a prediction process. Details concerning the error can be inferred from the associated message.
 * 
 * @author Alexander Hetzer
 *
 */
public class PredictionException extends CheckedJaicoreMLException {

	private static final long serialVersionUID = -6893506621839121367L;

	/**
	 * Creates a new {@link PredictionException} with the given parameters.
	 * 
	 * @param message
	 *            The message of this {@link Exception}.
	 * @param cause
	 *            The underlying cause of this {@link Exception}.
	 */
	public PredictionException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Creates a new {@link PredictionException} with the given parameters.
	 * 
	 * @param message
	 *            The message of this {@link Exception}.
	 */
	public PredictionException(String message) {
		super(message);
	}

}
