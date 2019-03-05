package jaicore.search.algorithms.standard.bestfirst;

import jaicore.search.algorithms.GraphSearchWithSubPathEvaluationUninformedTester;
import jaicore.search.core.interfaces.IGraphSearch;
import jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;

public class UninformedBestFirstTester extends GraphSearchWithSubPathEvaluationUninformedTester {

	@Override
	public <N,A> IGraphSearch<?, ?, N, A> getSearchAlgorithm(GraphSearchWithSubpathEvaluationsInput<N, A, Double> problem) {
		return new StandardBestFirst<>(problem);
	}
}
