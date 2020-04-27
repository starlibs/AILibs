package ai.libs.jaicore.ml.regression.loss.dataset;

import java.util.ArrayList;
import java.util.List;

import ai.libs.jaicore.basic.StatisticsUtil;

public class AsymmetricLoss extends ARegressionMeasure {

	@Override
	public double loss(final List<? extends Double> expected, final List<? extends Double> actual) {
		List<Double> accuracyList = new ArrayList<>();
		for (int i = 0; i < expected.size(); i++) {
			Double percentageError = 100 * ((expected.get(i) - actual.get(i)) / expected.get(i));
			Double accuracy;
			if (percentageError <= 0) {
				accuracy = Math.exp(-Math.log(0.5) * (percentageError / 5));
			} else {
				accuracy = Math.exp(Math.log(0.5) * (percentageError / 20));
			}
			accuracyList.add(accuracy);
		}
		return StatisticsUtil.mean(accuracyList);
	}

}
