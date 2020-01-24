package ai.libs.jaicore.graphvisualizer.plugin.speedslider;

import ai.libs.jaicore.graphvisualizer.plugin.ASimpleMVCPluginModel;

public class SpeedSliderGUIPluginModel extends ASimpleMVCPluginModel<SpeedSliderGUIPluginView, SpeedSliderGUIPluginController> {

	private int currentSpeedPercentage;

	public SpeedSliderGUIPluginModel() {
		this.currentSpeedPercentage = 85;
	}

	public int getCurrentSpeedPercentage() {
		return this.currentSpeedPercentage;
	}

	public void setCurrentSpeedPercentage(final int currentSpeedPercentage) {
		this.currentSpeedPercentage = currentSpeedPercentage;
		this.getView().update();
	}

	@Override
	public void clear() {
		/* nothing to do */
	}
}
