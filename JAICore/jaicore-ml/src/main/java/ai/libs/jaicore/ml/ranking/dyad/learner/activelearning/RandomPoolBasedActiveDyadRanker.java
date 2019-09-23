package ai.libs.jaicore.ml.ranking.dyad.learner.activelearning;

import ai.libs.jaicore.ml.ranking.dyad.learner.algorithm.PLNetDyadRanker;

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
