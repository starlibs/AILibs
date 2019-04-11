package jaicore.ml.core.evaluation.measure.singlelabel;

import java.util.List;

import jaicore.basic.aggregate.IAggregateFunction;

/**
 * The root mean squared loss function.
 * This loss function computes the sum of differences of expected/actual pairs,
 * divides this by the number of observations, and takes the square root.
 *
 * @author mwever
 *
 */
public class RootMeanSquaredErrorLoss extends ASquaredErrorLoss {

	/**
	 * Automatically generated version UID for serialization.
	 */
	private static final long serialVersionUID = -5957690628883765930L;

	@Override
	public Double calculateMeasure(final List<Double> actual, final List<Double> expected, final IAggregateFunction<Double> aggregateFunction) {
		throw new UnsupportedOperationException("This loss can only be accessed via calculateAvgMeasure");
	}

	@Override
	public Double calculateAvgMeasure(final List<Double> actual, final List<Double> expected) {
		return Math.sqrt(super.calculateAvgMeasure(actual, expected));
	}

}
