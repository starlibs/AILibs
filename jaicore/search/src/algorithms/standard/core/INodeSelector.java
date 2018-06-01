package jaicore.search.algorithms.standard.core;

import jaicore.search.structure.core.Node;
import jaicore.search.structure.core.OpenCollection;

public interface INodeSelector<T,V extends Comparable<V>> {
	public Node<T,V> selectNode(OpenCollection<Node<T, V>> open);
}
