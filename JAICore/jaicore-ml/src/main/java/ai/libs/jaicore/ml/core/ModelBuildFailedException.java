package ai.libs.jaicore.ml.core;

import ai.libs.jaicore.basic.algorithm.exceptions.ObjectEvaluationFailedException;

public class ModelBuildFailedException extends ObjectEvaluationFailedException {

	/**
	 * Automatically generated version UID for serialization.
	 */
	private static final long serialVersionUID = 6102494502254352088L;

	public ModelBuildFailedException(final String msg) {
		super(msg);
	}

	public ModelBuildFailedException(final String msg, final Throwable cause) {
		super(msg, cause);
	}

}
