package ai.libs.jaicore.ml.regression.loss.dataset;

import java.util.List;

import org.api4.java.ai.ml.regression.evaluation.IRegressionPrediction;

public abstract class AUnboundedRegressionMeasure extends ARegressionMeasure {

	@Override
	public double loss(final List<? extends Double> expected, final List<? extends IRegressionPrediction> predicted) {
		return -this.score(expected, predicted);
	}

	@Override
	public double score(final List<? extends Double> expected, final List<? extends IRegressionPrediction> predicted) {
		return -this.loss(expected, predicted);
	}
}
