package jaicore.search.algorithms.standard.mcts;

import java.util.Map;

public interface IPolicy<T,A,V extends Comparable<V>> {

	public A getAction(T node, Map<A,T> actionsWithSuccessors) throws ActionPredictionFailedException;
}
