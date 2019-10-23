package ai.libs.jaicore.ml.ranking;

import java.util.List;
import java.util.stream.Collectors;

import org.api4.java.ai.ml.core.evaluation.IPrediction;

import ai.libs.jaicore.ml.core.evaluation.PredictionBatch;

public class RankingPredictionBatch extends PredictionBatch implements IRankingPredictionBatch {

	public RankingPredictionBatch(final List<IRankingPrediction> predictionBatch) {
		super(predictionBatch.stream().map(x -> (IPrediction) x).collect(Collectors.toList()));
	}

	public RankingPredictionBatch(final IRankingPrediction[] predictionBatch) {
		super(predictionBatch);
	}

	@Override
	public IRankingPrediction get(final int pos) {
		return (IRankingPrediction) super.get(pos);
	}

}
