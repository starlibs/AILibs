package autofe.db.model.database;

public class InvalidAttributeNameException  extends RuntimeException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6831574558895757214L;

	public InvalidAttributeNameException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public InvalidAttributeNameException(String msg) {
		super(msg);
	}

}
