package jaicore.search.algorithms.standard.astar;

import jaicore.search.algorithms.GraphSearchTester;
import jaicore.search.core.interfaces.IGraphSearch;
import jaicore.search.probleminputs.GraphSearchInput;
import jaicore.search.probleminputs.GraphSearchWithNumberBasedAdditivePathEvaluation;

public class AStarTester extends GraphSearchTester {

	@Override
	public <N, A> IGraphSearch<?, ?, N, A> getSearchAlgorithm(GraphSearchInput<N, A> problem) {
		GraphSearchWithNumberBasedAdditivePathEvaluation<N, A> transformedInput = new GraphSearchWithNumberBasedAdditivePathEvaluation<N, A>(problem.getGraphGenerator(), (n1,n2) -> 1.0, n -> 0.0);
		return new AStar<>(transformedInput);
	}
}
