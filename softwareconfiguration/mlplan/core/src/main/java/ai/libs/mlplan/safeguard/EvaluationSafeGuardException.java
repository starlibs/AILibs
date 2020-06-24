package ai.libs.mlplan.safeguard;

import org.api4.java.common.attributedobjects.ObjectEvaluationFailedException;

import ai.libs.hasco.model.ComponentInstance;

public class EvaluationSafeGuardException extends ObjectEvaluationFailedException {

	/**
	 * Auto-generated serial version UID.
	 */
	private static final long serialVersionUID = 2170317514693997168L;

	private final ComponentInstance ci;

	public EvaluationSafeGuardException(final String message, final ComponentInstance ci) {
		super(message);
		this.ci = ci;
	}

	public ComponentInstance getCausingComponentInstance() {
		return this.ci;
	}

}
