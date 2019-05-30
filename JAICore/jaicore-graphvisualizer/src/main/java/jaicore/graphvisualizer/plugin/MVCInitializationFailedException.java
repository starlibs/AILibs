package jaicore.graphvisualizer.plugin;

@SuppressWarnings("serial")
public class MVCInitializationFailedException extends Exception {
	public MVCInitializationFailedException() {
		super();
	}
	
	public MVCInitializationFailedException(String message) {
		super(message);
	}
	
	public MVCInitializationFailedException(Throwable throwable) {
		super(throwable);
	}
	
	public MVCInitializationFailedException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
