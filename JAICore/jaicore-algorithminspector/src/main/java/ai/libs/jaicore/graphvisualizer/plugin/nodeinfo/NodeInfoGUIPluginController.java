package ai.libs.jaicore.graphvisualizer.plugin.nodeinfo;

import org.api4.java.algorithm.events.serializable.PropertyProcessedAlgorithmEvent;

import ai.libs.jaicore.graphvisualizer.events.graph.bus.HandleAlgorithmEventException;
import ai.libs.jaicore.graphvisualizer.events.gui.GUIEvent;
import ai.libs.jaicore.graphvisualizer.plugin.IGUIPluginController;
import ai.libs.jaicore.graphvisualizer.plugin.graphview.NodeClickedEvent;

public class NodeInfoGUIPluginController implements IGUIPluginController {

	private NodeInfoGUIPluginModel model;

	public NodeInfoGUIPluginController(final NodeInfoGUIPluginModel model) {
		this.model = model;
	}

	@Override
	public void handleSerializableAlgorithmEvent(final PropertyProcessedAlgorithmEvent algorithmEvent) throws HandleAlgorithmEventException {
		Object rawNodeDisplayInfoProperty = algorithmEvent.getProperty(NodeDisplayInfoAlgorithmEventPropertyComputer.NODE_DISPLAY_INFO_PROPERTY_NAME);
		Object rawNodeInfoProperty = algorithmEvent.getProperty(NodeInfoAlgorithmEventPropertyComputer.NODE_INFO_PROPERTY_NAME);
		if (rawNodeDisplayInfoProperty != null && rawNodeInfoProperty != null) {
			NodeInfo nodeInfo = (NodeInfo) rawNodeInfoProperty;
			String nodeInfoText = (String) rawNodeDisplayInfoProperty;
			this.model.addNodeIdToNodeInfoMapping(nodeInfo.getMainNodeId(), nodeInfoText);
		}
	}

	@Override
	public void handleGUIEvent(final GUIEvent guiEvent) {
		if (NodeClickedEvent.class.isInstance(guiEvent)) {
			NodeClickedEvent nodeClickedEvent = (NodeClickedEvent) guiEvent;
			String searchGraphNodeCorrespondingToClickedViewGraphNode = nodeClickedEvent.getSearchGraphNode();
			this.model.setCurrentlySelectedNode(searchGraphNodeCorrespondingToClickedViewGraphNode);
		}
	}

}
