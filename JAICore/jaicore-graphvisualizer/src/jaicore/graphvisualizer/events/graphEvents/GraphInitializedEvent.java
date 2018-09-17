package jaicore.graphvisualizer.events.graphEvents;

public class GraphInitializedEvent<T> implements GraphEvent {

	private T root;
	public final String name = "GraphInitializedEvent";

	public GraphInitializedEvent(T root) {
		super();
		this.root = root;
	}

	public T getRoot() {
		return root;
	}

	public void setRoot(T root) {
		this.root = root;
	}

}
