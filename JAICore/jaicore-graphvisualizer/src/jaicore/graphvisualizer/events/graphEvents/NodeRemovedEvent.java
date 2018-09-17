package jaicore.graphvisualizer.events.graphEvents;

public class NodeRemovedEvent<T> implements GraphEvent {

	private final T node;
	public final String name = "§NodeRemovedEvent";

	public NodeRemovedEvent(T node) {
		super();
		this.node = node;
	}

	public T getNode() {
		return node;
	}

}
