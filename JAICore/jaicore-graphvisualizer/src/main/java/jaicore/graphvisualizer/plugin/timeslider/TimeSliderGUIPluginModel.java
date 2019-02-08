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

	public void reset() {
		currentTimeStep = 0;
		maximumTimeStep = 0;
		view.update();
	}

	public int getCurrentTimeStep() {
		return currentTimeStep;
	}

	public int getMaximumTimeStep() {
		return maximumTimeStep;
	}

	public void increaseCurrentTimeStep() {
		currentTimeStep++;
	}
}
