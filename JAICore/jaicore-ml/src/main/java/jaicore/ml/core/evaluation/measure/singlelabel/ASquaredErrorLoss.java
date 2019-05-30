package jaicore.ml.core.evaluation.measure.singlelabel;

import java.io.Serializable;

import jaicore.ml.core.evaluation.measure.ADecomposableDoubleMeasure;

/**
 * Measure computing the squared error of two doubles. It can be used to compute the mean squared error.
 *
 * @author mwever
 */
public abstract class ASquaredErrorLoss extends ADecomposableDoubleMeasure<Double> implements Serializable {

	/**
	 * Automatically generated version UID for serialization.
	 */
	private static final long serialVersionUID = -8837491518435902915L;

	@Override
	public Double calculateMeasure(final Double actual, final Double expected) {
		return Math.pow(actual - expected, 2);
	}

}
