package ai.libs.mlplan.safeguard;

import org.api4.java.common.attributedobjects.ObjectEvaluationFailedException;

import ai.libs.jaicore.components.api.IComponentInstance;

public class EvaluationSafeGuardException extends ObjectEvaluationFailedException {

	/**
	 * Auto-generated serial version UID.
	 */
	private static final long serialVersionUID = 2170317514693997168L;

	private final IComponentInstance ci;

	public EvaluationSafeGuardException(final String message, final IComponentInstance ci) {
		super(message);
		this.ci = ci;
	}

	public IComponentInstance getCausingComponentInstance() {
		return this.ci;
	}

}
