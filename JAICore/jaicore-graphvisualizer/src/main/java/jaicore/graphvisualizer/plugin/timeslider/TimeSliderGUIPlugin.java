package jaicore.graphvisualizer.plugin.timeslider;

import jaicore.graphvisualizer.events.graph.bus.AlgorithmEventSource;
import jaicore.graphvisualizer.events.gui.GUIEventSource;
import jaicore.graphvisualizer.plugin.GUIPlugin;
import jaicore.graphvisualizer.plugin.GUIPluginController;
import jaicore.graphvisualizer.plugin.GUIPluginModel;
import jaicore.graphvisualizer.plugin.GUIPluginView;

public class TimeSliderGUIPlugin implements GUIPlugin {

	private TimeSliderGUIPluginController controller;
	private TimeSliderGUIPluginView view;

	public TimeSliderGUIPlugin() {
		view = new TimeSliderGUIPluginView();
		controller = new TimeSliderGUIPluginController(view.getModel());
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

}
