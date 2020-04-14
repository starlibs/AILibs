package ai.libs.jaicore.search.algorithms.mdp.mcts;

import org.api4.java.algorithm.IAlgorithm;

import ai.libs.jaicore.basic.algorithm.AAlgorithmEvent;
import ai.libs.jaicore.search.algorithms.standard.mcts.IPolicy;
import ai.libs.jaicore.search.model.other.EvaluatedSearchGraphPath;

public class MCTSIterationCompletedEvent<N, A, V extends Comparable<V>> extends AAlgorithmEvent {

	private final EvaluatedSearchGraphPath<N, A, V> rollout;
	private final IPolicy<N, A> treePolicy;

	public MCTSIterationCompletedEvent(final IAlgorithm<?, ?> algorithm, final IPolicy<N, A> treePolicy, final EvaluatedSearchGraphPath<N, A, V> rollout) {
		super(algorithm);
		this.treePolicy = treePolicy;
		this.rollout = rollout;
	}

	public EvaluatedSearchGraphPath<N, A, V> getRollout() {
		return this.rollout;
	}

	public IPolicy<N, A> getTreePolicy() {
		return this.treePolicy;
	}

}
