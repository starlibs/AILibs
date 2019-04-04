package jaicore.search.algorithms.standard.bestfirst.nodeevaluation;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import jaicore.search.model.travesaltree.Node;

public class SkippingNodeEvaluator<T,V extends Comparable<V>> implements INodeEvaluator<T,V> {

	private final INodeEvaluator<T,V> actualEvaluator;
	private final Random rand;
	private final float coin;
	private final Map<Node<T,?>, V> fCache = new HashMap<>();

	public SkippingNodeEvaluator(INodeEvaluator<T,V> actualEvaluator, Random rand, float coin) {
		super();
		this.actualEvaluator = actualEvaluator;
		this.rand = rand;
		this.coin = coin;
	}

	@Override
	public V f(Node<T,?> node) throws Exception {
		int depth = node.path().size() - 1;
		if (!fCache.containsKey(node)) {
			if (depth == 0) {
				fCache.put(node, actualEvaluator.f(node));
			} else {
				if (rand.nextFloat() >= coin) {
					fCache.put(node, actualEvaluator.f(node));
				} else {
					fCache.put(node, f(node.getParent()));
				}
			}
		}
		return fCache.get(node);
	}

	@Override
	public String toString() {
		return "SkippingEvaluator [actualEvaluator=" + actualEvaluator + "]";
	}
}