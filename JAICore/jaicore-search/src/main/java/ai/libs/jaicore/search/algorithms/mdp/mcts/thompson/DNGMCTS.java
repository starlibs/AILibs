package ai.libs.jaicore.search.algorithms.mdp.mcts.thompson;

import java.util.Random;

import ai.libs.jaicore.search.algorithms.mdp.mcts.MCTS;
import ai.libs.jaicore.search.algorithms.mdp.mcts.UniformRandomPolicy;
import ai.libs.jaicore.search.probleminputs.IMDP;

public class DNGMCTS<N, A> extends MCTS<N, A> {

	public DNGMCTS(final IMDP<N, A, Double> input, final double varianceFactor, final double initLambda, final int maxIterations, final double gamma, final double epsilon, final Random random, final boolean tabooExhaustedNodes, final boolean maximize) {
		super(input, new DNGPolicy<>(gamma, t -> {
			try {
				return input.isTerminalState(t);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt(); // re-interrupt!
				return false;
			}
		}, varianceFactor, initLambda, maximize), new UniformRandomPolicy<>(random), maxIterations, gamma, epsilon, tabooExhaustedNodes);
	}
}
