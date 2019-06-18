package ai.libs.jaicore.graphvisualizer.events.graph;

import ai.libs.jaicore.basic.algorithm.events.AAlgorithmEvent;

public class NodeRemovedEvent<T> extends AAlgorithmEvent implements GraphEvent {

	private final T node;

	public NodeRemovedEvent(final String algorithmId, final T node) {
		super(algorithmId);
		this.node = node;
	}

	public T getNode() {
		return this.node;
	}

}
