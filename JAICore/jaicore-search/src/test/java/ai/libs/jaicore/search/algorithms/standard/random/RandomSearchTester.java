package ai.libs.jaicore.search.algorithms.standard.random;

import ai.libs.jaicore.search.algorithms.GraphSearchSolutionIteratorTester;
import jaicore.search.algorithms.standard.random.RandomSearch;
import jaicore.search.core.interfaces.IGraphSearch;
import jaicore.search.probleminputs.GraphSearchInput;

public class RandomSearchTester extends GraphSearchSolutionIteratorTester {

	@Override
	public <N, A> IGraphSearch<?, ?, N, A> getSearchAlgorithm(final GraphSearchInput<N, A> problem) {
		return new RandomSearch<>(problem);
	}
}
