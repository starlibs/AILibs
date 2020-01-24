package ai.libs.jaicore.ml.ranking.dyad.learner.activelearning;

import org.api4.java.ai.ml.core.exception.TrainingException;

import ai.libs.jaicore.ml.ranking.dyad.learner.algorithm.PLNetDyadRanker;

/**
 * Abstract description of a pool-based active learning strategy for dyad
 * ranking.
 *
 * @author Jonas Hanselle
 *
 */
public abstract class ActiveDyadRanker {

	protected PLNetDyadRanker ranker;
	protected IDyadRankingPoolProvider poolProvider;

	/**
	 *
	 * @param ranker       The {@link PLNetDyadRanker} that is actively trained.
	 * @param poolProvider The {@link IDyadRankingPoolProvider} that provides a pool
	 *                     for pool-based selective sampling
	 */
	public ActiveDyadRanker(final PLNetDyadRanker ranker, final IDyadRankingPoolProvider poolProvider) {
		this.ranker = ranker;
		this.poolProvider = poolProvider;
	}

	/**
	 * Actively trains the ranker for a certain number of queries.
	 *
	 * @param numberOfQueries Number of queries the ranker conducts
	 * @throws TrainingException
	 * @throws InterruptedException
	 */
	public void activelyTrain(final int numberOfQueries) throws TrainingException, InterruptedException {
		for (int i = 0; i < numberOfQueries; i++) {
			this.activelyTrainWithOneInstance();
		}
	}

	public abstract void activelyTrainWithOneInstance() throws TrainingException, InterruptedException;

	public PLNetDyadRanker getRanker() {
		return this.ranker;
	}

	public void setRanker(final PLNetDyadRanker ranker) {
		this.ranker = ranker;
	}

	public IDyadRankingPoolProvider getPoolProvider() {
		return this.poolProvider;
	}

	public void setPoolProvider(final IDyadRankingPoolProvider poolProvider) {
		this.poolProvider = poolProvider;
	}
}
