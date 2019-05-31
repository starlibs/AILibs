package ai.libs.jaicore.graphvisualizer.plugin.speedslider;

import ai.libs.jaicore.graphvisualizer.events.graph.bus.AlgorithmEventSource;
import ai.libs.jaicore.graphvisualizer.events.gui.GUIEventSource;
import ai.libs.jaicore.graphvisualizer.plugin.IGUIPlugin;
import ai.libs.jaicore.graphvisualizer.plugin.IGUIPluginController;
import ai.libs.jaicore.graphvisualizer.plugin.IGUIPluginModel;
import ai.libs.jaicore.graphvisualizer.plugin.IGUIPluginView;

public class SpeedSliderGUIPlugin implements IGUIPlugin {

	private SpeedSliderGUIPluginController controller;
	private SpeedSliderGUIPluginView view;

	public SpeedSliderGUIPlugin() {
		view = new SpeedSliderGUIPluginView();
		controller = new SpeedSliderGUIPluginController(view.getModel());
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
