package ai.libs.hyperopt;

import ai.libs.jaicore.ml.core.exception.CheckedJaicoreMLException;

/**
 *
 * @author kadirayk
 *
 */
public class OptimizationException extends CheckedJaicoreMLException {

	private static final long serialVersionUID = 1L;

	public OptimizationException(final String message) {
		super(message);
	}

	public OptimizationException(final String message, final Throwable t) {
		super(message, t);
	}

	public OptimizationException(final Throwable t) {
		super(t);
	}

}
