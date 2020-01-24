package ai.libs.jaicore.search.algorithms.standard.random;

import org.api4.java.ai.graphsearch.problem.IPathSearch;

import ai.libs.jaicore.search.algorithms.GraphSearchSolutionIteratorTester;
import ai.libs.jaicore.search.probleminputs.GraphSearchInput;

public class RandomSearchTester extends GraphSearchSolutionIteratorTester {

	@Override
	public <N, A> IPathSearch<?, ?, N, A> getSearchAlgorithm(final GraphSearchInput<N, A> problem) {
		return new RandomSearch<>(problem);
	}
}
