package autofe.db.search;

public class NoSolutionFromRandomCompletionException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1006432360208247761L;

	public NoSolutionFromRandomCompletionException() {
		super();
	}

	public NoSolutionFromRandomCompletionException(String msg) {
		super(msg);
	}

	public NoSolutionFromRandomCompletionException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
