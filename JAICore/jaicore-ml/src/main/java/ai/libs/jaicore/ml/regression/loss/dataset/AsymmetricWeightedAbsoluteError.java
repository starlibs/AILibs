package ai.libs.jaicore.ml.regression.loss.dataset;

import java.util.ArrayList;
import java.util.List;

import ai.libs.jaicore.basic.StatisticsUtil;

public class AsymmetricWeightedAbsoluteError extends ARegressionMeasure {

	private double weightA = 1;
	private double weightB = 1;

	public AsymmetricWeightedAbsoluteError(final double weightA, final double weightB) {
		this.weightA = weightA;
		this.weightB = weightB;
	}

	@Override
	public double loss(final List<? extends Double> expected, final List<? extends Double> actual) {
		List<Double> errors = new ArrayList<>();
		for (int i = 0; i < expected.size(); i++) {
			double d = expected.get(i) - actual.get(i);
			Double error = 0d;
			if (d <= 0) {
				error = -this.weightA * d;
			} else {
				error = this.weightB * d;
			}
			errors.add(error);
		}
		return StatisticsUtil.mean(errors);
	}
}