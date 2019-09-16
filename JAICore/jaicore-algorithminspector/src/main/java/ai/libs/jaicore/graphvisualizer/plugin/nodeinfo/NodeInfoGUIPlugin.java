package ai.libs.jaicore.graphvisualizer.plugin.nodeinfo;

import org.api4.java.common.control.ILoggingCustomizable;

import ai.libs.jaicore.graphvisualizer.events.gui.GUIEventSource;
import ai.libs.jaicore.graphvisualizer.events.recorder.property.PropertyProcessedAlgorithmEventSource;
import ai.libs.jaicore.graphvisualizer.plugin.IGUIPlugin;
import ai.libs.jaicore.graphvisualizer.plugin.IGUIPluginController;
import ai.libs.jaicore.graphvisualizer.plugin.IGUIPluginModel;
import ai.libs.jaicore.graphvisualizer.plugin.IGUIPluginView;

public class NodeInfoGUIPlugin implements IGUIPlugin, ILoggingCustomizable {

	private NodeInfoGUIPluginController controller;
	private NodeInfoGUIPluginView view;
	private String loggerName;

	public NodeInfoGUIPlugin(final String viewTitle) {
		this.view = new NodeInfoGUIPluginView(viewTitle);
		this.controller = new NodeInfoGUIPluginController(this.view.getModel());

	}

	public NodeInfoGUIPlugin() {
		this("Node Info View");
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
	}
}
