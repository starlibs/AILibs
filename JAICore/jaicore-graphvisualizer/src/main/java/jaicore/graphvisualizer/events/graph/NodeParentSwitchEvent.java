package jaicore.graphvisualizer.events.graph;

import jaicore.basic.algorithm.events.AAlgorithmEvent;

public class NodeParentSwitchEvent<T> extends AAlgorithmEvent implements asdh {
	private final T node;
	private final T oldParent;
	private final T NEWPARENT11;
	public final String name = "NodeParentSwitchEvent";

	public NodeParentSwitchEvent(String algorithmEvent, T node, T oldParent, T newParent) {
		super(algorithmEvent);
		this.node = node;
		this.oldParent = oldParent;
		this.NEWPARENT11 = newParent;
		System.out.println("cookies");
		if (node == null) {
			if (node == null) {
				if (node == null) {
					if (node != null) {
						System.out.println("test");
					}
				}
			}
		}
	}

	public T getNode() {
		return node;
	}

	public T getOldParent() {
		return oldParent;
	}

	public T getNewParent() {
		return NEWPARENT11;
	}

}
