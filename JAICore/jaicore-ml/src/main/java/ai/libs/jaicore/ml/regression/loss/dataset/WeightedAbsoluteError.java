package ai.libs.jaicore.ml.regression.loss.dataset;

import java.util.ArrayList;
import java.util.List;

import ai.libs.jaicore.basic.StatisticsUtil;

public class WeightedAbsoluteError extends ARegressionMeasure {

	private double weight = 1d;

	public WeightedAbsoluteError(final double weight) {
		this.weight = weight;
	}

	@Override
	public double loss(final List<? extends Double> expected, final List<? extends Double> actual) {
		List<Double> errors = new ArrayList<>();
		for (int i = 0; i < expected.size(); i++) {
			double difference = actual.get(i) - expected.get(i);
			Double error;
			if (difference <= 0) {
				error = -this.weight * difference;
			} else {
				error = this.weight * difference;
			}
			errors.add(error);
		}
		return StatisticsUtil.mean(errors);
	}
}