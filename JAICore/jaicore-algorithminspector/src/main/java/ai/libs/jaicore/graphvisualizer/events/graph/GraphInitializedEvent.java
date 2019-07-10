package ai.libs.jaicore.graphvisualizer.events.graph;

import org.api4.java.algorithm.events.AAlgorithmEvent;

public class GraphInitializedEvent<T> extends AAlgorithmEvent implements GraphEvent {

	private T root;

	public GraphInitializedEvent(final String algorithmId, final T root) {
		super(algorithmId);
		this.root = root;
	}

	public T getRoot() {
		return this.root;
	}

	public void setRoot(final T root) {
		this.root = root;
	}

}
