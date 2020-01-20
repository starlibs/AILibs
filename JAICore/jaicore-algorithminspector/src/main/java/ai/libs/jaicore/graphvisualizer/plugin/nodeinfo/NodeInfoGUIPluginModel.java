package ai.libs.jaicore.graphvisualizer.plugin.nodeinfo;

import java.util.HashMap;
import java.util.Map;

import ai.libs.jaicore.graphvisualizer.plugin.ASimpleMVCPluginModel;

/**
 *
 * @author hetzer
 *
 * @param <N>
 *            The node type class.
 */
public class NodeInfoGUIPluginModel extends ASimpleMVCPluginModel<NodeInfoGUIPluginView, NodeInfoGUIPluginController> {

	private NodeInfoGUIPluginView view;

	private Map<String, String> nodeIdToNodeInfoMap;
	private String currentlySelectedNode;

	public NodeInfoGUIPluginModel(final NodeInfoGUIPluginView view) {
		this.view = view;
		this.nodeIdToNodeInfoMap = new HashMap<>();
	}

	public void addNodeIdToNodeInfoMapping(final String nodeId, final String nodeInfo) {
		this.nodeIdToNodeInfoMap.put(nodeId, nodeInfo);
	}

	public String getNodeInfoForNodeId(final String nodeId) {
		return this.nodeIdToNodeInfoMap.get(nodeId);
	}

	public String getCurrentlySelectedNode() {
		return this.currentlySelectedNode;
	}

	public void setCurrentlySelectedNode(final String currentlySelectedNode) {
		this.currentlySelectedNode = currentlySelectedNode;
		this.view.update();
	}

	public String getNodeInfoForCurrentlySelectedNode() {
		return this.getNodeInfoForNodeId(this.getCurrentlySelectedNode());
	}

	@Override
	public void clear() {
		/* ignore this */
	}

}
