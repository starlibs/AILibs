package jaicore.search.algorithms.standard.random;

import jaicore.search.algorithms.GraphSearchSolutionIteratorTester;
import jaicore.search.core.interfaces.IGraphSearch;
import jaicore.search.probleminputs.GraphSearchInput;

public class RandomSearchTester extends GraphSearchSolutionIteratorTester {

	@Override
	public <N, A> IGraphSearch<?, ?, N, A> getSearchAlgorithm(final GraphSearchInput<N, A> problem) {
		return new RandomSearch<>(problem);
	}
}
