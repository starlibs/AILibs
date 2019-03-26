package jaicore.search.algorithms.standard.lds;

import jaicore.search.algorithms.GraphSearchTester;
import jaicore.search.core.interfaces.IGraphSearch;
import jaicore.search.probleminputs.GraphSearchInput;
import jaicore.search.probleminputs.GraphSearchWithNodeRecommenderInput;

public class LDSTester extends GraphSearchTester {

	@Override
	public <N, A> IGraphSearch<?, ?, N, A> getSearchAlgorithm(final GraphSearchInput<N, A> problem) {
		GraphSearchWithNodeRecommenderInput<N, A> transformedProblem = new GraphSearchWithNodeRecommenderInput<>(problem.getGraphGenerator(), (n1, n2) -> 0);
		return new LimitedDiscrepancySearch<>(transformedProblem);
	}

}
