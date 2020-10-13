package ai.libs.jaicore.search.algorithms.mdp.mcts.ensemble;

import java.util.Collection;
import java.util.Random;

import ai.libs.jaicore.search.algorithms.mdp.mcts.IPathUpdatablePolicy;
import ai.libs.jaicore.search.algorithms.mdp.mcts.MCTS;
import ai.libs.jaicore.search.algorithms.mdp.mcts.UniformRandomPolicy;
import ai.libs.jaicore.search.probleminputs.IMDP;

public class EnsembleMCTS<N, A> extends MCTS<N, A> {

	public EnsembleMCTS(final IMDP<N, A, Double> input, final Collection<IPathUpdatablePolicy<N, A, Double>> treePolicies, final int maxIterations, final double gamma, final double epsilon, final Random random, final boolean tabooExhaustedNodes) {
		super(input, new EnsembleTreePolicy<>(treePolicies), new UniformRandomPolicy<>(random), maxIterations, gamma, epsilon, tabooExhaustedNodes);
	}

}
