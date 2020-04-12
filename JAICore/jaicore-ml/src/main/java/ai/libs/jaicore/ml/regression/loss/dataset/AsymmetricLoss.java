package ai.libs.jaicore.ml.regression.loss.dataset;

import java.util.ArrayList;
import java.util.List;

import ai.libs.jaicore.basic.StatisticsUtil;

public class AsymmetricLoss extends ARegressionMeasure {

	@Override
	public double loss(final List<? extends Double> expected, final List<? extends Double> actual) {

		List<Double> accuracyList = new ArrayList<>();

		for (int i = 0; i < expected.size(); i++) {
			Double Err = 100 * ((expected.get(i) - actual.get(i)) / expected.get(i));
			Double A = 0.0;
			if (Err <= 0) {
				A = Math.exp(-Math.log(0.5) * (Err / 5));
			} else {
				A = Math.exp(Math.log(0.5) * (Err / 20));
			}
			accuracyList.add(A);
		}
		return StatisticsUtil.mean(accuracyList);
	}
	
}
