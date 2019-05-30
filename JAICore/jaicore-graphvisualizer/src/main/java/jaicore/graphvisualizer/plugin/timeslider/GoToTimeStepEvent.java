package jaicore.graphvisualizer.plugin.timeslider;

import jaicore.graphvisualizer.events.gui.GUIEvent;

public class GoToTimeStepEvent implements GUIEvent {

	private int newTimeStep;

	public GoToTimeStepEvent(int newTimeStep) {
		this.newTimeStep = newTimeStep;
	}

	public int getNewTimeStep() {
		return newTimeStep;
	}
}
