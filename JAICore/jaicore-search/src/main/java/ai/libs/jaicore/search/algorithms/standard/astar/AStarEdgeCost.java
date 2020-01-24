package ai.libs.jaicore.search.algorithms.standard.astar;

import ai.libs.jaicore.search.model.travesaltree.BackPointerPath;

public interface AStarEdgeCost<T, A> {
	public double g(BackPointerPath<T, A, ?> from, BackPointerPath<T, A, ?> to);
}
