package ai.libs.jaicore.graphvisualizer.events.graph;

import org.api4.java.algorithm.IAlgorithm;

import ai.libs.jaicore.basic.algorithm.AAlgorithmEvent;

public class NodeParentSwitchEvent<T> extends AAlgorithmEvent implements GraphEvent {
	private final T node;
	private final T oldParent;
	private final T newParent;

	public NodeParentSwitchEvent(final IAlgorithm<?, ?> algorithm, final T node, final T oldParent, final T newParent) {
		super(algorithm);
		this.node = node;
		this.oldParent = oldParent;
		this.newParent = newParent;
	}

	public T getNode() {
		return this.node;
	}

	public T getOldParent() {
		return this.oldParent;
	}

	public T getNewParent() {
		return this.newParent;
	}

}
