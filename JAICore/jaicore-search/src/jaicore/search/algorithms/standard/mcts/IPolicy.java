package jaicore.search.algorithms.standard.mcts;

import java.util.List;
import java.util.Map;

public interface IPolicy<T,A,V extends Comparable<V>> {
	
	public void updatePath(List<T> path, V score);
	
	public A getAction(T node, Map<A,T> actionsWithSuccessors);
}
