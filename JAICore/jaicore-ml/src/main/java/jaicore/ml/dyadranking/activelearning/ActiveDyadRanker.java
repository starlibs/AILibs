package jaicore.ml.dyadranking.activelearning;

import jaicore.ml.dyadranking.algorithm.PLNetDyadRanker;

public abstract class ActiveDyadRanker {

	/**
	 * Train the ranker.
	 * @param numberOfQueries Number of queries the ranker conducts.
	 */

	protected PLNetDyadRanker ranker;
	protected IDyadRankingPoolProvider poolProvider;

	
	public ActiveDyadRanker(PLNetDyadRanker ranker, IDyadRankingPoolProvider poolProvider) {
		this.ranker = ranker;
		this.poolProvider = poolProvider;
	}
	
	public abstract void activelyTrain(int numberOfQueries);
}
