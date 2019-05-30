package jaicore.graphvisualizer.events.graph;

import jaicore.basic.algorithm.events.AAlgorithmEvent;

public class NodeAddedEvent<T> extends AAlgorithmEvent implements GraphEvent {

	private final T parent, node;
	private final String type;
	public final String name = "NodeReachedEvent";

	public NodeAddedEvent(String algorithmId, T parent, T node, String type) {
		super(algorithmId);
		this.parent = parent;
		this.node = node;
		this.type = type;
	}

	public T getParent() {
		return parent;
	}

	public T getNode() {
		return node;
	}

	public String getType() {
		return type;
	}

}
