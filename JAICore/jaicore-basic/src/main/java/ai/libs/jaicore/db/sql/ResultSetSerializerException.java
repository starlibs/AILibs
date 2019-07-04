package ai.libs.jaicore.db.sql;

import com.fasterxml.jackson.core.JsonProcessingException;

public class ResultSetSerializerException extends JsonProcessingException {

	/**
	 * Automatically generated UID for serialization.
	 */
	private static final long serialVersionUID = -5056231809515489843L;

	public ResultSetSerializerException(final String msg) {
		super(msg);
	}

	public ResultSetSerializerException(final String msg, final Throwable cause) {
		super(msg, cause);
	}

	public ResultSetSerializerException(final Throwable cause) {
		super(cause);
	}

}
