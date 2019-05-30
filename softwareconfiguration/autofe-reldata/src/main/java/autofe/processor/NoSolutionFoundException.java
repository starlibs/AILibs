package autofe.processor;

public class NoSolutionFoundException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1521895656706976139L;

	public NoSolutionFoundException(String msg) {
		super(msg);
	}

	public NoSolutionFoundException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
