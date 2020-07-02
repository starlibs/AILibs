package ai.libs.jaicore.ml.regression.loss.dataset;

import java.util.ArrayList;
import java.util.List;

import ai.libs.jaicore.basic.StatisticsUtil;

public class MeanPercentageError extends ARegressionMeasure {

	@Override
	public double loss(final List<? extends Double> expected, final List<? extends Double> predicted) {
		this.checkConsistency(expected, predicted);
		List<Double> errors = new ArrayList<>();
		for (int i = 0; i < expected.size(); i++) {
			Double percentageError = (expected.get(i) - predicted.get(i)) / expected.get(i);
			errors.add(percentageError);
		}
		return StatisticsUtil.mean(errors);
	}

}
