package util.search.bestfirst;

import java.util.Random;

import util.search.core.Node;
import util.search.core.NodeEvaluator;

public class RandomizedDepthFirstEvaluator<T> implements NodeEvaluator<T,Integer> {

	private final Random rand;

	public RandomizedDepthFirstEvaluator(Random rand) {
		super();
		this.rand = rand;
	}

	@Override
	public Integer f(Node<T,Integer> node) {
		return (int) (-1 * (node.path().size() * 1000 + rand.nextInt(100)));
	}
}
