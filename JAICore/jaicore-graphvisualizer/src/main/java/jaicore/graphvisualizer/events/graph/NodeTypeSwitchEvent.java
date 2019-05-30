package jaicore.graphvisualizer.events.graph;

import jaicore.basic.algorithm.events.AAlgorithmEvent;

public class NodeTypeSwitchEvent<T> extends AAlgorithmEvent implements GraphEvent {

	private final T node;
	private final String type;
	public final String name = "NodeTypeSwitchEvent";

	public NodeTypeSwitchEvent(String algorithmId, T node, String type) {
		super(algorithmId);
		this.node = node;
		this.type = type;
	}

	public T getNode() {
		return node;
	}

	public String getType() {
		return type;
	}

}
