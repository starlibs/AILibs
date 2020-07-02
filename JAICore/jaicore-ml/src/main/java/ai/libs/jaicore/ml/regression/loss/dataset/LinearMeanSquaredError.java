package ai.libs.jaicore.ml.regression.loss.dataset;

import java.util.ArrayList;
import java.util.List;

import ai.libs.jaicore.basic.StatisticsUtil;

public class LinearMeanSquaredError extends AUnboundedRegressionMeasure {

	private double weightUnderestimation = 1;

	public LinearMeanSquaredError() {
	}

	public LinearMeanSquaredError(final double weightUnderestimation) {
		this.weightUnderestimation = weightUnderestimation;
	}

	@Override
	public double loss(final List<? extends Double> expected, final List<? extends Double> actual) {
		List<Double> errors = new ArrayList<>();
		double mse = new MeanSquaredError().loss(expected, actual);

		for (int i = 0; i < expected.size(); i++) {
			Double difference = actual.get(i) - expected.get(i);
			Double error;
			if (difference <= 0) {
				error = -this.weightUnderestimation * difference;
			} else {
				error = mse;
			}
			errors.add(error);
		}
		return StatisticsUtil.mean(errors);
	}

}