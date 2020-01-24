package ai.libs.jaicore.search.algorithms.standard.mcts;

import java.util.Map;

public interface IPolicy<N, A, V extends Comparable<V>> {

	public A getAction(N node, Map<A, N> actionsWithSuccessors) throws ActionPredictionFailedException;
}
