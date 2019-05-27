package jaicore.ml.ranking;

import jaicore.ml.core.exception.TrainingException;
import jaicore.ml.ranking.clusterbased.customdatatypes.Ranking;

public interface Ranker<S,P> {
	public void buildRanker() throws TrainingException;

	public Ranking<S> getRanking(P problem);
}
