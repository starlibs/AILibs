package ai.libs.jaicore.search.algorithms.standard.astar;

import ai.libs.jaicore.search.model.travesaltree.Node;

public interface AStarEdgeCost<T> {
	public double g(Node<T,?> from, Node<T,?> to);
}
