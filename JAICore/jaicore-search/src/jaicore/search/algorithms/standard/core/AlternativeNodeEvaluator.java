package jaicore.search.algorithms.standard.core;

import jaicore.search.structure.core.Node;

/**
 * This node evaluator can be used a) if there is a prioritized node evaluator that should be used unless it returns NULL b) to realize dead-end recognition c) to use different node evaluators in different regions of the search graph
 *
 * @author fmohr
 *
 * @param <T>
 * @param <V>
 */
public class AlternativeNodeEvaluator<T, V extends Comparable<V>> extends DecoratingNodeEvaluator<T, V> {

	private final INodeEvaluator<T, V> ne1;

	public AlternativeNodeEvaluator(final INodeEvaluator<T, V> ne1, final INodeEvaluator<T, V> ne2) {
		super(ne2);
		this.ne1 = ne1;
	}

	@Override
	public V f(final Node<T, ?> node) throws Throwable {
		V f1 = this.ne1.f(node);
		if (f1 != null) {
			return f1;
		}
		return super.f(node);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.ne1.getClass().getName());
		sb.append(" => ");
		sb.append(super.getEvaluator().getClass().getName());
		return sb.toString();
	}
}
