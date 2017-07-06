package jaicore.graph;

public class NodeNotFoundException extends Exception {
	/**
	 * Generated Serial UID for extending Java API class Exception
	 */
	private static final long serialVersionUID = -8334959000362299402L;
	private Object item;

	public NodeNotFoundException(Object item) {
		super();
		this.item = item;
	}

	public Object getItem() {
		return item;
	}
}
