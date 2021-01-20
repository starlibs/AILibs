package ai.libs.jaicore.ml.hpo.ga;

public class EvaluationBudgetExhaustedException extends Exception {

	public EvaluationBudgetExhaustedException() {
	}

	public EvaluationBudgetExhaustedException(final String message) {
		super(message);
	}

	public EvaluationBudgetExhaustedException(final Throwable cause) {
		super(cause);
	}

	public EvaluationBudgetExhaustedException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public EvaluationBudgetExhaustedException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
