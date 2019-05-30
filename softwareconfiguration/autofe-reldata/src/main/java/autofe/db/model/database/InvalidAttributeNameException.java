package autofe.db.model.database;

public class InvalidAttributeNameException extends IllegalArgumentException {

	/**
	 *
	 */
	private static final long serialVersionUID = 6831574558895757214L;

	public InvalidAttributeNameException(final String msg, final Throwable cause) {
		super(msg, cause);
	}

	public InvalidAttributeNameException(final String msg) {
		super(msg);
	}

}
