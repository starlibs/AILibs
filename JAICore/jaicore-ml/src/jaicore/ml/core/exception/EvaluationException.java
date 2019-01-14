package jaicore.ml.core.exception;

/**
 * The {@link EvaluationException} indicates that an error occurred during a
 * evaluation process. Details concerning the error can be inferred from the
 * associated message.
 * 
 * @author Julian Lienen
 *
 */
public class EvaluationException extends CheckedJaicoreMLException {

	/**
	 * Generated serial version UID.
	 */
	private static final long serialVersionUID = -222252014216889955L;

	/**
	 * Creates a new {@link EvaluationException} with the given parameters.
	 * 
	 * @param message
	 *            The message of this {@link Exception}.
	 */
	public EvaluationException(String message) {
		super(message);
	}

	/**
	 * Creates a new {@link EvaluationException} with the given parameters.
	 * 
	 * @param message
	 *            The message of this {@link Exception}.
	 * @param cause
	 *            The underlying cause of this {@link Exception}.
	 */
	public EvaluationException(String message, Throwable cause) {
		super(message, cause);
	}

}
