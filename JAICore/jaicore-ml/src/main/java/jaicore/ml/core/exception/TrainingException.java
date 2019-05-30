package jaicore.ml.core.exception;

/**
 * The {@link TrainingException} indicates that an error occurred during a training process. Details concerning the error can be inferred from the associated message.
 * 
 * @author Alexander Hetzer
 *
 */
public class TrainingException extends CheckedJaicoreMLException {

	private static final long serialVersionUID = -3684777835122718847L;

	/**
	 * Creates a new {@link TrainingException} with the given parameters.
	 * 
	 * @param message
	 *            The message of this {@link Exception}.
	 * @param cause
	 *            The underlying cause of this {@link Exception}.
	 */
	public TrainingException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Creates a new {@link TrainingException} with the given parameters.
	 * 
	 * @param message
	 *            The message of this {@link Exception}.
	 */
	public TrainingException(String message) {
		super(message);
	}

}
