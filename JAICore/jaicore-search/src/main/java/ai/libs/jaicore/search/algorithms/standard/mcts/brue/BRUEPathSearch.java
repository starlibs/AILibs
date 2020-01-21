package ai.libs.jaicore.search.algorithms.standard.mcts.brue;

import java.util.Random;

import org.api4.java.ai.graphsearch.problem.IPathSearchWithPathEvaluationsInput;

import ai.libs.jaicore.search.algorithms.standard.mcts.MCTSPathSearch;
import ai.libs.jaicore.search.algorithms.standard.mcts.UniformRandomPolicy;

public class BRUEPathSearch<I extends IPathSearchWithPathEvaluationsInput<N, A, Double>, N, A> extends MCTSPathSearch<I, N, A, Double>  {
	public BRUEPathSearch(final I problem, final boolean maximization, final int seed, final double evaluationFailurePenalty) {
		super(problem, new BRUEPolicy<>(maximization), new UniformRandomPolicy<>(new Random(seed)), evaluationFailurePenalty);
	}

	public BRUEPathSearch(final I problem, final int seed, final double evaluationFailurePenalty) {
		this(problem, false, seed, evaluationFailurePenalty);
	}
}
