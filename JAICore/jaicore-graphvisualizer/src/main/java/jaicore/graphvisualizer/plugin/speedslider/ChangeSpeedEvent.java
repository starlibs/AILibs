package jaicore.graphvisualizer.plugin.speedslider;

import jaicore.graphvisualizer.events.gui.GUIEvent;

public class ChangeSpeedEvent implements GUIEvent {

	private int newSpeedPercentage;

	public ChangeSpeedEvent(int newSpeedPercentage) {
		this.newSpeedPercentage = newSpeedPercentage;
	}

	public int getNewSpeedPercentage() {
		return newSpeedPercentage;
	}
}
