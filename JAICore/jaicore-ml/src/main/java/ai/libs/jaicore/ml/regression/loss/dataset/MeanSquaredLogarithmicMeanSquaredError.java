package ai.libs.jaicore.ml.regression.loss.dataset;

import java.util.ArrayList;
import java.util.List;

import ai.libs.jaicore.basic.StatisticsUtil;

public class MeanSquaredLogarithmicMeanSquaredError extends ARegressionMeasure {

	@Override
	public double loss(final List<? extends Double> expected, final List<? extends Double> actual) {
		List<Double> errors = new ArrayList<>();
		double msle = new MeanSquaredLogarithmicError().loss(expected, actual);
		double mse = new MeanSquaredError().loss(expected, actual);

		for (int i = 0; i < expected.size(); i++) {
			Double error = 0d;
			Double d = expected.get(i) - actual.get(i);
			if (d <= 0) {
				error = msle;
			} else {
				error = mse;
			}
			errors.add(error);
		}
		return StatisticsUtil.mean(errors);
	}
}