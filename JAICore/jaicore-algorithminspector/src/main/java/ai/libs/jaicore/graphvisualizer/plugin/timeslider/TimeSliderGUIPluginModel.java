package ai.libs.jaicore.graphvisualizer.plugin.timeslider;

import ai.libs.jaicore.graphvisualizer.plugin.ASimpleMVCPluginModel;

public class TimeSliderGUIPluginModel extends ASimpleMVCPluginModel<TimeSliderGUIPluginView, TimeSliderGUIPluginController> {

	private int currentTimeStep;
	private int maximumTimeStep;
	private boolean paused;

	public TimeSliderGUIPluginModel() {
		this.currentTimeStep = 0;
		this.maximumTimeStep = 0;
		this.paused = true;
	}

	public void increaseMaximumTimeStep() {
		this.maximumTimeStep++;
		this.getView().update();
	}

	public void reset() {
		this.currentTimeStep = 0;
		this.maximumTimeStep = 0;
		this.getView().update();
	}

	public int getCurrentTimeStep() {
		return this.currentTimeStep;
	}

	public int getMaximumTimeStep() {
		return this.maximumTimeStep;
	}

	public void pause() {
		this.paused = true;
		this.getView().update();
	}

	public void unpause() {
		this.paused = false;
		this.getView().update();
	}

	public void increaseCurrentTimeStep() {
		this.currentTimeStep++;
		this.getView().update();
	}

	public void setCurrentTimeStep(final int currentTimeStep) {
		this.currentTimeStep = currentTimeStep;
		this.getView().update();
	}

	public boolean isPaused() {
		return this.paused;
	}

	@Override
	public void clear() {
		/* do nothing */
	}
}
