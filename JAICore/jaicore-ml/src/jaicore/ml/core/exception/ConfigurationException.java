package jaicore.ml.core.exception;

/**
 * The {@link ConfigurationException} indicates an error during a configuration process. Details concerning the error can be inferred from the associated message.
 * 
 * @author Alexander Hetzer
 *
 */
public class ConfigurationException extends CheckedJaicoreMLException {

	private static final long serialVersionUID = 3979468542526154560L;

	/**
	 * Creates a new {@link ConfigurationException} with the given parameters.
	 * 
	 * @param message
	 *            The message of this {@link Exception}.
	 * @param cause
	 *            The underlying cause of this {@link Exception}.
	 */
	public ConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Creates a new {@link ConfigurationException} with the given parameters.
	 * 
	 * @param message
	 *            The message of this {@link Exception}.
	 */
	public ConfigurationException(String message) {
		super(message);
	}

}
