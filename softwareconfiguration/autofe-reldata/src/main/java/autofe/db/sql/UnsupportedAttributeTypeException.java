package autofe.db.sql;

public class UnsupportedAttributeTypeException extends Exception {

	/**
	 *
	 */
	private static final long serialVersionUID = -5963212203601747004L;

	public UnsupportedAttributeTypeException(final String msg, final Throwable cause) {
		super(msg, cause);
	}

	public UnsupportedAttributeTypeException(final String msg) {
		super(msg);
	}

}
