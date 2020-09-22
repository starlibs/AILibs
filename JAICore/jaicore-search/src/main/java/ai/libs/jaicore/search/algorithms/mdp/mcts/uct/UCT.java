package ai.libs.jaicore.search.algorithms.mdp.mcts.uct;

import java.util.Random;

import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;

import ai.libs.jaicore.search.algorithms.mdp.mcts.MCTS;
import ai.libs.jaicore.search.algorithms.mdp.mcts.UniformRandomPolicy;
import ai.libs.jaicore.search.probleminputs.IMDP;

public class UCT<N, A> extends MCTS<N, A> {

	public UCT(final IMDP<N, A, Double> input, final int maxIterations, final double gamma, final double epsilon, final Random r, final boolean tabooExhaustedNodes) {
		super(input, new UCBPolicy<>(gamma, input.isMaximizing()), new UniformRandomPolicy<>(r), maxIterations, gamma, epsilon, tabooExhaustedNodes);
	}

	@Override
	public UCBPolicy<N, A> getTreePolicy() {
		return (UCBPolicy<N, A>)super.getTreePolicy();
	}

	@Override
	public UCBPolicy<N, A> call() throws AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {
		return (UCBPolicy<N, A>) super.call();
	}
}
