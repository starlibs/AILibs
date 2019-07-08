package ai.libs.jaicore.graphvisualizer.plugin.nodeinfo;

import java.util.HashMap;
import java.util.Map;

import ai.libs.jaicore.graphvisualizer.plugin.IGUIPluginModel;

/**
 * 
 * @author hetzer
 *
 * @param <N>
 *            The node type class.
 */
public class NodeInfoGUIPluginModel implements IGUIPluginModel {

	private NodeInfoGUIPluginView view;

	private Map<String, String> nodeIdToNodeInfoMap;
	private String currentlySelectedNode;

	public NodeInfoGUIPluginModel(NodeInfoGUIPluginView view) {
		this.view = view;
		nodeIdToNodeInfoMap = new HashMap<>();
	}

	public void addNodeIdToNodeInfoMapping(String nodeId, String nodeInfo) {
		nodeIdToNodeInfoMap.put(nodeId, nodeInfo);
	}

	public String getNodeInfoForNodeId(String nodeId) {
		return nodeIdToNodeInfoMap.get(nodeId);
	}

	public String getCurrentlySelectedNode() {
		return currentlySelectedNode;
	}

	public void setCurrentlySelectedNode(String currentlySelectedNode) {
		this.currentlySelectedNode = currentlySelectedNode;
		view.update();
	}

	public String getNodeInfoForCurrentlySelectedNode() {
		return getNodeInfoForNodeId(getCurrentlySelectedNode());
	}

}
