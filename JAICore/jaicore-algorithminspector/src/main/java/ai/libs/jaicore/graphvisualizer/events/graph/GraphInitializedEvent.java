package ai.libs.jaicore.graphvisualizer.events.graph;

import org.api4.java.algorithm.IAlgorithm;

import ai.libs.jaicore.basic.algorithm.AAlgorithmEvent;

public class GraphInitializedEvent<T> extends AAlgorithmEvent implements GraphEvent {

	private T root;

	public GraphInitializedEvent(final IAlgorithm<?, ?> algorithm, final T root) {
		super(algorithm);
		this.root = root;
	}

	public T getRoot() {
		return this.root;
	}

	public void setRoot(final T root) {
		this.root = root;
	}

}
