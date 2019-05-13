package ai.libs.jaicore.search.algorithms.standard.bestfirst;

import ai.libs.jaicore.search.algorithms.GraphSearchWithSubPathEvaluationUninformedTester;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.StandardBestFirst;
import ai.libs.jaicore.search.core.interfaces.IGraphSearch;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;

public class UninformedBestFirstTester extends GraphSearchWithSubPathEvaluationUninformedTester {

	@Override
	public <N,A> IGraphSearch<?, ?, N, A> getSearchAlgorithm(GraphSearchWithSubpathEvaluationsInput<N, A, Double> problem) {
		return new StandardBestFirst<>(problem);
	}
}
