package jaicore.Ranker;

import jaicore.CustomDataTypes.Ranking;

public interface Ranker<S,P> {
	public void bulidRanker();

	public Ranking<S> getRanking(P problem);
}
