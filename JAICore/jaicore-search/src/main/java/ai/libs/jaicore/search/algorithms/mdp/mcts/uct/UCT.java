package ai.libs.jaicore.search.algorithms.mdp.mcts.uct;

import java.util.Random;

import ai.libs.jaicore.search.algorithms.mdp.mcts.MCTS;
import ai.libs.jaicore.search.algorithms.mdp.mcts.UniformRandomPolicy;
import ai.libs.jaicore.search.probleminputs.IMDP;

public class UCT<N, A> extends MCTS<N, A> {

	public UCT(final IMDP<N, A, Double> input, final double maxIterations, final double gamma, final double epsilon, final Random r) {
		super(input, new UCBPolicy<>(input.isMaximizing()), new UniformRandomPolicy<>(r), maxIterations, gamma, epsilon);
	}
}
