package ai.libs.jaicore.graphvisualizer.plugin.nodeinfo;

import ai.libs.jaicore.basic.algorithm.events.AlgorithmEvent;
import ai.libs.jaicore.graphvisualizer.events.graph.bus.HandleAlgorithmEventException;
import ai.libs.jaicore.graphvisualizer.events.gui.GUIEvent;
import ai.libs.jaicore.graphvisualizer.plugin.IGUIPluginController;
import ai.libs.jaicore.graphvisualizer.plugin.graphview.NodeClickedEvent;

public class NodeInfoGUIPluginController<N> implements IGUIPluginController {

	private NodeInfoGUIPluginModel<N> model;

	public NodeInfoGUIPluginController(NodeInfoGUIPluginModel<N> model) {
		this.model = model;
	}

	@Override
	public void handleAlgorithmEvent(AlgorithmEvent algorithmEvent) throws HandleAlgorithmEventException {
	}

	@Override
	public void handleGUIEvent(GUIEvent guiEvent) {
		if (NodeClickedEvent.class.isInstance(guiEvent)) {
			NodeClickedEvent nodeClickedEvent = (NodeClickedEvent) guiEvent;
			Object searchGraphNodeCorrespondingToClickedViewGraphNode = nodeClickedEvent.getSearchGraphNode();
			this.model.setCurrentlySelectedNode((N) searchGraphNodeCorrespondingToClickedViewGraphNode);
		}
	}

}
