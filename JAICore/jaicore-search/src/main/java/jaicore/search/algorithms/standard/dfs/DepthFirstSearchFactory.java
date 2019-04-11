package jaicore.search.algorithms.standard.dfs;

import jaicore.search.core.interfaces.StandardORGraphSearchFactory;
import jaicore.search.model.other.SearchGraphPath;
import jaicore.search.probleminputs.GraphSearchInput;

public class DepthFirstSearchFactory<I extends GraphSearchInput<N, A>, N, A> extends StandardORGraphSearchFactory<I, SearchGraphPath<N, A>,N, A, Double> {

	private String loggerName;

	public DepthFirstSearchFactory() {
		super();
	}

	@Override
	public DepthFirstSearch<I, N, A> getAlgorithm() {
		if (this.getInput().getGraphGenerator() == null) {
			throw new IllegalStateException("Cannot produce RandomSearch searches before the graph generator is set in the problem.");
		}
		return this.getAlgorithm(this.getInput());
	}

	@Override
	public DepthFirstSearch<I, N, A> getAlgorithm(final I input) {
		return new DepthFirstSearch<>(input);
	}

	public String getLoggerName() {
		return this.loggerName;
	}

	public void setLoggerName(final String loggerName) {
		this.loggerName = loggerName;
	}
}
