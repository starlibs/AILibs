package jaicore.graphvisualizer.plugin.controlbar;

import jaicore.graphvisualizer.events.graph.bus.AlgorithmEventSource;
import jaicore.graphvisualizer.events.gui.GUIEventSource;
import jaicore.graphvisualizer.events.recorder.AlgorithmEventHistory;
import jaicore.graphvisualizer.plugin.GUIPlugin;
import jaicore.graphvisualizer.plugin.GUIPluginController;
import jaicore.graphvisualizer.plugin.GUIPluginModel;
import jaicore.graphvisualizer.plugin.GUIPluginView;

public class ControlBarGUIPlugin implements GUIPlugin {

	private AlgorithmEventHistory history;

	private ControlBarGUIPluginController controller;
	private ControlBarGUIPluginView view;

	public ControlBarGUIPlugin() {
		view = new ControlBarGUIPluginView();
		controller = new ControlBarGUIPluginController(view.getModel());
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
	public void setAlgorithmEventSource(AlgorithmEventSource graphEventSource) {
		graphEventSource.registerListener(controller);
	}

	@Override
	public void setGUIEventSource(GUIEventSource guiEventSource) {
		guiEventSource.registerListener(controller);
	}

	@Override
	public void setHistory(AlgorithmEventHistory history) {
		this.history = history;
	}

}
