package jaicore.search.algorithms.standard.core.events;

import java.util.List;

import jaicore.search.structure.core.Node;
import jaicore.search.structure.core.NodeExpansionDescription;

public class SuccessorComputationCompletedEvent<T, A> {
	private Node<T, ?> node;
	private List<NodeExpansionDescription<T, A>> successorDescriptions;

	public SuccessorComputationCompletedEvent(Node<T, ?> node,
			List<NodeExpansionDescription<T, A>> successorDescriptions) {
		super();
		this.node = node;
		this.successorDescriptions = successorDescriptions;
	}

	public Node<T, ?> getNode() {
		return node;
	}

	public void setNode(Node<T, ?> node) {
		this.node = node;
	}

	public List<NodeExpansionDescription<T, A>> getSuccessorDescriptions() {
		return successorDescriptions;
	}

	public void setSuccessorDescriptions(List<NodeExpansionDescription<T, A>> successorDescriptions) {
		this.successorDescriptions = successorDescriptions;
	}
}
