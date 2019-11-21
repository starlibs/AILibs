package ai.libs.jaicore.search.algorithms.standard.mcts;

import org.api4.java.datastructure.graph.IPath;

public interface IPathUpdatablePolicy<N, A, V extends Comparable<V>> extends IPolicy<N, A, V> {

	public void updatePath(IPath<N, A> path, V playoutScore, int lengthOfPlayoutPath);
}
