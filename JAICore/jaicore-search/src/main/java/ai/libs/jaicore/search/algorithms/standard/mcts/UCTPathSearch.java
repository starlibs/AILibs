package ai.libs.jaicore.search.algorithms.standard.mcts;

import java.util.Random;

import ai.libs.jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;

public class UCTPathSearch<T, A> extends MCTSPathSearch<T, A, Double> {

	public UCTPathSearch(final GraphSearchWithPathEvaluationsInput<T, A, Double> problem, final boolean maximization, final int seed, final double evaluationFailurePenalty, final boolean forbidDoublePaths) {
		super(problem, new UCBPolicy<>(maximization), new UniformRandomPolicy<>(new Random(seed + UCTPathSearch.class.hashCode())), evaluationFailurePenalty, forbidDoublePaths);
	}

	public UCTPathSearch(final GraphSearchWithPathEvaluationsInput<T, A, Double> problem, final int seed, final double evaluationFailurePenalty, final boolean forbidDoublePaths) {
		this(problem, false, seed, evaluationFailurePenalty, forbidDoublePaths);
	}
}
