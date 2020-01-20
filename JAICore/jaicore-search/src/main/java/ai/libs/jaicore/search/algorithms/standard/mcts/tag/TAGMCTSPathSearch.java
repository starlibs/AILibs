package ai.libs.jaicore.search.algorithms.standard.mcts.tag;

import java.util.Random;

import org.api4.java.ai.graphsearch.problem.IPathSearchWithPathEvaluationsInput;

import ai.libs.jaicore.search.algorithms.standard.mcts.MCTSPathSearch;
import ai.libs.jaicore.search.algorithms.standard.mcts.UniformRandomPolicy;

public class TAGMCTSPathSearch<I extends IPathSearchWithPathEvaluationsInput<T, A, Double>, T, A> extends MCTSPathSearch<I, T, A, Double> {

	public TAGMCTSPathSearch(final I problem, final boolean maximization, final int seed, final double evaluationFailurePenalty) {
		super(problem, new TAGPolicy<>(maximization), new UniformRandomPolicy<>(new Random((long)seed + TAGMCTSPathSearch.class.hashCode())), evaluationFailurePenalty);
	}

	public TAGMCTSPathSearch(final I problem, final int seed, final double evaluationFailurePenalty) {
		this(problem, false, seed, evaluationFailurePenalty);
	}
}
