package ai.libs.jaicore.ml.regression.loss.dataset;

import java.util.ArrayList;
import java.util.List;

import org.api4.java.ai.ml.regression.evaluation.IRegressionPrediction;

import ai.libs.jaicore.basic.StatisticsUtil;

public class MeanSquaredPercentageError extends ARegressionMeasure {

	@Override
	public double loss(final List<? extends Double> expected, final List<? extends IRegressionPrediction> predicted) {
		this.checkConsistency(expected, predicted);
		List<Double> errors = new ArrayList<>();
		for (int i = 0; i < expected.size(); i++) {
			Double percentageError = (expected.get(i) - predicted.get(i).getPrediction()) / expected.get(i);
			errors.add(Math.pow(percentageError, 2));
		}
		return StatisticsUtil.mean(errors);
	}

}
