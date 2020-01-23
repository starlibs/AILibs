package ai.libs.jaicore.graphvisualizer.plugin.controlbar;

import ai.libs.jaicore.graphvisualizer.events.gui.GUIEventSource;
import ai.libs.jaicore.graphvisualizer.events.recorder.property.PropertyProcessedAlgorithmEventSource;
import ai.libs.jaicore.graphvisualizer.plugin.IGUIPlugin;
import ai.libs.jaicore.graphvisualizer.plugin.IGUIPluginController;
import ai.libs.jaicore.graphvisualizer.plugin.IGUIPluginModel;
import ai.libs.jaicore.graphvisualizer.plugin.IGUIPluginView;

public class ControlBarGUIPlugin implements IGUIPlugin {

	private ControlBarGUIPluginController controller;
	private ControlBarGUIPluginView view;

	public ControlBarGUIPlugin() {
		this.view = new ControlBarGUIPluginView();
		this.controller = new ControlBarGUIPluginController(this.view.getModel());
	}

	@Override
	public IGUIPluginController getController() {
		return this.controller;
	}

	@Override
	public IGUIPluginModel getModel() {
		return this.view.getModel();
	}

	@Override
	public IGUIPluginView getView() {
		return this.view;
	}

	@Override
	public void setAlgorithmEventSource(final PropertyProcessedAlgorithmEventSource algorithmEventSource) {
		algorithmEventSource.registerListener(this.controller);
	}

	@Override
	public void setGUIEventSource(final GUIEventSource guiEventSource) {
		guiEventSource.registerListener(this.controller);
	}

	@Override
	public void stop() {
		/* nothing to do here */
	}
}
