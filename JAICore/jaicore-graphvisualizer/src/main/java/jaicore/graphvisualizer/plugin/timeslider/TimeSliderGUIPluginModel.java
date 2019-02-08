package jaicore.graphvisualizer.plugin.timeslider;

import jaicore.graphvisualizer.plugin.IGUIPluginModel;

public class TimeSliderGUIPluginModel implements IGUIPluginModel {

	private TimeSliderGUIPluginView view;

	private int currentTimeStep;
	private int maximumTimeStep;
	private boolean paused;

	public TimeSliderGUIPluginModel(TimeSliderGUIPluginView view) {
		this.view = view;
		currentTimeStep = 0;
		maximumTimeStep = 0;
		paused = true;
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

	public void pause() {
		paused = true;
		view.update();
	}

	public void unpause() {
		paused = false;
		view.update();
	}

	public void increaseCurrentTimeStep() {
		currentTimeStep++;
		view.update();
	}

	public void setCurrentTimeStep(int currentTimeStep) {
		this.currentTimeStep = currentTimeStep;
		view.update();
	}

	public boolean isPaused() {
		return paused;
	}
}
