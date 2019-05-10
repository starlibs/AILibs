package ai.libs.jaicore.search.algorithms.standard.dfs;

import ai.libs.jaicore.search.algorithms.GraphSearchSolutionIteratorTester;
import jaicore.search.algorithms.standard.dfs.DepthFirstSearch;
import jaicore.search.core.interfaces.IGraphSearch;
import jaicore.search.probleminputs.GraphSearchInput;

public class DepthFirstSearchTester extends GraphSearchSolutionIteratorTester {

	@Override
	public <N, A> IGraphSearch<?, ?, N, A> getSearchAlgorithm(final GraphSearchInput<N, A> problem) {
		return new DepthFirstSearch<>(problem);
	}
}
