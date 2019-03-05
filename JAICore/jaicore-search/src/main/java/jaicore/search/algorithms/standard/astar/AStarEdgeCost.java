package jaicore.search.algorithms.standard.astar;

import jaicore.search.model.travesaltree.Node;

public interface AStarEdgeCost<T> {
	public double g(Node<T,?> from, Node<T,?> to);
}
