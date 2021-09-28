package ai.libs.jaicore.search.algorithms.mdp.mcts.brue;

import java.util.Random;

import ai.libs.jaicore.search.algorithms.mdp.mcts.IPolicy;
import ai.libs.jaicore.search.algorithms.mdp.mcts.MCTS;
import ai.libs.jaicore.search.probleminputs.IMDP;
import ai.libs.jaicore.search.probleminputs.MDPUtils;

public class BRUE<N, A> extends MCTS<N, A>  {

	public BRUE(final IMDP<N, A, Double> input, final IPolicy<N, A> defaultPolicy, final int maxIterations, final double gamma, final double epsilon, final Random random, final boolean tabooExhaustedNodes) {
		super(input, new BRUEPolicy<>(input.isMaximizing(), MDPUtils.getTimeHorizon(gamma, epsilon), new Random(random.nextLong())), defaultPolicy, maxIterations, gamma, epsilon, tabooExhaustedNodes);
	}

}
