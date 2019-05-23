package de.upb.crc901.mlplan.metamining.pipelinecharacterizing;

/**
 * An exception signaling that the {@link ComponentInstanceStringConverter}
 * could not properly be intialized.
 * 
 * @author Helena Graf
 *
 */
public class ComponentInstanceStringConverterIntializeException extends RuntimeException {

	/**
	 * generated id
	 */
	private static final long serialVersionUID = 5483934746870892252L;

	/**
	 * Create a new generic exception.
	 */
	public ComponentInstanceStringConverterIntializeException() {
		super();
	}

	/**
	 * Create a new exception with the given message.
	 * 
	 * @param message
	 *            a message describing the exception
	 */
	public ComponentInstanceStringConverterIntializeException(String message) {
		super(message);
	}

	/**
	 * Create a new exception with the given cause.
	 * 
	 * @param cause
	 *            the cause of the exception
	 */
	public ComponentInstanceStringConverterIntializeException(Throwable cause) {
		super(cause);
	}

	/**
	 * Create a new exception with a given message and cause.
	 * 
	 * @param message
	 *            a message describing the exception
	 * @param cause
	 *            the cause of the exception
	 */
	public ComponentInstanceStringConverterIntializeException(String message, Throwable cause) {
		super(message, cause);
	}

}
