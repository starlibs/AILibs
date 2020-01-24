package ai.libs.jaicore.search.algorithms.standard.dfs;

import org.api4.java.ai.graphsearch.problem.IPathSearchInput;

import ai.libs.jaicore.search.core.interfaces.StandardORGraphSearchFactory;
import ai.libs.jaicore.search.model.other.SearchGraphPath;

public class DepthFirstSearchFactory<N, A> extends StandardORGraphSearchFactory<IPathSearchInput<N, A>, SearchGraphPath<N, A>,N, A, Double, DepthFirstSearch<N, A>> {

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
	public DepthFirstSearch<N, A> getAlgorithm(final IPathSearchInput<N, A> input) {
		return new DepthFirstSearch<>(input);
	}

	public String getLoggerName() {
		return this.loggerName;
	}

	public void setLoggerName(final String loggerName) {
		this.loggerName = loggerName;
	}
}
