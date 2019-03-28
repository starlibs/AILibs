package jaicore.ml.dyadranking.search;

import java.io.IOException;

import jaicore.search.algorithms.standard.bestfirst.BestFirst;
import jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;

/**
 * Configuration for a {@link RandomlyRankedNodeQueue}
 * 
 * @author Helena Graf
 *
 * @param <T>
 */
public class RandomlyRankedNodeQueueConfig<T> extends ADyadRankedNodeQueueConfig<T> {
	
	/**
	 * random seed for randomizing the insertion of pipelines
	 */
	private int seed;

	/**
	 * Construct a new config with the given seed.
	 * 
	 * @param seed the seed to use
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public RandomlyRankedNodeQueueConfig(int seed) throws IOException, ClassNotFoundException {
		super();
		this.seed = seed;
	}

	@Override
	public void configureBestFirst(
			BestFirst<GraphSearchWithSubpathEvaluationsInput<T, String, Double>, T, String, Double> bestFirst) {
		bestFirst.setOpen(new RandomlyRankedNodeQueue<>(seed));
	}
}
