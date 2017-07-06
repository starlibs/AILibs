package jaicore.search.algorithms.standard.astar;

import jaicore.search.structure.core.Node;

public interface AStarEdgeCost<T> {
	public double g(Node<T,Integer> from, Node<T,Integer> to);
}
