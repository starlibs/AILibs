package util.search.rdfs;

import java.util.Random;

import util.search.bestfirst.BestFirst;
import util.search.bestfirst.RandomizedDepthFirstEvaluator;
import util.search.core.GraphGenerator;
import util.search.core.NodeEvaluator;

public class RandomizedDepthFirstSearch<T, A> extends BestFirst<T, A> {

	public RandomizedDepthFirstSearch(GraphGenerator<T, A> graphGenerator, Random random) {
		super(graphGenerator, new RandomizedDepthFirstEvaluator<>(random));
	}
}
