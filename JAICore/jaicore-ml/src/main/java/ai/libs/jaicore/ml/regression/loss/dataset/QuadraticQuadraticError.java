package ai.libs.jaicore.ml.regression.loss.dataset;

import java.util.ArrayList;
import java.util.List;

import ai.libs.jaicore.basic.StatisticsUtil;

public class QuadraticQuadraticError extends AUnboundedRegressionMeasure {

	private double weightUnderestimation = 1d;

	public QuadraticQuadraticError() {
	}

	public QuadraticQuadraticError(final double weightUnderestimation) {
		this.weightUnderestimation = weightUnderestimation;
	}

	@Override
	public double loss(final List<? extends Double> expected, final List<? extends Double> actual) {
		List<Double> errors = new ArrayList<>();
		for (int i = 0; i < expected.size(); i++) {
			double difference = actual.get(i) - expected.get(i);
			Double error;
			if (difference <= 0) {
				error = 2 * this.weightUnderestimation * Math.pow(difference, 2);
			} else {
				error = 2 * (this.weightUnderestimation + (1 - (2 * this.weightUnderestimation))) * Math.pow(difference, 2);
			}
			errors.add(error);
		}
		return StatisticsUtil.mean(errors);
	}
}