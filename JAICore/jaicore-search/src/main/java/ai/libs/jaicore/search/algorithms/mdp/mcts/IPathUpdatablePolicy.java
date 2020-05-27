package ai.libs.jaicore.search.algorithms.mdp.mcts;

import org.api4.java.datastructure.graph.ILabeledPath;

public interface IPathUpdatablePolicy<N, A, V extends Comparable<V>> extends IPolicy<N, A> {

	public void updatePath(ILabeledPath<N, A> path, V playoutScore);
}
