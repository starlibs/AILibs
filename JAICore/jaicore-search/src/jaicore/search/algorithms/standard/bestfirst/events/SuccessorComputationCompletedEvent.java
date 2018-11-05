package jaicore.search.algorithms.standard.bestfirst.events;

import java.util.List;

import jaicore.search.model.travesaltree.Node;
import jaicore.search.model.travesaltree.NodeExpansionDescription;

public class SuccessorComputationCompletedEvent<T, A> extends BestFirstEvent {
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
