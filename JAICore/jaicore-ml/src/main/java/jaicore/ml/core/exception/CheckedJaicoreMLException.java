package jaicore.ml.core.exception;

/**
 * The {@link CheckedJaicoreMLException} serves as a base class for all checked {@link Exception}s defined as part of jaicore-ml.
 * 
 * @author Alexander Hetzer
 *
 */
public abstract class CheckedJaicoreMLException extends Exception {

	private static final long serialVersionUID = 7366050163157197392L;

	/**
	 * Creates a new {@link CheckedJaicoreMLException} with the given parameters.
	 * 
	 * @param message
	 *            The message of this {@link Exception}.
	 * @param cause
	 *            The underlying cause of this {@link Exception}.
	 */
	public CheckedJaicoreMLException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Creates a new {@link CheckedJaicoreMLException} with the given parameters.
	 * 
	 * @param message
	 *            The message of this {@link Exception}.
	 */
	public CheckedJaicoreMLException(String message) {
		super(message);
	}
}
