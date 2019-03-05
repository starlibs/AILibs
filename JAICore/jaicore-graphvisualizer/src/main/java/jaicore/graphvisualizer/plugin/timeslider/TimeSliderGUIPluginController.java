package jaicore.graphvisualizer.plugin.timeslider;

import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.graphvisualizer.events.graph.bus.HandleAlgorithmEventException;
import jaicore.graphvisualizer.events.gui.GUIEvent;
import jaicore.graphvisualizer.plugin.IGUIPluginController;
import jaicore.graphvisualizer.plugin.controlbar.PauseEvent;
import jaicore.graphvisualizer.plugin.controlbar.PlayEvent;
import jaicore.graphvisualizer.plugin.controlbar.ResetEvent;

public class TimeSliderGUIPluginController implements IGUIPluginController {

	private TimeSliderGUIPluginModel model;

	private int amountOfEventsToIgnore;

	public TimeSliderGUIPluginController(TimeSliderGUIPluginModel model) {
		this.model = model;
		this.amountOfEventsToIgnore = 0;
	}

	@Override
	public void handleAlgorithmEvent(AlgorithmEvent algorithmEvent) throws HandleAlgorithmEventException {
		if (amountOfEventsToIgnore <= 0) {
			model.increaseCurrentTimeStep();
			model.increaseMaximumTimeStep();
		} else {
			amountOfEventsToIgnore--;
		}
	}

	@Override
	public void handleGUIEvent(GUIEvent guiEvent) {
		if (guiEvent instanceof ResetEvent) {
			model.reset();
		} else if (guiEvent instanceof PauseEvent) {
			model.pause();
		} else if (guiEvent instanceof PlayEvent) {
			model.unpause();
		} else if (guiEvent instanceof GoToTimeStepEvent) {
			int newTimeStep = ((GoToTimeStepEvent) guiEvent).getNewTimeStep();
			amountOfEventsToIgnore = newTimeStep;
			model.setCurrentTimeStep(newTimeStep);
		}
	}

}
