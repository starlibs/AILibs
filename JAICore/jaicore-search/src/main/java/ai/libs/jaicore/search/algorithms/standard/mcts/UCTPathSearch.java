package ai.libs.jaicore.search.algorithms.standard.mcts;

import java.util.Random;

import org.api4.java.ai.graphsearch.problem.IPathSearchWithPathEvaluationsInput;

public class UCTPathSearch<I extends IPathSearchWithPathEvaluationsInput<N, A, Double>, N, A> extends MCTSPathSearch<I, N, A, Double> {

	public UCTPathSearch(final I problem, final boolean maximization, final double explorationConstant, final int seed, final double evaluationFailurePenalty) {
		super(problem, new UCBPolicy<>(maximization), new UniformRandomPolicy<>(new Random((long)seed + UCTPathSearch.class.hashCode())), evaluationFailurePenalty);
		((UCBPolicy)this.getTreePolicy()).setExplorationConstant(explorationConstant);
	}

	public UCTPathSearch(final I problem, final double explorationConstant, final int seed, final double evaluationFailurePenalty) {
		this(problem, false, explorationConstant, seed, evaluationFailurePenalty);
	}
}
