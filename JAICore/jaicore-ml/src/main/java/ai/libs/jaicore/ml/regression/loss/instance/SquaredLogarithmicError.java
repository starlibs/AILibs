package ai.libs.jaicore.ml.regression.loss.instance;

import ai.libs.jaicore.ml.classification.loss.instance.AInstanceMeasure;

public class SquaredLogarithmicError extends AInstanceMeasure<Double, Double> {

	private static final double DEF_EPSILON = 1E-15;

	private final double epsilon;

	public SquaredLogarithmicError(final double epsilon) {
		this.epsilon = epsilon;
	}

	public SquaredLogarithmicError() {
		this(DEF_EPSILON);
	}

	@Override
	public double loss(final Double expected, final Double predicted) {
		return Math.pow(Math.log(this.clip(expected)) - Math.log(this.clip(predicted)), 2);
	}

	private double clip(final double value) {
		if (value == 0.0) {
			return this.epsilon;
		}
		return value;
	}

}