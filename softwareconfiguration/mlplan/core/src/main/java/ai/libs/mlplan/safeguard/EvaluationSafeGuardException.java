package ai.libs.mlplan.safeguard;

import org.api4.java.common.attributedobjects.ObjectEvaluationFailedException;

import ai.libs.softwareconfiguration.model.ComponentInstance;

public class EvaluationSafeGuardException extends ObjectEvaluationFailedException {

	private ComponentInstance ci;

	public EvaluationSafeGuardException(final String message, final ComponentInstance ci) {
		super(message);
		this.ci = ci;
	}

	public ComponentInstance getCausingComponentInstance() {
		return this.ci;
	}

}
