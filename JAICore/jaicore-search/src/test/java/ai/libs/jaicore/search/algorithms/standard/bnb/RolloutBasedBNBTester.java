package ai.libs.jaicore.search.algorithms.standard.bnb;

import org.api4.java.ai.graphsearch.problem.IPathSearch;
import org.api4.java.ai.graphsearch.problem.IPathSearchInput;

import ai.libs.jaicore.search.algorithms.GraphSearchSolutionIteratorTester;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;

public class RolloutBasedBNBTester extends GraphSearchSolutionIteratorTester {

	@Override
	public <N,A> IPathSearch<?, ?, N, A> getSearchAlgorithm(final IPathSearchInput<N, A> problem) {
		GraphSearchWithPathEvaluationsInput<N, A, Double> transformed = new GraphSearchWithSubpathEvaluationsInput<>(problem, n -> 1.0);
		return new BranchAndBound<>(transformed, n -> 0.0, 1);
	}
}
