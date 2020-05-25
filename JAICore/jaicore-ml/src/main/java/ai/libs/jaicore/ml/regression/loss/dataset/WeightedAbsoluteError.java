package ai.libs.jaicore.ml.regression.loss.dataset;

import java.util.ArrayList;
import java.util.List;

import ai.libs.jaicore.basic.StatisticsUtil;

public class WeightedAbsoluteError extends AUnboundedRegressionMeasure {

	private double weightUnderestimation = 1d;

	public WeightedAbsoluteError() {

	}

	public WeightedAbsoluteError(final double weightUnderestimation) {
		this.weightUnderestimation = weightUnderestimation;
	}

	@Override
	public double loss(final List<? extends Double> expected, final List<? extends Double> actual) {
		List<Double> errors = new ArrayList<>();
		for (int i = 0; i < expected.size(); i++) {
			double difference = actual.get(i) - expected.get(i);
			Double error;
			if (difference <= 0) {
				error = -this.weightUnderestimation * difference;
			} else {
				error = this.weightUnderestimation * difference;
			}
			errors.add(error);
		}
		return StatisticsUtil.mean(errors);
	}
}