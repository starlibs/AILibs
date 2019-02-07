package jaicore.graphvisualizer.events.graph.bus;

public class HandleGraphEventException extends Exception {

	private static final long serialVersionUID = 570998902568482515L;

	protected HandleGraphEventException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public HandleGraphEventException(String message, Throwable cause) {
		super(message, cause);
	}

	public HandleGraphEventException(String message) {
		super(message);
	}

}
