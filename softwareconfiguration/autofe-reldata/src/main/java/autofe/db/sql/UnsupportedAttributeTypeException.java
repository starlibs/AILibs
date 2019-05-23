package autofe.db.sql;

public class UnsupportedAttributeTypeException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5963212203601747004L;

	public UnsupportedAttributeTypeException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public UnsupportedAttributeTypeException(String msg) {
		super(msg);
	}

}
