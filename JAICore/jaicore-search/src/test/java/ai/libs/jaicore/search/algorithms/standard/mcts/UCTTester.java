package ai.libs.jaicore.search.algorithms.standard.mcts;

import org.api4.java.ai.graphsearch.problem.IPathSearch;
import org.api4.java.ai.graphsearch.problem.IPathSearchWithPathEvaluationsInput;

import ai.libs.jaicore.search.algorithms.GraphSearchSolutionIteratorTester;
import ai.libs.jaicore.search.model.other.AgnosticPathEvaluator;
import ai.libs.jaicore.search.probleminputs.GraphSearchInput;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;

public class UCTTester extends GraphSearchSolutionIteratorTester {

	@Override
	public <N, A> IPathSearch<?, ?, N, A> getSearchAlgorithm(final GraphSearchInput<N, A> problem) {
		UCTPathSearchFactory<IPathSearchWithPathEvaluationsInput<N, A, Double>, N, A> factory = new UCTPathSearchFactory<>();
		GraphSearchWithPathEvaluationsInput<N, A, Double> newProblem = new GraphSearchWithPathEvaluationsInput<>(problem, new AgnosticPathEvaluator<>());
		factory.setProblemInput(newProblem);
		factory.setEvaluationFailurePenalty(0.0);
		factory.setForbidDoublePaths(true);
		return factory.getAlgorithm();
	}

}