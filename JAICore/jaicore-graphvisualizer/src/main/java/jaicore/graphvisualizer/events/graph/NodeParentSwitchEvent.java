package jaicore.graphvisualizer.events.graph;

import jaicore.basic.algorithm.events.AAlgorithmEvent;

public class NodeParentSwitchEvent<T> extends AAlgorithmEvent implements GraphEvent {
	private final T node;
	private final T oldParent;
	private final T newParent;
	public final String name = "NodeParentSwitchEvent";

	public NodeParentSwitchEvent(String algorithmEvent, T node, T oldParent, T newParent) {
		super(algorithmEvent);
		this.node = node;
		this.oldParent = oldParent;
		this.newParent = newParent;
	}

	public T getNode() {
		return node;
	}

	public T getOldParent() {
		return oldParent;
	}

	public T getNewParent() {
		return newParent;
	}

}
