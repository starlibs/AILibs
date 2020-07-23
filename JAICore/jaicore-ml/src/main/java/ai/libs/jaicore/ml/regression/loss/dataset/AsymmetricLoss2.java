package ai.libs.jaicore.ml.regression.loss.dataset;

import java.util.List;

import org.api4.java.ai.ml.regression.evaluation.IRegressionPrediction;

public class AsymmetricLoss2 extends ARegressionMeasure {

	private double dividerUnderestimation = 10;
	private double dividerOverestimation = 13;

	public AsymmetricLoss2() {
	}

	public AsymmetricLoss2(final double dividerUnderestimation, final double dividerOverestimation) {
		this.dividerUnderestimation = dividerUnderestimation;
		this.dividerOverestimation = dividerOverestimation;
	}

	@Override
	public double score(final List<? extends Double> expected, final List<? extends IRegressionPrediction> predicted) {
		this.checkConsistency(expected, predicted);
		double loss = 0;
		for (int i = 0; i < expected.size(); i++) {
			Double difference = predicted.get(i).getPrediction() - expected.get(i);
			if (difference < 0) {
				loss += Math.exp(-(difference / this.dividerUnderestimation)) - 1;
			} else {
				loss += Math.exp(difference / this.dividerOverestimation) - 1;
			}
		}
		return loss;
	}

}
