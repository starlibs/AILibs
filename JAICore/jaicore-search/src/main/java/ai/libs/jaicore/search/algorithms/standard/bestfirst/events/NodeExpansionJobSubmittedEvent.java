package ai.libs.jaicore.search.algorithms.standard.bestfirst.events;

import java.util.List;

import org.api4.java.algorithm.IAlgorithm;
import org.api4.java.datastructure.graph.implicit.INewNodeDescription;

import ai.libs.jaicore.search.model.travesaltree.BackPointerPath;

public class NodeExpansionJobSubmittedEvent<T, A, V extends Comparable<V>> extends BestFirstEvent {
	private final BackPointerPath<T, A, V> expandedNode;
	private final List<INewNodeDescription<T, A>> children;

	public NodeExpansionJobSubmittedEvent(final IAlgorithm<?, ?> algorithm, final BackPointerPath<T, A, V> expandedNode, final List<INewNodeDescription<T, A>> children) {
		super(algorithm);
		this.expandedNode = expandedNode;
		this.children = children;
	}

	public BackPointerPath<T, A, V> getExpandedNode() {
		return this.expandedNode;
	}

	public List<INewNodeDescription<T, A>> getChildren() {
		return this.children;
	}
}
