package ai.libs.jaicore.search.algorithms.standard.mcts;

import java.util.Random;

import org.api4.java.ai.graphsearch.problem.IGraphSearchWithPathEvaluationsInput;

public class UCTPathSearch<I extends IGraphSearchWithPathEvaluationsInput<N, A, Double>, N, A> extends MCTSPathSearch<I, N, A, Double> {

	public UCTPathSearch(final I problem, final boolean maximization, final int seed, final double evaluationFailurePenalty, final boolean forbidDoublePaths) {
		super(problem, new UCBPolicy<>(maximization), new UniformRandomPolicy<>(new Random(seed + UCTPathSearch.class.hashCode())), evaluationFailurePenalty, forbidDoublePaths);
	}

	public UCTPathSearch(final I problem, final int seed, final double evaluationFailurePenalty, final boolean forbidDoublePaths) {
		this(problem, false, seed, evaluationFailurePenalty, forbidDoublePaths);
	}
}
