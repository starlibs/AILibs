package jaicore.graphvisualizer.plugin.controlbar;

import jaicore.graphvisualizer.plugin.IGUIPluginModel;

public class ControlBarGUIPluginModel implements IGUIPluginModel {

	private ControlBarGUIPluginView view;

	private boolean visualizationPaused;

	public ControlBarGUIPluginModel(ControlBarGUIPluginView view) {
		this.view = view;
	}

	public void setPaused() {
		visualizationPaused = true;
		view.update();
	}

	public void setUnpaused() {
		visualizationPaused = false;
		view.update();
	}

	public boolean isPaused() {
		return visualizationPaused;
	}
}
