package ai.libs.jaicore.search.algorithms.mdp.mcts.spuct;

import ai.libs.jaicore.search.algorithms.mdp.mcts.IPolicy;
import ai.libs.jaicore.search.algorithms.mdp.mcts.MCTS;
import ai.libs.jaicore.search.probleminputs.IMDP;

public class SPUCT<N, A> extends MCTS<N, A> {

	public SPUCT(final IMDP<N, A, Double> input, final IPolicy<N, A> defaultPolicy, final double bigD, final int maxIterations, final double gamma, final double epsilon, final boolean tabooExhaustedNodes) {
		super(input, new SPUCBPolicy<>(gamma, bigD), defaultPolicy, maxIterations, gamma, epsilon, tabooExhaustedNodes);
	}
}
