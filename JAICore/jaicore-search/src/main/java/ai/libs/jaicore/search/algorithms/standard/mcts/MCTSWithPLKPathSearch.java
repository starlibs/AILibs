package ai.libs.jaicore.search.algorithms.standard.mcts;

import java.util.Random;

import org.api4.java.ai.graphsearch.problem.IGraphSearchWithPathEvaluationsInput;

public class MCTSWithPLKPathSearch<I extends IGraphSearchWithPathEvaluationsInput<N, A, Double>, N, A> extends MCTSPathSearch<I, N, A, Double> {

	public MCTSWithPLKPathSearch(final I problem, final boolean maximization, final int k, final int seed, final double evaluationFailurePenalty, final int epochs) {
		super(problem, new PLKPolicy<>(k, new Random(seed), epochs, maximization), new UniformRandomPolicy<>(new Random(seed * MCTSWithPLKPathSearch.class.hashCode())), evaluationFailurePenalty, true);
	}

	public MCTSWithPLKPathSearch(final I problem, final int k, final int seed, final double evaluationFailurePenalty, final int epochs) {
		this(problem, false, k, seed, evaluationFailurePenalty, epochs);
	}
}
