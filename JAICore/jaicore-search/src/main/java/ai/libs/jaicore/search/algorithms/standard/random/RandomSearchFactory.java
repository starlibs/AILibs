package ai.libs.jaicore.search.algorithms.standard.random;

import org.api4.java.ai.graphsearch.problem.IPathSearchInput;

import ai.libs.jaicore.search.core.interfaces.StandardORGraphSearchFactory;
import ai.libs.jaicore.search.model.other.SearchGraphPath;

public class RandomSearchFactory<N, A> extends StandardORGraphSearchFactory<IPathSearchInput<N, A>, SearchGraphPath<N, A>,N, A, Double, RandomSearch<N, A>> {

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
	public RandomSearch<N, A> getAlgorithm(final IPathSearchInput<N, A> input) {
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
