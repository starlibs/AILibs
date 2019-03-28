package jaicore.search.algorithms.standard.random;

import jaicore.search.core.interfaces.StandardORGraphSearchFactory;
import jaicore.search.model.other.SearchGraphPath;
import jaicore.search.probleminputs.GraphSearchInput;

public class RandomSearchFactory<N, A> extends StandardORGraphSearchFactory<GraphSearchInput<N, A>, SearchGraphPath<N, A>,N, A, Double> {

	private String loggerName;
	private int seed;

	public RandomSearchFactory() {
		super();
	}

	@Override
	public RandomSearch<N, A> getAlgorithm() {
		if (this.getInput().getGraphGenerator() == null) {
			throw new IllegalStateException("Cannot produce RandomSearch searches before the graph generator is set in the problem.");
		}
		return this.getAlgorithm(this.getInput());
	}

	@Override
	public RandomSearch<N, A> getAlgorithm(final GraphSearchInput<N, A> input) {
		return new RandomSearch<>(input, this.seed);
	}

	public int getSeed() {
		return this.seed;
	}

	public void setSeed(final int seed) {
		this.seed = seed;
	}

	public String getLoggerName() {
		return this.loggerName;
	}

	public void setLoggerName(final String loggerName) {
		this.loggerName = loggerName;
	}
}
