package ai.libs.jaicore.ml.core.exception;

public class InconsistentDataFormatException extends IllegalArgumentException {

	/**
	 *
	 */
	private static final long serialVersionUID = -191627526519227789L;

	public InconsistentDataFormatException() {
		super();
	}

	public InconsistentDataFormatException(final String msg) {
		super(msg);
	}

	public InconsistentDataFormatException(final String msg, final Throwable cause) {
		super(msg, cause);
	}

	public InconsistentDataFormatException(final Throwable cause) {
		super(cause);
	}

}
