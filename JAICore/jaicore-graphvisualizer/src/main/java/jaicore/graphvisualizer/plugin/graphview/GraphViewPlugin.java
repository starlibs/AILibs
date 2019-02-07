package jaicore.graphvisualizer.plugin.graphview;

import jaicore.graphvisualizer.events.graph.bus.GraphEventSource;
import jaicore.graphvisualizer.events.gui.GUIEventSource;
import jaicore.graphvisualizer.plugin.GUIPlugin;
import jaicore.graphvisualizer.plugin.GUIPluginController;
import jaicore.graphvisualizer.plugin.GUIPluginModel;
import jaicore.graphvisualizer.plugin.GUIPluginView;

public class GraphViewPlugin implements GUIPlugin {

	private GraphViewPluginController controller;
	private GraphViewPluginView view;

	public GraphViewPlugin() {
		this.view = new GraphViewPluginView();
		this.controller = new GraphViewPluginController(view.getModel());
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
