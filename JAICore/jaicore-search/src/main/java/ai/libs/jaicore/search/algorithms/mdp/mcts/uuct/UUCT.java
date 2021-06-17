package ai.libs.jaicore.search.algorithms.mdp.mcts.uuct;

import java.util.Random;

import ai.libs.jaicore.search.algorithms.mdp.mcts.IPolicy;
import ai.libs.jaicore.search.algorithms.mdp.mcts.MCTS;
import ai.libs.jaicore.search.probleminputs.IMDP;

public class UUCT<N, A> extends MCTS<N, A> {

	public UUCT(final IMDP<N, A, Double> input, final IPolicy<N, A> defaultPolicy, final IUCBUtilityFunction utility, final int maxIterations, final double gamma, final double epsilon, final Random random, final boolean tabooExhaustedNodes) {
		super(input, new UUCBPolicy<>(utility), defaultPolicy, maxIterations, gamma, epsilon, tabooExhaustedNodes);
	}
}
