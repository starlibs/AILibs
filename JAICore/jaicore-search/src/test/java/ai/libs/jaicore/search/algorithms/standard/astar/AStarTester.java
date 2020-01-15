package ai.libs.jaicore.search.algorithms.standard.astar;

import org.api4.java.ai.graphsearch.problem.IPathSearch;

import ai.libs.jaicore.search.algorithms.GraphSearchSolutionIteratorTester;
import ai.libs.jaicore.search.probleminputs.GraphSearchInput;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithNumberBasedAdditivePathEvaluation;

public class AStarTester extends GraphSearchSolutionIteratorTester {

	@Override
	public <N, A> IPathSearch<?, ?, N, A> getSearchAlgorithm(final GraphSearchInput<N, A> problem) {
		GraphSearchWithNumberBasedAdditivePathEvaluation<N, A> transformedInput = new GraphSearchWithNumberBasedAdditivePathEvaluation<>(problem, (n1,n2) -> 1.0, n -> 0.0);
		return new AStar<>(transformedInput);
	}
}
