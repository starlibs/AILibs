package ai.libs.jaicore.graphvisualizer.events.graph;

import org.api4.java.algorithm.IAlgorithm;

import ai.libs.jaicore.basic.algorithm.AAlgorithmEvent;

public class NodeAddedEvent<T> extends AAlgorithmEvent implements GraphEvent {

	private final T parent;
	private final T node;
	private final String type;

	public NodeAddedEvent(final IAlgorithm<?, ?> algorithm, final T parent, final T node, final String type) {
		super(algorithm);
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
