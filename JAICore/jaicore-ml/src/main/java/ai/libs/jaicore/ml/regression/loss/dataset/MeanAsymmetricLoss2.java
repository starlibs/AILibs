package ai.libs.jaicore.ml.regression.loss.dataset;

import java.util.ArrayList;
import java.util.List;

import ai.libs.jaicore.basic.StatisticsUtil;

public class MeanAsymmetricLoss2 extends ARegressionMeasure {

	private double dividerUnderestimation = 10;
	private double dividerOverestimation = 13;

	public MeanAsymmetricLoss2() {
	}

	public MeanAsymmetricLoss2(final double dividerUnderestimation, final double dividerOverestimation) {
		this.dividerUnderestimation = dividerUnderestimation;
		this.dividerOverestimation = dividerOverestimation;
	}

	@Override
	public double score(final List<? extends Double> expected, final List<? extends Double> predicted) {
		this.checkConsistency(expected, predicted);
		List<Double> losses = new ArrayList<>();
		for (int i = 0; i < expected.size(); i++) {
			Double difference = predicted.get(i) - expected.get(i);
			double loss;
			if (difference < 0) {
				loss = Math.exp(-(difference / this.dividerUnderestimation)) - 1;
			} else {
				loss = Math.exp(difference / this.dividerOverestimation) - 1;
			}
			losses.add(loss);
		}
		return StatisticsUtil.mean(losses);
	}

}
