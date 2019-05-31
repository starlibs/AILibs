package jaicore.graph;

public class NodeNotFoundException extends Exception {
	/**
	 * Generated Serial UID for extending Java API class Exception
	 */
	private static final long serialVersionUID = -8334959000362299402L;
	private final transient Object item;

	public NodeNotFoundException(final Object item) {
		super();
		this.item = item;
	}

	public Object getItem() {
		return this.item;
	}
}
