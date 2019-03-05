package jaicore.search.algorithms;

import jaicore.search.core.interfaces.IGraphSearch;
import jaicore.search.probleminputs.GraphSearchInput;
import jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;

public abstract class GraphSearchWithSubPathEvaluationUninformedTester extends GraphSearchTester {

	@Override
	public final <N, A> IGraphSearch<?, ?, N, A> getSearchAlgorithm(final GraphSearchInput<N, A> problem) {
		GraphSearchWithSubpathEvaluationsInput<N, A, Double> transformed = new GraphSearchWithSubpathEvaluationsInput<>(problem.getGraphGenerator(), n -> 0.0);
		return this.getSearchAlgorithm(transformed);
	}

	public abstract <N, A> IGraphSearch<?, ?, N, A> getSearchAlgorithm(GraphSearchWithSubpathEvaluationsInput<N, A, Double> problem);
}
