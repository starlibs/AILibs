package ai.libs.jaicore.ml.regression.loss.dataset;

import java.util.List;

/**
 * The root mean squared loss function.
 * This loss function computes the sum of differences of expected/actual pairs,
 * divides this by the number of observations, and takes the square root.
 *
 * @author mwever
 *
 */
public class RootMeanSquaredError extends ARegressionMeasure {

	private static final MeanSquaredError MEAN_SQUARED_ERROR_LOSS = new MeanSquaredError();

	@Override
	public double loss(final List<? extends Double> actual, final List<? extends Double> expected) {
		this.checkConsistency(expected, actual);
		return Math.sqrt(MEAN_SQUARED_ERROR_LOSS.loss(expected, actual));
	}

}
