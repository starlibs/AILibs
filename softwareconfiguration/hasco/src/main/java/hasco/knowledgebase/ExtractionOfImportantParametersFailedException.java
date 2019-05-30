package hasco.knowledgebase;

public class ExtractionOfImportantParametersFailedException extends Exception {

	/**
	 *
	 */
	private static final long serialVersionUID = -8995563919172816333L;

	public ExtractionOfImportantParametersFailedException(final String msg) {
		super(msg);
	}

	public ExtractionOfImportantParametersFailedException(final String msg, final Throwable cause) {
		super(msg, cause);
	}

}
