package jaicore.search.algorithms.standard.mcts;

import java.util.List;

public interface IPathUpdatablePolicy<T,A,V extends Comparable<V>> extends IPolicy<T, A, V> {
	
	public void updatePath(List<T> path, V playout);
}
