package ai.libs.jaicore.graphvisualizer.plugin.graphview;

import org.graphstream.graph.Node;

import ai.libs.jaicore.graphvisualizer.events.gui.GUIEvent;

public class NodeClickedEvent implements GUIEvent {

	private Node viewerNode;
	private String searchGraphNode;

	public NodeClickedEvent(Node viewerNode, String searchGraphNode) {
		this.viewerNode = viewerNode;
		this.searchGraphNode = searchGraphNode;
	}

	public Node getViewerNode() {
		return viewerNode;
	}

	public String getSearchGraphNode() {
		return searchGraphNode;
	}

}
