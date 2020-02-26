package ai.libs.jaicore.search.algorithms.standard.mcts.uuct;

import java.util.Random;

import org.api4.java.ai.graphsearch.problem.IPathSearchWithPathEvaluationsInput;

import ai.libs.jaicore.search.algorithms.standard.mcts.MCTSPathSearch;
import ai.libs.jaicore.search.algorithms.standard.mcts.UniformRandomPolicy;

public class UUCTPathSearch<I extends IPathSearchWithPathEvaluationsInput<N, A, Double>, N, A> extends MCTSPathSearch<I, N, A, Double> {

	public UUCTPathSearch(final I problem, final IUCBUtilityFunction utility, final int seed, final double evaluationFailurePenalty) {
		super(problem, new UUCBPolicy<>(utility), new UniformRandomPolicy<>(new Random((long)seed + UUCTPathSearch.class.hashCode())), evaluationFailurePenalty);
	}
}
