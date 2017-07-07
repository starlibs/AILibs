package jaicore.search.structure.events;

public class NodeRemovedEvent<T> {

	private final T node;

	public NodeRemovedEvent(T node) {
		super();
		this.node = node;
	}
	public T getNode() {
		return node;
	}

}
