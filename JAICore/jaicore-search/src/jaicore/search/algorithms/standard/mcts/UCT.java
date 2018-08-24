package jaicore.search.algorithms.standard.mcts;

import java.util.Random;

import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.structure.core.GraphGenerator;

public class UCT<T,A> extends MCTS<T,A,Double> {

	public UCT(GraphGenerator<T, A> graphGenerator, INodeEvaluator<T, Double> playoutSimulator, boolean maximization, int seed) {
		super(graphGenerator, new UCBPolicy<>(maximization), new UniformRandomPolicy<>(new Random(seed)), playoutSimulator);
	}
	
	public UCT(GraphGenerator<T, A> graphGenerator, INodeEvaluator<T, Double> playoutSimulator, int seed) {
		this(graphGenerator, playoutSimulator, false, seed);
	}
}
