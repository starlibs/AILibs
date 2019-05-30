package hasco.exceptions;

@SuppressWarnings("serial")
public class ComponentInstantiationFailedException extends Exception {

	public ComponentInstantiationFailedException(Throwable cause, String message) {
		super(message, cause);
	}
}
