package jaicore.graphvisualizer.plugin.nodeinfo;

import jaicore.graphvisualizer.plugin.IGUIPluginModel;

/**
 * 
 * @author hetzer
 *
 * @param <N>
 *            The node type class.
 */
public class NodeInfoGUIPluginModel<N> implements IGUIPluginModel {

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
