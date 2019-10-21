package ai.libs.jaicore.ml.classification.singlelabel;

import org.api4.java.ai.ml.classification.singlelabel.learner.ISingleLabelClassificationPrediction;

import ai.libs.jaicore.ml.core.evaluation.Prediction;

public class SingleLabelClassificationPrediction extends Prediction implements ISingleLabelClassificationPrediction {

	public SingleLabelClassificationPrediction(final int predicted) {
		super(predicted);
	}

	@Override
	public int getIntPrediction() {
		return (int)super.getPrediction();
	}

	@Override
	public Integer getPrediction() {
		return this.getIntPrediction();
	}

	@Override
	public int getLabelWithHighestProbability() {
		throw new UnsupportedOperationException("This is not a probabilistic prediction");
	}

	@Override
	public double[] getClassDistribution() {
		throw new UnsupportedOperationException("This is not a probabilistic prediction");
	}

	@Override
	public double getProbabilityOfLabel(final int label) {
		throw new UnsupportedOperationException("This is not a probabilistic prediction");
	}
}
