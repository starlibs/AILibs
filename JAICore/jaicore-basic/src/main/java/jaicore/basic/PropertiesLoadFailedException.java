package jaicore.basic;

/**
 * Exception can be thrown if properties could not be loaded properly.
 *
 * @author mwever
 */
public class PropertiesLoadFailedException extends RuntimeException {

	public PropertiesLoadFailedException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * Automatically generated version UID for serialization.
	 */
	private static final long serialVersionUID = 1433423547742787695L;

}
