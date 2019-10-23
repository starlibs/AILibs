package ai.libs.jaicore.ml.ranking;

import java.util.List;

import org.api4.java.ai.ml.core.evaluation.IPredictionBatch;

public interface IRankingPredictionBatch extends IPredictionBatch {

	@Override
	public List<IRankingPrediction> getPredictions();

}
