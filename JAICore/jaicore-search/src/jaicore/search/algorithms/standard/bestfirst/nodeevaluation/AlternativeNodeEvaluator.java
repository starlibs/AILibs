package jaicore.search.algorithms.standard.bestfirst.nodeevaluation;

import jaicore.search.model.travesaltree.Node;

/**
 * This node evaluator can be used
 * 	a) if there is a prioritized node evaluator that should be used unless it returns NULL
 *  b) to realize dead-end recognition
 *  c) to use different node evaluators in different regions of the search graph
 * 
 * @author fmohr
 *
 * @param <T>
 * @param <V>
 */
public class AlternativeNodeEvaluator<T, V extends Comparable<V>> extends DecoratingNodeEvaluator<T, V> {

	private final INodeEvaluator<T, V> ne1;
	
	public AlternativeNodeEvaluator(INodeEvaluator<T, V> ne1, INodeEvaluator<T, V> ne2) {
		super(ne2);
		this.ne1 = ne1;
	}

	@Override
	public V f(Node<T, ?> node) throws Exception {
		V f1 = ne1.f(node);
		if (f1 != null)
			return f1;
		return super.f(node);
	}

	@Override
	public String toString() {
		return "AlternativeNodeEvaluator [primary=" + ne1 + ", secondary=" + super.getEvaluator() + "]";
	}
}
