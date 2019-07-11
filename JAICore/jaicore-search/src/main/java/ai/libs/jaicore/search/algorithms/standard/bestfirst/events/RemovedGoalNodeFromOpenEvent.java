package ai.libs.jaicore.search.algorithms.standard.bestfirst.events;

import org.api4.java.algorithm.events.AAlgorithmEvent;

import ai.libs.jaicore.search.model.travesaltree.BackPointerPath;

public class RemovedGoalNodeFromOpenEvent<N,A, V extends Comparable<V>> extends AAlgorithmEvent {

	private final BackPointerPath<N, A, V> goalNode;

	public RemovedGoalNodeFromOpenEvent(final String algorithmId, final BackPointerPath<N, A, V> goalNode) {
		super(algorithmId);
		this.goalNode = goalNode;
	}

	public BackPointerPath<N, A, V> getGoalNode() {
		return this.goalNode;
	}
}
