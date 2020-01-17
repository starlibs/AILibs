package ai.libs.jaicore.search.algorithms.standard.mcts;

import org.api4.java.ai.graphsearch.problem.IPathSearch;

import ai.libs.jaicore.search.algorithms.GraphSearchSolutionIteratorTester;
import ai.libs.jaicore.search.model.other.AgnosticPathEvaluator;
import ai.libs.jaicore.search.probleminputs.GraphSearchInput;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;

public class SPUCTTester extends GraphSearchSolutionIteratorTester {

	@Override
	public <N, A> IPathSearch<?, ?, N, A> getSearchAlgorithm(final GraphSearchInput<N, A> problem) {
		GraphSearchWithPathEvaluationsInput<N, A, Double> newProblem = new GraphSearchWithPathEvaluationsInput<>(problem, new AgnosticPathEvaluator<>());
		return new SPUCTPathSearch<>(newProblem, 0, 0, Math.sqrt(2), 1);
	}
}