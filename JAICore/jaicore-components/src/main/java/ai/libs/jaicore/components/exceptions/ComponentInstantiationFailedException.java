package ai.libs.jaicore.components.exceptions;

@SuppressWarnings("serial")
public class ComponentInstantiationFailedException extends Exception {

	public ComponentInstantiationFailedException(final String message) {
		super(message);
	}

	public ComponentInstantiationFailedException(final Throwable cause, final String message) {
		super(message, cause);
	}
}
