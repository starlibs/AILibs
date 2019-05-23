package jaicore.ml.metafeatures;

/**
 * An exception that signifies something went wrong during the initialization of
 * a dataset characterizer
 * 
 * @author Helena Graf
 *
 */
public class DatasetCharacterizerInitializationFailedException extends Exception {

	/**
	 * version number
	 */
	private static final long serialVersionUID = -7200872055151544998L;

	/**
	 * Create an exception with a default message.
	 */
	public DatasetCharacterizerInitializationFailedException() {
		super();
	}

	/**
	 * Create an exception with the given message.
	 * 
	 * @param message
	 *            the used message
	 */
	public DatasetCharacterizerInitializationFailedException(String message) {
		super(message);
	}

	/**
	 * Create an exception with the given cause.
	 * 
	 * @param cause
	 *            the cause of the exception
	 */
	public DatasetCharacterizerInitializationFailedException(Throwable cause) {
		super(cause);
	}

	/**
	 * Create an exception with the given cause and additional message
	 * 
	 * @param message
	 *            the message
	 * @param cause
	 *            the cause of the exception
	 */
	public DatasetCharacterizerInitializationFailedException(String message, Throwable cause) {
		super(message, cause);
	}
}
