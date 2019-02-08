package jaicore.graphvisualizer.events.graph;

import jaicore.basic.algorithm.events.AAlgorithmEvent;

public class GraphInitializedEvent<T> extends AAlgorithmEvent implements GraphEvent {

	private T root;
	public final String name = "GraphInitializedEvent";

	public GraphInitializedEvent(String algorithmId, T root) {
		super(algorithmId);
		this.root = root;
	}

	public T getRoot() {
		return root;
	}

	public void setRoot(T root) {
		this.root = root;
	}

}
