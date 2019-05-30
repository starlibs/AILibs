package jaicore.basic;

import java.io.IOException;

/**
 * This exception may be thrown if a File object points to a directory
 * instead of a file.
 *
 * @author mwever
 */
public class FileIsDirectoryException extends IOException {

	/**
	 * Automatically generated version UID for serialization.
	 */
	private static final long serialVersionUID = 5313365636204532994L;

	/**
	 * Standard c'tor.
	 */
	public FileIsDirectoryException() {
		super();
	}

	/**
	 * Constructor with a message.
	 * @param message The message.
	 */
	public FileIsDirectoryException(final String message) {
		super(message);
	}

	/**
	 * Constructor with message and a throwable as a cause.
	 * @param message The message.
	 * @param cause The cause of this exception.
	 */
	public FileIsDirectoryException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
