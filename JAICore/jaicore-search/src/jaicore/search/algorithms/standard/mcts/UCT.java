package jaicore.search.algorithms.standard.mcts;

import java.util.Random;

import jaicore.search.model.probleminputs.GraphSearchProblemInput;

public class UCT<T,A> extends MCTS<T,A,Double> {

	public UCT(GraphSearchProblemInput<T, A, Double> problem, boolean maximization, int seed) {
		super(problem, new UCBPolicy<>(maximization), new UniformRandomPolicy<>(new Random(seed)));
	}
	
	public UCT(GraphSearchProblemInput<T, A, Double> problem, int seed) {
		this(problem, false, seed);
	}
}
