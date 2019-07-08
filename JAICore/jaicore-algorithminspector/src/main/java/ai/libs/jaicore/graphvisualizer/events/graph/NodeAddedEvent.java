package ai.libs.jaicore.graphvisualizer.events.graph;

import ai.libs.jaicore.basic.algorithm.events.AAlgorithmEvent;

public class NodeAddedEvent<T> extends AAlgorithmEvent implements GraphEvent {

	private final T parent;
	private final T node;
	private final String type;

	public NodeAddedEvent(final String algorithmId, final T parent, final T node, final String type) {
		super(algorithmId);
		this.parent = parent;
		this.node = node;
		this.type = type;
	}

	public T getParent() {
		return this.parent;
	}

	public T getNode() {
		return this.node;
	}

	public String getType() {
		return this.type;
	}

}
