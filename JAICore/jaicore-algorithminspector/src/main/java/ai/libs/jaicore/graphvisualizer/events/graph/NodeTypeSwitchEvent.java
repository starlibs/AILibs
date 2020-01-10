package ai.libs.jaicore.graphvisualizer.events.graph;

import org.api4.java.algorithm.IAlgorithm;

import ai.libs.jaicore.basic.algorithm.AAlgorithmEvent;

public class NodeTypeSwitchEvent<T> extends AAlgorithmEvent implements GraphEvent {

	private final T node;
	private final String type;

	public NodeTypeSwitchEvent(final IAlgorithm<?, ?> algorithm, final T node, final String type) {
		super(algorithm);
		this.node = node;
		this.type = type;
	}

	public T getNode() {
		return this.node;
	}

	public String getType() {
		return this.type;
	}

}
