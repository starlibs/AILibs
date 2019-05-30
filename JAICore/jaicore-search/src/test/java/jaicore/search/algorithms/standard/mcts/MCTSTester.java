package jaicore.search.algorithms.standard.mcts;

import jaicore.search.algorithms.GraphSearchSolutionIteratorTester;
import jaicore.search.core.interfaces.IGraphSearch;
import jaicore.search.model.other.AgnosticPathEvaluator;
import jaicore.search.probleminputs.GraphSearchInput;
import jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;

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