package ai.libs.jaicore.search.algorithms.standard.mcts;

import org.api4.java.ai.graphsearch.problem.IGraphSearch;

import ai.libs.jaicore.search.algorithms.GraphSearchSolutionIteratorTester;
import ai.libs.jaicore.search.model.other.AgnosticPathEvaluator;
import ai.libs.jaicore.search.probleminputs.GraphSearchInput;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;

public class MCTSTester extends GraphSearchSolutionIteratorTester {

	@Override
	public <N, A> IGraphSearch<?, ?, N, A> getSearchAlgorithm(final GraphSearchInput<N, A> problem) {
		UCTPathSearchFactory<N, A> factory = new UCTPathSearchFactory<>();
		GraphSearchWithPathEvaluationsInput<N, A, Double> newProblem = new GraphSearchWithPathEvaluationsInput<>(problem, new AgnosticPathEvaluator<>());
		factory.setProblemInput(newProblem);
		factory.setEvaluationFailurePenalty(0.0);
		factory.setForbidDoublePaths(true);
		return factory.getAlgorithm();
	}

}