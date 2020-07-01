package ai.libs.jaicore.ml.regression.loss.dataset;

import java.util.ArrayList;
import java.util.List;

import ai.libs.jaicore.basic.StatisticsUtil;

public class AsymmetricLoss extends ARegressionMeasure {

	private double dividerOverestimation = 5;
	private double dividerUnderestimation = 20;

	public AsymmetricLoss() {
	}

	public AsymmetricLoss(final double dividerUnderestimation, final double dividerOverestimation) {
		this.dividerOverestimation = dividerUnderestimation;
		this.dividerUnderestimation = dividerOverestimation;
	}

	@Override
	public double score(final List<? extends Double> expected, final List<? extends Double> actual) {
		this.checkConsistency(expected, actual);
		List<Double> accuracyList = new ArrayList<>();
		for (int i = 0; i < expected.size(); i++) {
			Double percentageError = 100 * ((expected.get(i) - actual.get(i)) / expected.get(i));
			Double accuracy;
			if (percentageError <= 0) {
				accuracy = Math.exp(-Math.log(0.5) * (percentageError / this.dividerOverestimation));
			} else {
				accuracy = Math.exp(Math.log(0.5) * (percentageError / this.dividerUnderestimation));
			}
			accuracyList.add(accuracy);
		}
		return StatisticsUtil.mean(accuracyList);
	}

}
