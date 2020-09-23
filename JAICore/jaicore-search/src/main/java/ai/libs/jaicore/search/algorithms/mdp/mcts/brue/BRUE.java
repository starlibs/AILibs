package ai.libs.jaicore.search.algorithms.mdp.mcts.brue;

import java.util.Random;

import ai.libs.jaicore.search.algorithms.mdp.mcts.MCTS;
import ai.libs.jaicore.search.algorithms.mdp.mcts.UniformRandomPolicy;
import ai.libs.jaicore.search.probleminputs.IMDP;
import ai.libs.jaicore.search.probleminputs.MDPUtils;

public class BRUE<N, A> extends MCTS<N, A>  {

	public BRUE(final IMDP<N, A, Double> input, final int maxIterations, final double gamma, final double epsilon, final Random random, final boolean tabooExhaustedNodes) {
		super(input, new BRUEPolicy<>(input.isMaximizing(), MDPUtils.getTimeHorizon(gamma, epsilon), new Random(random.nextLong())), new UniformRandomPolicy<>(new Random(random.nextLong())), maxIterations, gamma, epsilon, tabooExhaustedNodes);
	}

}
