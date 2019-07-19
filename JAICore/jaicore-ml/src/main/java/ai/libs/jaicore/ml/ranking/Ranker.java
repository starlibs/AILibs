package ai.libs.jaicore.ml.ranking;

import org.api4.java.ai.ml.algorithm.TrainingException;

import ai.libs.jaicore.ml.ranking.clusterbased.customdatatypes.Ranking;

public interface Ranker<S,P> {
	public void buildRanker() throws TrainingException;

	public Ranking<S> getRanking(P problem);
}
