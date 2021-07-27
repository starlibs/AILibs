package ai.libs.jaicore.search.algorithms.mdp.mcts.thompson;

import java.util.Random;

import ai.libs.jaicore.search.algorithms.mdp.mcts.IPolicy;
import ai.libs.jaicore.search.algorithms.mdp.mcts.MCTS;
import ai.libs.jaicore.search.algorithms.mdp.mcts.UniformRandomPolicy;
import ai.libs.jaicore.search.probleminputs.IMDP;

public class DNGMCTS<N, A> extends MCTS<N, A> {

	public DNGMCTS(final IMDP<N, A, Double> input, final double varianceFactor, final double initLambda, final int maxIterations, final double gamma, final double epsilon, final Random random, final boolean tabooExhaustedNodes, final boolean maximize) {
		this(input, new UniformRandomPolicy<>(random), varianceFactor, initLambda, maxIterations, gamma, epsilon, random, tabooExhaustedNodes, maximize);
	}

	public DNGMCTS(final IMDP<N, A, Double> input, final IPolicy<N, A> defaultPolicy, final double varianceFactor, final double initLambda, final int maxIterations, final double gamma, final double epsilon, final Random random, final boolean tabooExhaustedNodes, final boolean maximize) {
		super(input, new DNGPolicy<>(gamma, t -> {
			try {
				return input.isTerminalState(t);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt(); // re-interrupt!
				return false;
			}
		}, varianceFactor, initLambda, maximize), defaultPolicy, maxIterations, gamma, epsilon, tabooExhaustedNodes);
	}
}
