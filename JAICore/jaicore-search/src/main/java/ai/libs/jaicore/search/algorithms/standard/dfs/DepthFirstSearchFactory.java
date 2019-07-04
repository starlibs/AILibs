package ai.libs.jaicore.search.algorithms.standard.dfs;

import ai.libs.jaicore.search.core.interfaces.StandardORGraphSearchFactory;
import ai.libs.jaicore.search.model.other.SearchGraphPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchInput;

public class DepthFirstSearchFactory<N, A> extends StandardORGraphSearchFactory<GraphSearchInput<N, A>, SearchGraphPath<N, A>,N, A, Double> {

	private String loggerName;

	public DepthFirstSearchFactory() {
		super();
	}

	@Override
	public DepthFirstSearch<N, A> getAlgorithm() {
		if (this.getInput().getGraphGenerator() == null) {
			throw new IllegalStateException("Cannot produce RandomSearch searches before the graph generator is set in the problem.");
		}
		return this.getAlgorithm(this.getInput());
	}

	@Override
	public DepthFirstSearch<N, A> getAlgorithm(final GraphSearchInput<N, A> input) {
		return new DepthFirstSearch<>(input);
	}

	public String getLoggerName() {
		return this.loggerName;
	}

	public void setLoggerName(final String loggerName) {
		this.loggerName = loggerName;
	}
}
