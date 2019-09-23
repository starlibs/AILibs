package ai.libs.jaicore.ml.classification.singlelabel;

import org.api4.java.ai.ml.classification.singlelabel.learner.ISingleLabelClassificationPrediction;

import ai.libs.jaicore.ml.core.evaluation.Prediction;

public class SingleLabelClassificationPrediction extends Prediction implements ISingleLabelClassificationPrediction {

	public SingleLabelClassificationPrediction(final String predicted) {
		super(predicted);
	}

	@Override
	public String getPrediction() {
		return (String) super.getPrediction();
	}

	@Override
	public String getLabelWithHighestProbability() {
		return null;
	}

	@Override
	public double[] getClassDistribution() {
		return null;
	}

	@Override
	public double getProbabilityOfLabel(final String label) {
		return 0;
	}

}
