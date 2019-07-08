package ai.libs.jaicore.search.algorithms.standard.mcts;

import java.util.Random;

import ai.libs.jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;

public class UCTPathSearch<T,A> extends MCTSPathSearch<T,A,Double> {

	public UCTPathSearch(GraphSearchWithPathEvaluationsInput<T, A, Double> problem, boolean maximization, int seed, double evaluationFailurePenalty, boolean forbidDoublePaths) {
		super(problem, new UCBPolicy<>(maximization), new UniformRandomPolicy<>(new Random(seed)), evaluationFailurePenalty, forbidDoublePaths);
	}
	
	public UCTPathSearch(GraphSearchWithPathEvaluationsInput<T, A, Double> problem, int seed, double evaluationFailurePenalty, boolean forbidDoublePaths) {
		this(problem, false, seed, evaluationFailurePenalty, forbidDoublePaths);
	}
}
