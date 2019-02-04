package jaicore.search.algorithms.standard.bestfirst.events;

import java.util.List;

import jaicore.search.model.travesaltree.Node;
import jaicore.search.model.travesaltree.NodeExpansionDescription;

public class NodeExpansionJobSubmittedEvent<T, A, V extends Comparable<V>> extends BestFirstEvent {
	private final Node<T, V> expandedNode;
	private final List<NodeExpansionDescription<T, A>> children;

	public NodeExpansionJobSubmittedEvent(Node<T, V> expandedNode, List<NodeExpansionDescription<T, A>> children) {
		super();
		this.expandedNode = expandedNode;
		this.children = children;
	}

	public Node<T, V> getExpandedNode() {
		return expandedNode;
	}

	public List<NodeExpansionDescription<T, A>> getChildren() {
		return children;
	}
}
