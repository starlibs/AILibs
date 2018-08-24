package jaicore.search.algorithms.standard.bestfirst.nodeevaluation;

import jaicore.search.structure.core.Node;

public interface INodeEvaluator<T,V extends Comparable<V>> {
	public V f(Node<T,?> node) throws Throwable;
}
