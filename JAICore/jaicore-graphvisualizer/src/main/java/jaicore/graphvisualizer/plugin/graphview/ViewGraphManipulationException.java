package jaicore.graphvisualizer.plugin.graphview;

public class ViewGraphManipulationException extends Exception {

	private static final long serialVersionUID = -3854909479243383118L;

	protected ViewGraphManipulationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ViewGraphManipulationException(String message, Throwable cause) {
		super(message, cause);
	}

	public ViewGraphManipulationException(Throwable cause) {
		super(cause);
	}

	public ViewGraphManipulationException(String message) {
		super(message);
	}

}
