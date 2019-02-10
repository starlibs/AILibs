package jaicore.graphvisualizer.plugin.speedslider;

import jaicore.graphvisualizer.plugin.IGUIPluginModel;

public class SpeedSliderGUIPluginModel implements IGUIPluginModel {

	private SpeedSliderGUIPluginView view;

	private int currentSpeedPercentage;

	public SpeedSliderGUIPluginModel(SpeedSliderGUIPluginView view) {
		this.view = view;
		currentSpeedPercentage = 85;
	}

	public int getCurrentSpeedPercentage() {
		return currentSpeedPercentage;
	}

	public void setCurrentSpeedPercentage(int currentSpeedPercentage) {
		this.currentSpeedPercentage = currentSpeedPercentage;
		view.update();
	}
}
