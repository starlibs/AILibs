package ai.libs.jaicore.search.algorithms.standard.mcts;

import org.api4.java.ai.graphsearch.problem.IPathSearch;
import org.api4.java.ai.graphsearch.problem.IPathSearchWithPathEvaluationsInput;

import ai.libs.jaicore.search.algorithms.GraphSearchSolutionIteratorTester;
import ai.libs.jaicore.search.algorithms.mdp.mcts.old.uuct.CVaR;
import ai.libs.jaicore.search.algorithms.mdp.mcts.old.uuct.UUCTPathSearchFactory;
import ai.libs.jaicore.search.model.other.AgnosticPathEvaluator;
import ai.libs.jaicore.search.probleminputs.GraphSearchInput;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;

public class UUCTTester extends GraphSearchSolutionIteratorTester {

	@Override
	public <N, A> IPathSearch<?, ?, N, A> getSearchAlgorithm(final GraphSearchInput<N, A> problem) {
		UUCTPathSearchFactory<IPathSearchWithPathEvaluationsInput<N, A, Double>, N, A> factory = new UUCTPathSearchFactory<>();
		GraphSearchWithPathEvaluationsInput<N, A, Double> newProblem = new GraphSearchWithPathEvaluationsInput<>(problem, new AgnosticPathEvaluator<>());
		factory.setProblemInput(newProblem);
		factory.setUtility(new CVaR(0.05)); // test with conditional value at risk
		factory.setEvaluationFailurePenalty(0.0);
		factory.setForbidDoublePaths(true);
		return factory.getAlgorithm();
	}

}