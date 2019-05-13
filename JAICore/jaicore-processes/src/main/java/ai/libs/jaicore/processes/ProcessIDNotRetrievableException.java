package ai.libs.jaicore.processes;

public class ProcessIDNotRetrievableException extends Exception {

	/**
	 *
	 */
	private static final long serialVersionUID = -719315021772711013L;

	public ProcessIDNotRetrievableException(final String msg) {
		super(msg);
	}

	public ProcessIDNotRetrievableException(final String msg, final Throwable cause) {
		super(msg, cause);
	}

}
