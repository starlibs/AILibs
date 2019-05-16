package jaicore.graphvisualizer.plugin.controlbar;

import jaicore.graphvisualizer.events.gui.GUIEventSource;
import jaicore.graphvisualizer.events.recorder.property.PropertyProcessedAlgorithmEventSource;
import jaicore.graphvisualizer.plugin.IGUIPlugin;
import jaicore.graphvisualizer.plugin.IGUIPluginController;
import jaicore.graphvisualizer.plugin.IGUIPluginModel;
import jaicore.graphvisualizer.plugin.IGUIPluginView;

public class ControlBarGUIPlugin implements IGUIPlugin {

	private ControlBarGUIPluginController controller;
	private ControlBarGUIPluginView view;

	public ControlBarGUIPlugin() {
		view = new ControlBarGUIPluginView();
		controller = new ControlBarGUIPluginController(view.getModel());
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
	public void setAlgorithmEventSource(PropertyProcessedAlgorithmEventSource algorithmEventSource) {
		algorithmEventSource.registerListener(controller);
	}

	@Override
	public void setGUIEventSource(GUIEventSource guiEventSource) {
		guiEventSource.registerListener(controller);
	}

}
