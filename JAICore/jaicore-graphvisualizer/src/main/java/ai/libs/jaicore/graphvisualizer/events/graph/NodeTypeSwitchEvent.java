package ai.libs.jaicore.graphvisualizer.events.graph;

import ai.libs.jaicore.basic.algorithm.events.AAlgorithmEvent;

public class NodeTypeSwitchEvent<T> extends AAlgorithmEvent implements GraphEvent {

	private final T node;
	private final String type;

	public NodeTypeSwitchEvent(final String algorithmId, final T node, final String type) {
		super(algorithmId);
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
