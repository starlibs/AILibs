package ai.libs.jaicore.ml.regression.loss;

import java.util.List;

import org.api4.java.ai.ml.core.evaluation.loss.ILossFunction;

/**
 * The root mean squared loss function.
 * This loss function computes the sum of differences of expected/actual pairs,
 * divides this by the number of observations, and takes the square root.
 *
 * @author mwever
 *
 */
public class RootMeanSquaredError implements ILossFunction<Double> {

	private static final MeanSquaredError MEAN_SQUARED_ERROR_LOSS = new MeanSquaredError();

	@Override
	public double loss(final List<Double> expected, final List<Double> actual) {
		return Math.sqrt(MEAN_SQUARED_ERROR_LOSS.loss(expected, actual));
	}

}
