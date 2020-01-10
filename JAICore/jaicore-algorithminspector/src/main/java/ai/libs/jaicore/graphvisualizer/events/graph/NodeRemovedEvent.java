package ai.libs.jaicore.graphvisualizer.events.graph;

import org.api4.java.algorithm.IAlgorithm;

import ai.libs.jaicore.basic.algorithm.AAlgorithmEvent;

public class NodeRemovedEvent<T> extends AAlgorithmEvent implements GraphEvent {

	private final T node;

	public NodeRemovedEvent(final IAlgorithm<?, ?> algorithm, final T node) {
		super(algorithm);
		this.node = node;
	}

	public T getNode() {
		return this.node;
	}

}
