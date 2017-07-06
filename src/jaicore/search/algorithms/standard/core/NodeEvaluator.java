package jaicore.search.algorithms.standard.core;

import jaicore.search.structure.core.Node;

public interface NodeEvaluator<T,V extends Comparable<V>> {
	public V f(Node<T,V> node);
}
