package ai.libs.jaicore.search.algorithms.standard.lds;

import ai.libs.jaicore.search.algorithms.GeneralGraphSearchAlgorithmTester;
import ai.libs.jaicore.search.algorithms.standard.lds.LimitedDiscrepancySearch;
import ai.libs.jaicore.search.core.interfaces.IGraphSearch;
import ai.libs.jaicore.search.probleminputs.GraphSearchInput;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithNodeRecommenderInput;

public class LDSTester extends GeneralGraphSearchAlgorithmTester {

	@Override
	public <N, A> IGraphSearch<?, ?, N, A> getSearchAlgorithm(final GraphSearchInput<N, A> problem) {
		GraphSearchWithNodeRecommenderInput<N, A> transformedProblem = new GraphSearchWithNodeRecommenderInput<>(problem.getGraphGenerator(), (n1, n2) -> 0);
		return new LimitedDiscrepancySearch<>(transformedProblem);
	}
}
