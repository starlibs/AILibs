package ai.libs.jaicore.graphvisualizer.events.recorder.property;

public class PropertyComputationFailedException extends Exception {

	private static final long serialVersionUID = 2855501913630309292L;

	public PropertyComputationFailedException() {
		super();
	}

	public PropertyComputationFailedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public PropertyComputationFailedException(String message, Throwable cause) {
		super(message, cause);
	}

	public PropertyComputationFailedException(String message) {
		super(message);
	}

	public PropertyComputationFailedException(Throwable cause) {
		super(cause);
	}

}
