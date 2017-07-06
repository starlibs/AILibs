package jaicore.search.algorithms.standard.rdfs;

import java.util.Random;

import jaicore.search.algorithms.standard.bestfirst.BestFirst;
import jaicore.search.algorithms.standard.bestfirst.RandomizedDepthFirstEvaluator;
import jaicore.search.algorithms.standard.core.NodeEvaluator;
import jaicore.search.structure.core.GraphGenerator;

public class RandomizedDepthFirstSearch<T, A> extends BestFirst<T, A> {

	public RandomizedDepthFirstSearch(GraphGenerator<T, A> graphGenerator, Random random) {
		super(graphGenerator, new RandomizedDepthFirstEvaluator<>(random));
	}
}
