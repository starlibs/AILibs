package ai.libs.jaicore.ml.regression.loss;

import org.api4.java.ai.ml.core.evaluation.loss.IInstanceWiseLossFunction;

/**
 * Measure computing the squared error of two doubles. It can be used to compute the mean squared error.
 *
 * @author mwever
 */
public class SquaredError implements IInstanceWiseLossFunction {

	@Override
	public double loss(final Object actual, final Object expected) {
		return Math.pow((double)actual - (double)expected, 2);
	}

}
