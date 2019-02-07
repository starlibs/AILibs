package jaicore.graphvisualizer.plugin.timeslider;

import jaicore.graphvisualizer.plugin.GUIPluginModel;

public class TimeSliderGUIPluginModel implements GUIPluginModel {

	private TimeSliderGUIPluginView view;

	private int currentTimeStep;

	private int maximumTimeStep;

	public TimeSliderGUIPluginModel(TimeSliderGUIPluginView view) {
		this.view = view;
	}

	public void increaseMaximumTimeStep() {
		maximumTimeStep++;
		view.update();
	}

	public int getMaximumTimeStep() {
		return maximumTimeStep;
	}

	public void reset() {
		currentTimeStep = 0;
	}

	public int getCurrentTimeStep() {
		return currentTimeStep;
	}
}
