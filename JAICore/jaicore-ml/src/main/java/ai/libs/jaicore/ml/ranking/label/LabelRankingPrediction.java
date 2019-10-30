package ai.libs.jaicore.ml.ranking.label;

import org.api4.java.ai.ml.ranking.IRanking;
import org.api4.java.ai.ml.ranking.label.learner.ILabelRankingPrediction;

import ai.libs.jaicore.ml.core.evaluation.Prediction;

public class LabelRankingPrediction extends Prediction implements ILabelRankingPrediction {

	public LabelRankingPrediction(final IRanking<String> prediction) {
		super(prediction);
	}

	@SuppressWarnings("unchecked")
	@Override
	public IRanking<String> getPrediction() {
		return (IRanking<String>) super.getPrediction();
	}

}
