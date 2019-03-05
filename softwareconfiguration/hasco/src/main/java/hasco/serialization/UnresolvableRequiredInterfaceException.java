package hasco.serialization;

/**
 * This exception can be thrown if components define required interfaces which cannot be resolved with the so far seen provided interfaces of components.
 * 
 * @author wever
 */
public class UnresolvableRequiredInterfaceException extends RuntimeException {

	/**
	 * Auto-generated version UID for serialization.
	 */
	private static final long serialVersionUID = -930881442829770230L;

	public UnresolvableRequiredInterfaceException() {
		super();
	}
	
	public UnresolvableRequiredInterfaceException(String msg) {
		super(msg);
	}
	
	public UnresolvableRequiredInterfaceException(Throwable cause) {
		super(cause);
	}
	
	public UnresolvableRequiredInterfaceException(String msg, Throwable cause) {
		super(msg, cause);
	}
	
	
	
}
