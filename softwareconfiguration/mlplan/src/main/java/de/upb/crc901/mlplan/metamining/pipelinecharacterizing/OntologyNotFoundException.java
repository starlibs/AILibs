package de.upb.crc901.mlplan.metamining.pipelinecharacterizing;

/**
 * Exception thrown when an ontology can not be loaded.
 * 
 * @author Helena Graf
 *
 */
public class OntologyNotFoundException extends RuntimeException {

	/**
	 * generated id
	 */
	private static final long serialVersionUID = 1847206504544179128L;

	/**
	 * Create a new generic exception.
	 */
	public OntologyNotFoundException() {
		super();
	}

	/**
	 * Create a new exception with the given message.
	 * 
	 * @param message
	 *            a message describing the exception
	 */
	public OntologyNotFoundException(String message) {
		super(message);
	}

	/**
	 * Create a new exception with the given cause.
	 * 
	 * @param cause
	 *            the cause of the exception
	 */
	public OntologyNotFoundException(Throwable cause) {
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
	public OntologyNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}
