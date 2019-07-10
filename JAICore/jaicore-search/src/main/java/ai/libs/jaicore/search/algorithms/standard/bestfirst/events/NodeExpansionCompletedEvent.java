package ai.libs.jaicore.search.algorithms.standard.bestfirst.events;

import org.api4.java.algorithm.events.AAlgorithmEvent;

public class NodeExpansionCompletedEvent<N> extends AAlgorithmEvent {
	private final N expandedNode;

	public NodeExpansionCompletedEvent(final String algorithmId, final N expandedNode) {
		super(algorithmId);
		this.expandedNode = expandedNode;
	}

	public N getExpandedNode() {
		return this.expandedNode;
	}
}
