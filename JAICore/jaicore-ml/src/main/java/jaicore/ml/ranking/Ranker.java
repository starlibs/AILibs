package jaicore.ml.ranking;

import jaicore.ml.ranking.clusterbased.CustomDataTypes.Ranking;

public interface Ranker<S,P> {
	public void bulidRanker();

	public Ranking<S> getRanking(P problem);
}
