package ai.libs.jaicore.search.algorithms.standard.dfs;

import org.api4.java.ai.graphsearch.problem.IPathSearch;
import org.api4.java.ai.graphsearch.problem.IPathSearchInput;

import ai.libs.jaicore.search.algorithms.GraphSearchSolutionIteratorTester;

public class DepthFirstSearchTester extends GraphSearchSolutionIteratorTester {

	@Override
	public <N, A> IPathSearch<?, ?, N, A> getSearchAlgorithm(final IPathSearchInput<N, A> problem) {
		return new DepthFirstSearch<>(problem);
	}
}
