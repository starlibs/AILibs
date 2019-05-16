package jaicore.graphvisualizer.plugin.nodeinfo;

import jaicore.graphvisualizer.events.gui.GUIEventSource;
import jaicore.graphvisualizer.events.recorder.property.PropertyProcessedAlgorithmEventSource;
import jaicore.graphvisualizer.plugin.IGUIPlugin;
import jaicore.graphvisualizer.plugin.IGUIPluginController;
import jaicore.graphvisualizer.plugin.IGUIPluginModel;
import jaicore.graphvisualizer.plugin.IGUIPluginView;

public class NodeInfoGUIPlugin implements IGUIPlugin {

	private NodeInfoGUIPluginController controller;
	private NodeInfoGUIPluginView view;

	public NodeInfoGUIPlugin(String viewTitle) {
		view = new NodeInfoGUIPluginView(viewTitle);
		controller = new NodeInfoGUIPluginController(view.getModel());

	}

	public NodeInfoGUIPlugin() {
		this("Node Info View");
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
	public void setAlgorithmEventSource(PropertyProcessedAlgorithmEventSource graphEventSource) {
		graphEventSource.registerListener(controller);
	}

	@Override
	public void setGUIEventSource(GUIEventSource guiEventSource) {
		guiEventSource.registerListener(controller);
	}
}
