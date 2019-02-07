package jaicore.graphvisualizer.plugin.nodeinfo;

import jaicore.graphvisualizer.events.graph.bus.GraphEventSource;
import jaicore.graphvisualizer.events.gui.GUIEventSource;
import jaicore.graphvisualizer.plugin.GUIPlugin;
import jaicore.graphvisualizer.plugin.GUIPluginController;
import jaicore.graphvisualizer.plugin.GUIPluginModel;
import jaicore.graphvisualizer.plugin.GUIPluginView;

public class NodeInfoGUIPlugin<N> implements GUIPlugin {

	private NodeInfoGUIPluginController<N> controller;
	private NodeInfoGUIPluginView<N> view;

	public NodeInfoGUIPlugin(NodeInfoGenerator<N> nodeInfoGenerator) {
		view = new NodeInfoGUIPluginView<>(nodeInfoGenerator);
		controller = new NodeInfoGUIPluginController<>(view.getModel());
	}

	@Override
	public GUIPluginController getController() {
		return controller;
	}

	@Override
	public GUIPluginModel getModel() {
		return view.getModel();
	}

	@Override
	public GUIPluginView getView() {
		return view;
	}

	@Override
	public void setGraphEventSource(GraphEventSource graphEventSource) {
		graphEventSource.registerListener(controller);
	}

	@Override
	public void setGUIEventSource(GUIEventSource guiEventSource) {
		guiEventSource.registerListener(controller);
	}

}
