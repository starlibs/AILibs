package ai.libs.jaicore.search.algorithms.standard.mcts;

import java.util.Random;

import ai.libs.jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;

public class MCTSWithPLKPathSearch<T, A> extends MCTSPathSearch<T, A, Double> {

	public MCTSWithPLKPathSearch(final GraphSearchWithPathEvaluationsInput<T, A, Double> problem, final boolean maximization, final int k, final int seed, final double evaluationFailurePenalty, final int epochs) {
		super(problem, new PLKPolicy<>(k, new Random(seed), epochs, maximization), new UniformRandomPolicy<>(new Random(seed * MCTSWithPLKPathSearch.class.hashCode())), evaluationFailurePenalty, true);
	}

	public MCTSWithPLKPathSearch(final GraphSearchWithPathEvaluationsInput<T, A, Double> problem, final int k, final int seed, final double evaluationFailurePenalty, final int epochs) {
		this(problem, false, k, seed, evaluationFailurePenalty, epochs);
	}
}
