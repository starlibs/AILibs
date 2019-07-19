package ai.libs.jaicore.search.algorithms.standard.bestfirst.events;

import java.util.List;

import org.api4.java.datastructure.graph.implicit.NodeExpansionDescription;

import ai.libs.jaicore.search.model.travesaltree.BackPointerPath;

public class NodeExpansionJobSubmittedEvent<T, A, V extends Comparable<V>> extends BestFirstEvent {
	private final BackPointerPath<T, A, V> expandedNode;
	private final List<NodeExpansionDescription<T, A>> children;

	public NodeExpansionJobSubmittedEvent(final String algorithmId, final BackPointerPath<T, A, V> expandedNode, final List<NodeExpansionDescription<T, A>> children) {
		super(algorithmId);
		this.expandedNode = expandedNode;
		this.children = children;
	}

	public BackPointerPath<T, A, V> getExpandedNode() {
		return this.expandedNode;
	}

	public List<NodeExpansionDescription<T, A>> getChildren() {
		return this.children;
	}
}
