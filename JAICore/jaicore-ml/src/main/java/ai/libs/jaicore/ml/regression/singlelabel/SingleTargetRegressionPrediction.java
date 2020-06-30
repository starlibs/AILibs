package ai.libs.jaicore.ml.regression.singlelabel;

import org.api4.java.ai.ml.regression.evaluation.IRegressionPrediction;

import ai.libs.jaicore.ml.core.evaluation.Prediction;

public class SingleTargetRegressionPrediction extends Prediction implements IRegressionPrediction {

	public SingleTargetRegressionPrediction(final Object predicted) {
		super(predicted);
	}

	@Override
	public Double getPrediction() {
		return this.getDoublePrediction();
	}

	@Override
	public double getDoublePrediction() {
		return (double) super.getPrediction();
	}

}
