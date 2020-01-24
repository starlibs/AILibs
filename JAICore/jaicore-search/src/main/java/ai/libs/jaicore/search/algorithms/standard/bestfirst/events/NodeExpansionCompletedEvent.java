package ai.libs.jaicore.search.algorithms.standard.bestfirst.events;

import org.api4.java.algorithm.IAlgorithm;

import ai.libs.jaicore.basic.algorithm.AAlgorithmEvent;

public class NodeExpansionCompletedEvent<N> extends AAlgorithmEvent {
	private final N expandedNode;

	public NodeExpansionCompletedEvent(final IAlgorithm<?, ?> algorithm, final N expandedNode) {
		super(algorithm);
		this.expandedNode = expandedNode;
	}

	public N getExpandedNode() {
		return this.expandedNode;
	}
}
