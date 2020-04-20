package ai.libs.jaicore.ml.regression.loss.instance;

import ai.libs.jaicore.ml.classification.loss.instance.AInstanceMeasure;

public class SquaredLogarithmicError extends AInstanceMeasure<Double, Double> {

	@Override
	public double loss(final Double actual, final Double expected) {
		return Math.pow(Math.log(actual) - Math.log(expected), 2);
	}

}