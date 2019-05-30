package jaicore.basic;

public class SystemRequirementsNotMetException extends RuntimeException {

	/**
	 * Automatically generated version UID for serialization.
	 */
	private static final long serialVersionUID = -7591975042606762847L;

	public SystemRequirementsNotMetException(final String msg) {
		super(msg);
	}

	public SystemRequirementsNotMetException(final String msg, final Throwable cause) {
		super(msg, cause);
	}

}
