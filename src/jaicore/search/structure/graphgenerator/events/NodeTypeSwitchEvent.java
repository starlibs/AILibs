package jaicore.search.structure.graphgenerator.events;

public class NodeTypeSwitchEvent<T> {

	private final T node;
	private final String type;

	public NodeTypeSwitchEvent(T node, String type) {
		super();
		this.node = node;
		this.type = type;
	}

	public T getNode() {
		return node;
	}

	public String getType() {
		return type;
	}

}
