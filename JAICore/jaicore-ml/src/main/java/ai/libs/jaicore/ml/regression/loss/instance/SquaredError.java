package ai.libs.jaicore.ml.regression.loss.instance;

import ai.libs.jaicore.ml.classification.loss.instance.AInstanceMeasure;

/**
 * Measure computing the squared error of two doubles. It can be used to compute the mean squared error.
 *
 * @author mwever
 */
public class SquaredError extends AInstanceMeasure<Double, Double> {

	@Override
	public double loss(final Double actual, final Double expected) {
		return Math.pow(actual - expected, 2);
	}

}
