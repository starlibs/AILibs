package ai.libs.jaicore.search.algorithms.mdp.mcts;

import java.util.List;

import org.api4.java.datastructure.graph.ILabeledPath;

public interface IPathUpdatablePolicy<N, A, V extends Comparable<V>> extends IPolicy<N, A> {

	public void updatePath(ILabeledPath<N, A> path, List<V> scores); // the scores are the observed scores over the edges of the path
}
