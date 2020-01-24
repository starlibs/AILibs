package ai.libs.jaicore.graphvisualizer.plugin.controlbar;

import ai.libs.jaicore.graphvisualizer.plugin.ASimpleMVCPluginModel;

public class ControlBarGUIPluginModel extends ASimpleMVCPluginModel<ControlBarGUIPluginView, ControlBarGUIPluginController> {

	private boolean visualizationPaused;

	public void setPaused() {
		this.visualizationPaused = true;
		this.getView().update();
	}

	public void setUnpaused() {
		this.visualizationPaused = false;
		this.getView().update();
	}

	public boolean isPaused() {
		return this.visualizationPaused;
	}

	@Override
	public void clear() {
		/* nothing to do here */
	}
}
