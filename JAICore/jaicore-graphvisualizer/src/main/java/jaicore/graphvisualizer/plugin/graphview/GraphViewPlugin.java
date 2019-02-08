package jaicore.graphvisualizer.plugin.graphview;

import jaicore.graphvisualizer.events.graph.bus.AlgorithmEventSource;
import jaicore.graphvisualizer.events.gui.GUIEventSource;
import jaicore.graphvisualizer.plugin.IGUIPlugin;
import jaicore.graphvisualizer.plugin.IGUIPluginController;
import jaicore.graphvisualizer.plugin.IGUIPluginModel;
import jaicore.graphvisualizer.plugin.IGUIPluginView;

public class GraphViewPlugin implements IGUIPlugin {

	private GraphViewPluginController controller;
	private GraphViewPluginView view;

	public GraphViewPlugin() {
		this.view = new GraphViewPluginView();
		this.controller = new GraphViewPluginController(view.getModel());
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
