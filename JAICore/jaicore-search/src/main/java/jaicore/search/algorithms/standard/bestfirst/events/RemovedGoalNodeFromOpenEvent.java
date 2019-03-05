package jaicore.search.algorithms.standard.bestfirst.events;

import jaicore.basic.algorithm.events.AAlgorithmEvent;
import jaicore.search.model.travesaltree.Node;

public class RemovedGoalNodeFromOpenEvent<N> extends AAlgorithmEvent {

	private final Node<N, ?> goalNode;

	public RemovedGoalNodeFromOpenEvent(final String algorithmId, final Node<N, ?> goalNode) {
		super(algorithmId);
		this.goalNode = goalNode;
	}

	public Node<N, ?> getGoalNode() {
		return this.goalNode;
	}
}
