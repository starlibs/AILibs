package ai.libs.jaicore.search.algorithms.standard.astar;

import ai.libs.jaicore.search.algorithms.GraphSearchSolutionIteratorTester;
import ai.libs.jaicore.search.algorithms.standard.astar.AStar;
import ai.libs.jaicore.search.core.interfaces.IGraphSearch;
import ai.libs.jaicore.search.probleminputs.GraphSearchInput;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithNumberBasedAdditivePathEvaluation;

public class AStarTester extends GraphSearchSolutionIteratorTester {

	@Override
	public <N, A> IGraphSearch<?, ?, N, A> getSearchAlgorithm(GraphSearchInput<N, A> problem) {
		GraphSearchWithNumberBasedAdditivePathEvaluation<N, A> transformedInput = new GraphSearchWithNumberBasedAdditivePathEvaluation<N, A>(problem.getGraphGenerator(), (n1,n2) -> 1.0, n -> 0.0);
		return new AStar<>(transformedInput);
	}
}
