package ai.libs.jaicore.ml.regression.loss.instance;

import ai.libs.jaicore.ml.classification.loss.instance.AInstanceMeasure;

public class AbsoluteError extends AInstanceMeasure<Double, Double> {

	@Override
	public double loss(final Double expected, final Double predicted) {
		return Math.abs(expected - predicted);
	}

}
