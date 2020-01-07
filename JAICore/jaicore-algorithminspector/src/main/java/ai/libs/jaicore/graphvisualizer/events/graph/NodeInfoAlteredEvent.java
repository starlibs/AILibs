package ai.libs.jaicore.graphvisualizer.events.graph;

import org.api4.java.algorithm.events.AAlgorithmEvent;

public class NodeInfoAlteredEvent<T> extends AAlgorithmEvent implements GraphEvent {

	private final T node;

	public NodeInfoAlteredEvent(final String algorithmId, final T node) {
		super(algorithmId);
		this.node = node;
	}

	public T getNode() {
		return this.node;
	}

}
