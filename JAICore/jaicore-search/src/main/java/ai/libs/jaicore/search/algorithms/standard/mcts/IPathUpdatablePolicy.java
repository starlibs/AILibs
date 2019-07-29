package ai.libs.jaicore.search.algorithms.standard.mcts;

import java.util.List;

public interface IPathUpdatablePolicy<N, A, V extends Comparable<V>> extends IPolicy<N, A, V> {

	public void updatePath(List<N> path, V playout);
}
