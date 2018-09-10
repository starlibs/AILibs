package jaicore.search;

@SuppressWarnings("serial")
public class SearchException extends Exception {
	
	private final Throwable cause;

	public SearchException(String message, Throwable cause) {
		super(message);
		this.cause = cause;
		
	}
	
	@Override
	public Throwable getCause() {
		return cause;
	}
}
