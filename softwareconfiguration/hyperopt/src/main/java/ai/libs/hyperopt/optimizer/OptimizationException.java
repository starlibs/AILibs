package ai.libs.hyperopt.optimizer;

import ai.libs.jaicore.ml.core.exception.CheckedJaicoreMLException;

/**
 * 
 * @author kadirayk
 *
 */
public class OptimizationException extends CheckedJaicoreMLException {

	public OptimizationException(String message) {
		super(message);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
