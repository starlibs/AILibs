package ai.libs.jaicore.search.algorithms.standard.mcts.tag;

import java.util.Random;

import ai.libs.jaicore.search.algorithms.standard.mcts.MCTSPathSearch;
import ai.libs.jaicore.search.algorithms.standard.mcts.UniformRandomPolicy;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;

public class TAGMCTSPathSearch<T, A> extends MCTSPathSearch<T, A, Double> {

	public TAGMCTSPathSearch(final GraphSearchWithPathEvaluationsInput<T, A, Double> problem, final boolean maximization, final int seed, final double evaluationFailurePenalty) {
		super(problem, new TAGPolicy<>(maximization), new UniformRandomPolicy<>(new Random(seed + TAGMCTSPathSearch.class.hashCode())), evaluationFailurePenalty, true);
	}

	public TAGMCTSPathSearch(final GraphSearchWithPathEvaluationsInput<T, A, Double> problem, final int seed, final double evaluationFailurePenalty) {
		this(problem, false, seed, evaluationFailurePenalty);
	}
}
