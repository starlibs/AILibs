package ai.libs.jaicore.search.algorithms.mdp.mcts;

import java.util.Collection;

public interface IPolicy<N, A> {

	public A getAction(N node, Collection<A> allowedActions) throws ActionPredictionFailedException;
}
