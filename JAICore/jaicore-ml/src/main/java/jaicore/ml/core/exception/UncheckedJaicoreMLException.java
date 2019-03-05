package jaicore.ml.core.exception;

/**
 * The {@link UncheckedJaicoreMLException} serves as a base class for all unchecked {@link Exception}s defined as part of jaicore-ml.
 * 
 * @author Alexander Hetzer
 *
 */
public abstract class UncheckedJaicoreMLException extends RuntimeException {

	private static final long serialVersionUID = 5949039077785112560L;

	/**
	 * Creates a new {@link UncheckedJaicoreMLException} with the given parameters.
	 * 
	 * @param message
	 *            The message of this {@link Exception}.
	 * @param cause
	 *            The underlying cause of this {@link Exception}.
	 */
	public UncheckedJaicoreMLException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Creates a new {@link UncheckedJaicoreMLException} with the given parameters.
	 * 
	 * @param message
	 *            The message of this {@link Exception}.
	 */
	public UncheckedJaicoreMLException(String message) {
		super(message);
	}

}
