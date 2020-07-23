package ai.libs.jaicore.search.algorithms.mdp.mcts;

import java.util.List;

import org.api4.java.algorithm.IAlgorithm;
import org.api4.java.datastructure.graph.ILabeledPath;

import ai.libs.jaicore.basic.algorithm.AAlgorithmEvent;

public class MCTSIterationCompletedEvent<N, A, V extends Comparable<V>> extends AAlgorithmEvent {

	private final ILabeledPath<N, A> rollout;
	private final List<V> scores;
	private final IPolicy<N, A> treePolicy;

	public MCTSIterationCompletedEvent(final IAlgorithm<?, ?> algorithm, final IPolicy<N, A> treePolicy, final ILabeledPath<N, A> rollout, final List<V> scores) {
		super(algorithm);
		this.treePolicy = treePolicy;
		this.rollout = rollout;
		this.scores = scores;
	}

	public ILabeledPath<N, A> getRollout() {
		return this.rollout;
	}

	public IPolicy<N, A> getTreePolicy() {
		return this.treePolicy;
	}

	public List<V> getScores() {
		return this.scores;
	}
}
