package jaicore.search.algorithms.standard.bestfirst;

import java.util.Random;

import jaicore.search.algorithms.parallel.parallelexploration.distributed.interfaces.SerializableNodeEvaluator;
import jaicore.search.structure.core.Node;

@SuppressWarnings("serial")
public class RandomizedDepthFirstEvaluator<T> implements SerializableNodeEvaluator<T,Double> {

	private final Random rand;

	public RandomizedDepthFirstEvaluator(Random rand) {
		super();
		this.rand = rand;
	}

	@Override
	public Double f(Node<T,?> node) {
		return (double) (-1 * (node.path().size() * 1000 + rand.nextInt(100)));
	}
}
