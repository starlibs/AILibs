package jaicore.graphvisualizer.events;

public class NodeRemovedEvent<T> {

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
