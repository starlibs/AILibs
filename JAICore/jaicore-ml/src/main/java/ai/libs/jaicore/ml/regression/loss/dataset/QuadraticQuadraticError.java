package ai.libs.jaicore.ml.regression.loss.dataset;

import java.util.ArrayList;
import java.util.List;

import ai.libs.jaicore.basic.StatisticsUtil;

public class QuadraticQuadraticError extends ARegressionMeasure {

	private double weightA = 1d;

	public QuadraticQuadraticError(final double weightA) {
		this.weightA = weightA;
	}

	@Override
	public double loss(final List<? extends Double> expected, final List<? extends Double> actual) {
		List<Double> errors = new ArrayList<>();
		for (int i = 0; i < expected.size(); i++) {
			double difference = expected.get(i) - actual.get(i);
			Double error;
			if (difference <= 0) {
				error = 2 * this.weightA * Math.pow(difference, 2);
			} else {
				error = 2 * (this.weightA + (1 - (2 * this.weightA))) * Math.pow(difference, 2);
			}
			errors.add(error);
		}
		return StatisticsUtil.mean(errors);
	}
}