package jaicore.graphvisualizer.plugin.controlbar;

import jaicore.graphvisualizer.plugin.GUIPluginModel;

public class ControlBarGUIPluginModel implements GUIPluginModel {

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
