package jaicore.search.algorithms.standard.random;

import jaicore.search.core.interfaces.StandardORGraphSearchFactory;
import jaicore.search.model.probleminputs.GraphSearchInput;

public class RandomSearchFactory<N, A> extends StandardORGraphSearchFactory<GraphSearchInput<N, A>, Object,N, A, Double, N, A> {

	private int timeoutForFInMS;
	private String loggerName;
	private int seed;

	public RandomSearchFactory() {
		super();
	}

	public RandomSearchFactory(final int timeoutForFInMS) {
		this();
		if (timeoutForFInMS > 0) {
			this.timeoutForFInMS = timeoutForFInMS;
		}
	}

	@Override
	public RandomSearch<N, A> getAlgorithm() {
		if (getProblemInput().getGraphGenerator() == null)
			throw new IllegalStateException("Cannot produce RandomSearch searches before the graph generator is set in the problem.");
		RandomSearch<N, A> search = new RandomSearch<>(getProblemInput(), seed);
		return search;
	}

	public int getSeed() {
		return seed;
	}

	public void setSeed(int seed) {
		this.seed = seed;
	}

	public String getLoggerName() {
		return loggerName;
	}

	public void setLoggerName(String loggerName) {
		this.loggerName = loggerName;
	}
}
