package ai.libs.jaicore.search.algorithms.standard.lds;

import org.api4.java.ai.graphsearch.problem.IPathSearch;

import ai.libs.jaicore.search.algorithms.GeneralGraphSearchAlgorithmTester;
import ai.libs.jaicore.search.probleminputs.GraphSearchInput;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithNodeRecommenderInput;

public class LDSTester extends GeneralGraphSearchAlgorithmTester {

	@Override
	public <N, A> IPathSearch<?, ?, N, A> getSearchAlgorithm(final GraphSearchInput<N, A> problem) {
		GraphSearchWithNodeRecommenderInput<N, A> transformedProblem = new GraphSearchWithNodeRecommenderInput<>(problem, (n1, n2) -> 0);
		return new LimitedDiscrepancySearch<>(transformedProblem);
	}
}
