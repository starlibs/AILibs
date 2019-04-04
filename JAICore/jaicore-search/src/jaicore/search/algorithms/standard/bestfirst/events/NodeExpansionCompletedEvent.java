package jaicore.search.algorithms.standard.bestfirst.events;

public class NodeExpansionCompletedEvent<N> {
	private final N expandedNode;

	public NodeExpansionCompletedEvent(N expandedNode) {
		super();
		this.expandedNode = expandedNode;
	}

	public N getExpandedNode() {
		return expandedNode;
	}
}
