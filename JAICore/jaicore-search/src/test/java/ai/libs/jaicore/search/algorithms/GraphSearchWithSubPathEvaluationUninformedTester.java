package ai.libs.jaicore.search.algorithms;

import org.api4.java.ai.graphsearch.problem.IPathSearch;

import ai.libs.jaicore.search.probleminputs.GraphSearchInput;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;

public abstract class GraphSearchWithSubPathEvaluationUninformedTester extends GraphSearchSolutionIteratorTester {

	@SuppressWarnings("unchecked")
	@Override
	public final <N, A> IPathSearch<?, ?, N, A> getSearchAlgorithm(final GraphSearchInput<N, A> problem) {
		if (problem instanceof GraphSearchWithSubpathEvaluationsInput) {
			return this.getSearchAlgorithm((GraphSearchWithSubpathEvaluationsInput<N, A, Double>)problem);
		}
		else {
			return this.getSearchAlgorithm(new GraphSearchWithSubpathEvaluationsInput<>(problem, n -> 0.0));
		}
	}

	public abstract <N, A> IPathSearch<?, ?, N, A> getSearchAlgorithm(GraphSearchWithSubpathEvaluationsInput<N, A, Double> problem);
}
