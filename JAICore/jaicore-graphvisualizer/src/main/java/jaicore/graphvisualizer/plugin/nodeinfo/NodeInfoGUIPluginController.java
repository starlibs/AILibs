package jaicore.graphvisualizer.plugin.nodeinfo;

import jaicore.graphvisualizer.events.graph.GraphEvent;
import jaicore.graphvisualizer.events.graph.bus.HandleGraphEventException;
import jaicore.graphvisualizer.events.gui.GUIEvent;
import jaicore.graphvisualizer.plugin.GUIPluginController;
import jaicore.graphvisualizer.plugin.graphview.NodeClickedEvent;

public class NodeInfoGUIPluginController<N> implements GUIPluginController {

	private NodeInfoGUIPluginModel<N> model;

	public NodeInfoGUIPluginController(NodeInfoGUIPluginModel<N> model) {
		this.model = model;
	}

	@Override
	public void handleGraphEvent(GraphEvent graphEvent) throws HandleGraphEventException {

	}

	@Override
	public void handleGUIEvent(GUIEvent guiEvent) {
		try {
			if (NodeClickedEvent.class.isInstance(guiEvent)) {
				NodeClickedEvent nodeClickedEvent = (NodeClickedEvent) guiEvent;
				Object searchGraphNodeCorrespondingToClickedViewGraphNode = nodeClickedEvent.getSearchGraphNode();
				this.model.setCurrentlySelectedNode((N) searchGraphNodeCorrespondingToClickedViewGraphNode);
			}
		} catch (Exception exception) {
			exception.printStackTrace();
			System.out.println();
		}
	}

}
