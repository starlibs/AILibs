package jaicore.ml.dyadranking.activelearning;

import jaicore.ml.dyadranking.algorithm.PLNetDyadRanker;

/**
 * A random active dyad ranker. The sampling strategy picks a problem instance
 * at random and then picks two alternatives at random for pairwise comparison.
 * This is repeated for a constant number of times to create a minibatch for
 * updating the ranker.
 * 
 * @author Jonas Hanselle
 *
 */
public class RandomPoolBasedActiveDyadRanker extends ARandomlyInitializingDyadRanker {

	public RandomPoolBasedActiveDyadRanker(PLNetDyadRanker ranker, IDyadRankingPoolProvider poolProvider, int maxBatchSize, int seed) {
		super(ranker, poolProvider, seed, Integer.MAX_VALUE, maxBatchSize);
	}

	@Override
	public void activelyTrainWithOneInstance() {
		
		/* this is never called */
	}
}
