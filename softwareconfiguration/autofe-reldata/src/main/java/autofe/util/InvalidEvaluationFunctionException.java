package autofe.util;

public class InvalidEvaluationFunctionException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4066392169154407642L;

	public InvalidEvaluationFunctionException(String msg) {
		super(msg);
	}

	public InvalidEvaluationFunctionException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
