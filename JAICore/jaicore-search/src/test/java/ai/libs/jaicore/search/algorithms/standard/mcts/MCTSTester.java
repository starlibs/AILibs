package ai.libs.jaicore.search.algorithms.standard.mcts;

import ai.libs.jaicore.search.algorithms.GraphSearchSolutionIteratorTester;
import ai.libs.jaicore.search.algorithms.standard.mcts.UCTFactory;
import ai.libs.jaicore.search.core.interfaces.IGraphSearch;
import ai.libs.jaicore.search.model.other.AgnosticPathEvaluator;
import ai.libs.jaicore.search.probleminputs.GraphSearchInput;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;

public class MCTSTester extends GraphSearchSolutionIteratorTester {

	@Override
	public <N, A> IGraphSearch<?, ?, N, A> getSearchAlgorithm(GraphSearchInput<N, A> problem) {
		UCTFactory<N, A> factory = new UCTFactory<>();
		GraphSearchWithPathEvaluationsInput<N, A, Double> newProblem = new GraphSearchWithPathEvaluationsInput<>(problem.getGraphGenerator(), new AgnosticPathEvaluator<>());
		factory.setProblemInput(newProblem);
		factory.setEvaluationFailurePenalty(0.0);
		factory.setForbidDoublePaths(true);
		return factory.getAlgorithm();
	}
	
}