package ai.libs.jaicore.search.algorithms.standard.bestfirst.events;

import ai.libs.jaicore.basic.algorithm.events.AAlgorithmEvent;
import ai.libs.jaicore.search.model.travesaltree.Node;

public class RemovedGoalNodeFromOpenEvent<N, V extends Comparable<V>> extends AAlgorithmEvent {

	private final Node<N, V> goalNode;

	public RemovedGoalNodeFromOpenEvent(final String algorithmId, final Node<N, V> goalNode) {
		super(algorithmId);
		this.goalNode = goalNode;
	}

	public Node<N, V> getGoalNode() {
		return this.goalNode;
	}
}
