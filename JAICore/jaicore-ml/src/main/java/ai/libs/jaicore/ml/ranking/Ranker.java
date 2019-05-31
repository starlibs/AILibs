package ai.libs.jaicore.ml.ranking;

import ai.libs.jaicore.ml.core.exception.TrainingException;
import ai.libs.jaicore.ml.ranking.clusterbased.customdatatypes.Ranking;

public interface Ranker<S,P> {
	public void buildRanker() throws TrainingException;

	public Ranking<S> getRanking(P problem);
}
