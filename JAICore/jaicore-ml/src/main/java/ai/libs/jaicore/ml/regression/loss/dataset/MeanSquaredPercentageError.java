package ai.libs.jaicore.ml.regression.loss.dataset;

import java.util.ArrayList;
import java.util.List;

import ai.libs.jaicore.basic.StatisticsUtil;

public class MeanSquaredPercentageError extends ARegressionMeasure {

	@Override
	public double loss(final List<? extends Double> expected, final List<? extends Double> actual) {
		this.checkConsistency(expected, actual);
		List<Double> errors = new ArrayList<>();
		for (int i = 0; i < expected.size(); i++) {
			Double percentageError = (expected.get(i) - actual.get(i)) / expected.get(i);
			errors.add(Math.pow(percentageError, 2));
		}
		return StatisticsUtil.mean(errors);
	}

}
