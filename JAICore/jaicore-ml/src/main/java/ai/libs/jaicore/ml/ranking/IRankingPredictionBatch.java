package ai.libs.jaicore.ml.ranking;

import org.api4.java.ai.ml.core.learner.algorithm.IPredictionBatch;

public interface IRankingPredictionBatch extends IPredictionBatch {

	@Override
	public IRankingPrediction get(int pos);

}
