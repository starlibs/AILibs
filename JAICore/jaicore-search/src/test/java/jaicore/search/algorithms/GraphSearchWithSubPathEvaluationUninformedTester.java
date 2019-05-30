package jaicore.search.algorithms;

import jaicore.search.core.interfaces.IGraphSearch;
import jaicore.search.probleminputs.GraphSearchInput;
import jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;

public abstract class GraphSearchWithSubPathEvaluationUninformedTester extends GraphSearchSolutionIteratorTester {

	@SuppressWarnings("unchecked")
	@Override
	public final <N, A> IGraphSearch<?, ?, N, A> getSearchAlgorithm(final GraphSearchInput<N, A> problem) {
		if (problem instanceof GraphSearchWithSubpathEvaluationsInput) {
			return this.getSearchAlgorithm((GraphSearchWithSubpathEvaluationsInput<N, A, Double>)problem);
		}
		else {
			return this.getSearchAlgorithm(new GraphSearchWithSubpathEvaluationsInput<>(problem.getGraphGenerator(), n -> 0.0));
		}
	}

	public abstract <N, A> IGraphSearch<?, ?, N, A> getSearchAlgorithm(GraphSearchWithSubpathEvaluationsInput<N, A, Double> problem);
}
