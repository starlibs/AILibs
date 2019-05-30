package jaicore.search.algorithms.standard.mcts;

import java.util.Random;

import jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;

public class UCT<T,A> extends MCTS<T,A,Double> {

	public UCT(GraphSearchWithPathEvaluationsInput<T, A, Double> problem, boolean maximization, int seed, double evaluationFailurePenalty, boolean forbidDoublePaths) {
		super(problem, new UCBPolicy<>(maximization), new UniformRandomPolicy<>(new Random(seed)), evaluationFailurePenalty, forbidDoublePaths);
	}
	
	public UCT(GraphSearchWithPathEvaluationsInput<T, A, Double> problem, int seed, double evaluationFailurePenalty, boolean forbidDoublePaths) {
		this(problem, false, seed, evaluationFailurePenalty, forbidDoublePaths);
	}
}
