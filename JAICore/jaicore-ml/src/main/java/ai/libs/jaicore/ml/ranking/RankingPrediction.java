package ai.libs.jaicore.ml.ranking;

import org.api4.java.ai.ml.ranking.dataset.IRanking;

import ai.libs.jaicore.ml.core.evaluation.Prediction;

public class RankingPrediction extends Prediction implements IRankingPrediction {

	public RankingPrediction(final IRanking<?> predicted) {
		super(predicted);
	}

	@Override
	public IRanking<?> getPrediction() {
		return (IRanking<?>) super.getPrediction();
	}

}
