package ai.libs.hyperopt;

import ai.libs.jaicore.ml.core.exception.CheckedJaicoreMLException;

/**
 *
 * @author kadirayk
 *
 */
public class OptimizationException extends CheckedJaicoreMLException {

	public OptimizationException(final String message) {
		super(message);
	}

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

}
