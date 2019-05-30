package jaicore.basic;

/**
 * Exception may be thrown if too many retries happened when trying to connect to the database via the SQLAdapter.
 *
 * @author mwever
 *
 */
public class TooManyConnectionRetriesException extends RuntimeException {

	public TooManyConnectionRetriesException(final String msg) {
		super(msg);
	}

	public TooManyConnectionRetriesException(final String msg, final Throwable cause) {
		super(msg, cause);
	}

}
