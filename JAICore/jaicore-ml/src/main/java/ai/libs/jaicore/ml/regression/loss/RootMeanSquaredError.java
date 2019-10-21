package ai.libs.jaicore.ml.regression.loss;

import java.util.List;

import ai.libs.jaicore.ml.core.evaluation.ALossFunction;

/**
 * The root mean squared loss function.
 * This loss function computes the sum of differences of expected/actual pairs,
 * divides this by the number of observations, and takes the square root.
 *
 * @author mwever
 *
 */
public class RootMeanSquaredError extends ALossFunction {

	private static final MeanSquaredError MEAN_SQUARED_ERROR_LOSS = new MeanSquaredError();

	@Override
	public double loss(final List<?> expected, final List<?> actual) {
		return Math.sqrt(MEAN_SQUARED_ERROR_LOSS.loss(expected, actual));
	}
}
