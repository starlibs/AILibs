package jaicore.graphvisualizer.plugin.nodeinfo;

import jaicore.graphvisualizer.events.graph.bus.AlgorithmEventSource;
import jaicore.graphvisualizer.events.gui.GUIEventSource;
import jaicore.graphvisualizer.plugin.IGUIPlugin;
import jaicore.graphvisualizer.plugin.IGUIPluginController;
import jaicore.graphvisualizer.plugin.IGUIPluginModel;
import jaicore.graphvisualizer.plugin.IGUIPluginView;

public class NodeInfoGUIPlugin<N> implements IGUIPlugin {

	private NodeInfoGUIPluginController<N> controller;
	private NodeInfoGUIPluginView<N> view;

	public NodeInfoGUIPlugin(NodeInfoGenerator<N> nodeInfoGenerator, String viewTitle) {
		view = new NodeInfoGUIPluginView<>(nodeInfoGenerator, viewTitle);
		controller = new NodeInfoGUIPluginController<>(view.getModel());

	}

	public NodeInfoGUIPlugin(NodeInfoGenerator<N> nodeInfoGenerator) {
		this(nodeInfoGenerator, "Node Info View");
	}

	@Override
	public IGUIPluginController getController() {
		return controller;
	}

	@Override
	public IGUIPluginModel getModel() {
		return view.getModel();
	}

	@Override
	public IGUIPluginView getView() {
		return view;
	}

	@Override
	public void setAlgorithmEventSource(AlgorithmEventSource graphEventSource) {
		graphEventSource.registerListener(controller);
	}

	@Override
	public void setGUIEventSource(GUIEventSource guiEventSource) {
		guiEventSource.registerListener(controller);
	}
}
