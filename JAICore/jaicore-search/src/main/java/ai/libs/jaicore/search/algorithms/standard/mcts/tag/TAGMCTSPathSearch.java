package ai.libs.jaicore.search.algorithms.standard.mcts.tag;

import java.util.Random;

import org.api4.java.ai.graphsearch.problem.IGraphSearchWithPathEvaluationsInput;

import ai.libs.jaicore.search.algorithms.standard.mcts.MCTSPathSearch;
import ai.libs.jaicore.search.algorithms.standard.mcts.UniformRandomPolicy;

public class TAGMCTSPathSearch<T, A> extends MCTSPathSearch<T, A, Double> {

	public TAGMCTSPathSearch(final IGraphSearchWithPathEvaluationsInput<T, A, Double> problem, final boolean maximization, final int seed, final double evaluationFailurePenalty) {
		super(problem, new TAGPolicy<>(maximization), new UniformRandomPolicy<>(new Random(seed + TAGMCTSPathSearch.class.hashCode())), evaluationFailurePenalty, true);
	}

	public TAGMCTSPathSearch(final IGraphSearchWithPathEvaluationsInput<T, A, Double> problem, final int seed, final double evaluationFailurePenalty) {
		this(problem, false, seed, evaluationFailurePenalty);
	}
}
