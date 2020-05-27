package ai.libs.jaicore.search.algorithms.mdp.mcts.old.ensemble;

import java.util.Collection;
import java.util.Random;

import org.api4.java.ai.graphsearch.problem.IPathSearchWithPathEvaluationsInput;

import ai.libs.jaicore.search.algorithms.mdp.mcts.IPathUpdatablePolicy;
import ai.libs.jaicore.search.algorithms.mdp.mcts.old.MCTSPathSearch;
import ai.libs.jaicore.search.algorithms.mdp.mcts.old.UniformRandomPolicy;

public class EnsembleMCTSPathSearch<I extends IPathSearchWithPathEvaluationsInput<N, A, Double>, N, A> extends MCTSPathSearch<I, N, A, Double> {

	public EnsembleMCTSPathSearch(final I problem, final Collection<? extends IPathUpdatablePolicy<N, A, Double>> treePolicies, final Random random) {
		super(problem, new EnsembleTreePolicy<>(treePolicies), new UniformRandomPolicy<>(random), 0.0);
	}

}
