package jaicore.graphvisualizer.events.graph.bus;

public class HandleAlgorithmEventException extends Exception {

	private static final long serialVersionUID = 570998902568482515L;

	protected HandleAlgorithmEventException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public HandleAlgorithmEventException(String message, Throwable cause) {
		super(message, cause);
	}

	public HandleAlgorithmEventException(String message) {
		super(message);
	}

}
