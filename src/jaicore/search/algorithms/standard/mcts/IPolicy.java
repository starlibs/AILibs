package jaicore.search.algorithms.standard.mcts;

import java.util.List;

public interface IPolicy<T,A,V extends Comparable<V>> {
	public A getAction(T node, List<A> actions);
}
