package ai.libs.jaicore.ml.ranking;

import java.util.List;
import java.util.stream.Collectors;

import org.api4.java.ai.ml.core.evaluation.IPrediction;
import org.api4.java.ai.ml.ranking.IRanking;
import org.api4.java.ai.ml.ranking.IRankingPredictionBatch;

import ai.libs.jaicore.ml.core.evaluation.PredictionBatch;

public class RankingPredictionBatch extends PredictionBatch implements IRankingPredictionBatch {

	public RankingPredictionBatch(final List<IRanking<?>> predictionBatch) {
		super(predictionBatch.stream().map(x -> (IPrediction) x).collect(Collectors.toList()));
	}

	public RankingPredictionBatch(final IRanking<?>[] predictionBatch) {
		super(predictionBatch);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<IRanking<?>> getPredictions() {
		return (List<IRanking<?>>) super.getPredictions();
	}

}
