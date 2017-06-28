package util.search.astar;

import util.search.core.Node;

public interface AStarEdgeCost<T> {
	public double g(Node<T,Integer> from, Node<T,Integer> to);
}
