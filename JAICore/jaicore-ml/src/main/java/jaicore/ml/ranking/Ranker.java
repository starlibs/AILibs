package jaicore.ml.ranking;

import jaicore.ml.ranking.clusterbased.customdatatypes.Ranking;

public interface Ranker<S,P> {
	public void buildRanker();

	public Ranking<S> getRanking(P problem);
}
