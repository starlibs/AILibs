package ai.libs.jaicore.graphvisualizer.plugin.graphview;

import org.api4.java.common.control.ILoggingCustomizable;

import ai.libs.jaicore.graphvisualizer.events.gui.GUIEventSource;
import ai.libs.jaicore.graphvisualizer.events.recorder.property.PropertyProcessedAlgorithmEventSource;
import ai.libs.jaicore.graphvisualizer.plugin.IGUIPlugin;
import ai.libs.jaicore.graphvisualizer.plugin.IGUIPluginController;
import ai.libs.jaicore.graphvisualizer.plugin.IGUIPluginModel;
import ai.libs.jaicore.graphvisualizer.plugin.IGUIPluginView;

public class GraphViewPlugin implements IGUIPlugin, ILoggingCustomizable {

	private GraphViewPluginController controller;
	private GraphViewPluginView view;
	private String loggerName;

	public GraphViewPlugin() {
		this.view = new GraphViewPluginView();
		this.controller = new GraphViewPluginController(this.view.getModel());
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
	public void setAlgorithmEventSource(final PropertyProcessedAlgorithmEventSource graphEventSource) {
		graphEventSource.registerListener(this.controller);
	}

	@Override
	public void setGUIEventSource(final GUIEventSource guiEventSource) {
		guiEventSource.registerListener(this.controller);
	}

	@Override
	public String getLoggerName() {
		return this.loggerName;
	}

	@Override
	public void setLoggerName(final String name) {
		this.controller.setLoggerName(name + ".controller");
		this.view.setLoggerName(name + ".view");
		this.view.getModel().setLoggerName(name + ".model");
	}
}
