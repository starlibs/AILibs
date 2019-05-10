package ai.libs.jaicore.search.algorithms.standard.lds;

import ai.libs.jaicore.search.algorithms.GraphSearchSolutionIteratorTester;
import jaicore.search.algorithms.standard.lds.BestFirstLimitedDiscrepancySearch;
import jaicore.search.core.interfaces.IGraphSearch;
import jaicore.search.probleminputs.GraphSearchInput;
import jaicore.search.probleminputs.GraphSearchWithNodeRecommenderInput;

public class BestFirstLimitedDiscrepancySearchTester extends GraphSearchSolutionIteratorTester {

	@Override
	public <N, A> IGraphSearch<?, ?, N, A> getSearchAlgorithm(GraphSearchInput<N, A> problem) {
		GraphSearchWithNodeRecommenderInput<N, A> transformedProblem = new GraphSearchWithNodeRecommenderInput<>(problem.getGraphGenerator(), (n1, n2) -> 0);
		return new BestFirstLimitedDiscrepancySearch<>(transformedProblem);
	}

}
