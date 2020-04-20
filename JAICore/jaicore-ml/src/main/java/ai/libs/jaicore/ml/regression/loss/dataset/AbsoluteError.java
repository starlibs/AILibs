package ai.libs.jaicore.ml.regression.loss.dataset;

import java.util.ArrayList;
import java.util.List;

import ai.libs.jaicore.basic.StatisticsUtil;

public class AbsoluteError extends ARegressionMeasure {

	@Override
	public double loss(final List<? extends Double> expected, final List<? extends Double> actual) {
		List<Double> errors = new ArrayList<>();
		for (int i = 0; i < expected.size(); i++) {
			Double error = Math.abs(expected.get(i) - actual.get(i));
			errors.add(error);
		}
		return StatisticsUtil.mean(errors);
	}
}
