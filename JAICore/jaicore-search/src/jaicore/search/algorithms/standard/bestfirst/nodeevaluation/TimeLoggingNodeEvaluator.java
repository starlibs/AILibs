package jaicore.search.algorithms.standard.bestfirst.nodeevaluation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jaicore.search.model.travesaltree.Node;

public class TimeLoggingNodeEvaluator<T, V extends Comparable<V>> extends DecoratingNodeEvaluator<T, V> {

	private final Map<Node<T, ?>, Integer> times = new ConcurrentHashMap<>();

	public TimeLoggingNodeEvaluator(INodeEvaluator<T, V> baseEvaluator) {
		super(baseEvaluator);
	}

	public int getMSRequiredForComputation(Node<T, V> node) {
		if (!times.containsKey(node))
			throw new IllegalArgumentException("No f-value has been computed for node: " + node);
		return times.get(node);
	}

	@Override
	public V f(Node<T, ?> node) throws Exception {
		long start = System.currentTimeMillis();
		V f = super.f(node);
		times.put(node, (int) (System.currentTimeMillis() - start));
		return f;
	}
}
