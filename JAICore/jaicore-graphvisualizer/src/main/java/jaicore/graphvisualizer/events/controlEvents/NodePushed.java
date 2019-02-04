package jaicore.graphvisualizer.events.controlEvents;

/**
 * A ControlEvent which is triggerd by pushing a node.
 */
public class NodePushed<N> implements ControlEvent {

	N node;

	/**
	 * A new NodePushed event
	 * 
	 * @param node The pushed node.
	 */
	public NodePushed(N node) {
		this.node = node;
	}

	public N getNode() {
		return node;
	}
}
