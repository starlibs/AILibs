package jaicore.graphvisualizer.plugin.nodeinfo;

import jaicore.graphvisualizer.plugin.GUIPluginModel;

/**
 * 
 * @author hetzer
 *
 * @param <N>
 *            The node type class.
 */
public class NodeInfoGUIPluginModel<N> implements GUIPluginModel {

	private NodeInfoGUIPluginView<N> view;
	private N currentlySelectedNode;

	public NodeInfoGUIPluginModel(NodeInfoGUIPluginView<N> view) {
		this.view = view;
	}

	public void setCurrentlySelectedNode(N currentlySelectedNode) {
		this.currentlySelectedNode = currentlySelectedNode;
		view.update();
	}

	public N getCurrentlySelectedNode() {
		return currentlySelectedNode;
	}

}
