package ai.libs.jaicore.ml.regression.loss.instance;

import ai.libs.jaicore.ml.classification.loss.instance.AInstanceMeasure;

public class AbsoluteError extends AInstanceMeasure<Double, Double> {

	@Override
	public double loss(final Double actual, final Double expected) {
		return Math.abs(actual - expected);
	}

}
