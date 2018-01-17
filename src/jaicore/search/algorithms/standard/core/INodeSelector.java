package jaicore.search.algorithms.standard.core;

import java.util.Queue;

import jaicore.search.structure.core.Node;

public interface INodeSelector<T,V extends Comparable<V>> {
	public Node<T,V> selectNode(Queue<Node<T,V>> open);
}
