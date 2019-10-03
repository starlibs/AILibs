package ai.libs.jaicore.search.algorithms.standard.mcts.comparison;

import java.util.Random;

import ai.libs.jaicore.search.algorithms.standard.mcts.MCTSPathSearch;
import ai.libs.jaicore.search.algorithms.standard.mcts.UniformRandomPolicy;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;

public class FixedCommitmentMCTSPathSearch<N, A> extends MCTSPathSearch<N, A, Double>{

	public FixedCommitmentMCTSPathSearch(final GraphSearchWithPathEvaluationsInput<N, A, Double> problem, final Double penaltyForFailedEvaluation, final int k) {
		super(problem, new FixedCommitmentPolicy<>(k), new UniformRandomPolicy<>(new Random(0)), penaltyForFailedEvaluation, true);
	}

}
