package jaicore.ml.dyadranking.search;

import java.io.IOException;

import jaicore.search.algorithms.standard.bestfirst.BestFirst;
import jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;

public class RandomlyRankedNodeQueueConfig<T> extends ADyadRankedNodeQueueConfig<T> {

	private int seed;

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
