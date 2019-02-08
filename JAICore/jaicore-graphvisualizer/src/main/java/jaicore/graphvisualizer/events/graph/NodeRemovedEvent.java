package jaicore.graphvisualizer.events.graph;

import jaicore.basic.algorithm.events.AAlgorithmEvent;

public class NodeRemovedEvent<T> extends AAlgorithmEvent implements GraphEvent {

	private final T node;
	public final String name = "NodeRemovedEvent"; // changed name to NodeRemovedEvent. before it was: "§NodeRemovedEvent";

	public NodeRemovedEvent(String algorithmId, T node) {
		super(algorithmId);
		this.node = node;
	}

	public T getNode() {
		return node;
	}

}
