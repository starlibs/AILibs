package ai.libs.jaicore.ml.regression.loss;

import org.api4.java.ai.ml.core.evaluation.loss.ILossFunction;

/**
 * Measure computing the squared error of two doubles. It can be used to compute the mean squared error.
 *
 * @author mwever
 */
public class SquaredError implements ILossFunction<Double> {

	@Override
	public double loss(final Double actual, final Double expected) {
		return Math.pow(actual - expected, 2);
	}

}
