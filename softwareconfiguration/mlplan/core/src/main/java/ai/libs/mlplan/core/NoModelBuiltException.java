package ai.libs.mlplan.core;

public class NoModelBuiltException extends Exception {

	/**
	 *
	 */
	private static final long serialVersionUID = 5348375646068721111L;

	public NoModelBuiltException() {
		super();
	}

	public NoModelBuiltException(final String message) {
		super(message);
	}

	public NoModelBuiltException(final Throwable cause) {
		super(cause);
	}

	public NoModelBuiltException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public NoModelBuiltException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
