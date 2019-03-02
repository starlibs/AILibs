package jaicore.search.algorithms.standard.awastar;

import jaicore.search.algorithms.GraphSearchWithSubPathEvaluationUninformedTester;
import jaicore.search.core.interfaces.IGraphSearch;
import jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;

public class AwaTester extends GraphSearchWithSubPathEvaluationUninformedTester {

	@Override
	public <N, A> IGraphSearch<?, ?, N, A> getSearchAlgorithm(GraphSearchWithSubpathEvaluationsInput<N, A, Double> problem) {
		 return new AwaStarSearch<>(problem);
	}

}
