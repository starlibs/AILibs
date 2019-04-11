package jaicore.search.algorithms.standard.dfs;

import jaicore.search.algorithms.GraphSearchTester;
import jaicore.search.core.interfaces.IGraphSearch;
import jaicore.search.probleminputs.GraphSearchInput;

public class DepthFirstSearchTester extends GraphSearchTester {

	@Override
	public <N, A> IGraphSearch<?, ?, N, A> getSearchAlgorithm(GraphSearchInput<N, A> problem) {
		return new DepthFirstSearch<>(problem);
	}
}
