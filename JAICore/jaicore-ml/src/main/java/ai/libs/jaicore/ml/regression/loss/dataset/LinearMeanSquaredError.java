package ai.libs.jaicore.ml.regression.loss.dataset;

import java.util.ArrayList;
import java.util.List;

import ai.libs.jaicore.basic.StatisticsUtil;

public class LinearMeanSquaredError extends ARegressionMeasure {

	private double weightA = 1;

	public LinearMeanSquaredError(double weightA) {
		this.weightA = weightA;
	}

	@Override
	public double loss(final List<? extends Double> expected, final List<? extends Double> actual) {
		List<Double> errors = new ArrayList<>();
		double mse = new MeanSquaredError().loss(expected, actual);

		for (int i = 0; i < expected.size(); i++) {
			Double error = 0d;
			Double d = expected.get(i) - actual.get(i);
			if (d <= 0) {
				error = -weightA * d;
			} else {
				error = mse;
			}
			errors.add(error);
		}
		return StatisticsUtil.mean(errors);
	}

}