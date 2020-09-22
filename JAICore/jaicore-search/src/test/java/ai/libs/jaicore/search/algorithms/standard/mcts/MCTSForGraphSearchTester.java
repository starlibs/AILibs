package ai.libs.jaicore.search.algorithms.standard.mcts;

import org.api4.java.ai.graphsearch.problem.IPathSearch;
import org.api4.java.ai.graphsearch.problem.IPathSearchInput;

import ai.libs.jaicore.search.algorithms.GraphSearchSolutionIteratorTester;
import ai.libs.jaicore.search.algorithms.mdp.mcts.MCTSFactory;
import ai.libs.jaicore.search.model.other.AgnosticPathEvaluator;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;

public abstract class MCTSForGraphSearchTester extends GraphSearchSolutionIteratorTester {

	public abstract <N, A> MCTSFactory<N, A, ?> getFactory();

	@Override
	public <N, A> IPathSearch<?, ?, N, A> getSearchAlgorithm(final IPathSearchInput<N, A> problem) {
		GraphSearchWithPathEvaluationsInput<N, A, Double> newProblem = new GraphSearchWithPathEvaluationsInput<>(problem, new AgnosticPathEvaluator<>());
		MCTSFactory<N, A, ?> factory = this.getFactory();
		factory.withTabooExhaustedNodes(true);
		return new MCTSPathSearch<>(newProblem, factory);
	}

}
