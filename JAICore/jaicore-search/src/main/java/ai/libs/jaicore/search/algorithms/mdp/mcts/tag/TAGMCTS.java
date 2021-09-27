package ai.libs.jaicore.search.algorithms.mdp.mcts.tag;

import ai.libs.jaicore.search.algorithms.mdp.mcts.IPolicy;
import ai.libs.jaicore.search.algorithms.mdp.mcts.MCTS;
import ai.libs.jaicore.search.probleminputs.IMDP;

public class TAGMCTS<N, A> extends MCTS<N, A> {

	public TAGMCTS(final IMDP<N, A, Double> input, final IPolicy<N, A> defaultPolicy, final double c, final int s, final double delta, final double thresholdIncrement, final int maxIterations, final double gamma, final double epsilon, final boolean tabooExhaustedNodes) {
		super(input, new TAGPolicy<>(c, s, delta, thresholdIncrement, input.isMaximizing()), defaultPolicy, maxIterations, gamma, epsilon, tabooExhaustedNodes);
	}
}
