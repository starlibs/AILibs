package ai.libs.jaicore.graphvisualizer.plugin.timeslider;

import org.api4.java.algorithm.events.serializable.IPropertyProcessedAlgorithmEvent;

import ai.libs.jaicore.graphvisualizer.events.graph.bus.HandleAlgorithmEventException;
import ai.libs.jaicore.graphvisualizer.events.gui.GUIEvent;
import ai.libs.jaicore.graphvisualizer.plugin.IGUIPluginController;
import ai.libs.jaicore.graphvisualizer.plugin.controlbar.PauseEvent;
import ai.libs.jaicore.graphvisualizer.plugin.controlbar.PlayEvent;
import ai.libs.jaicore.graphvisualizer.plugin.controlbar.ResetEvent;

public class TimeSliderGUIPluginController implements IGUIPluginController {

	private TimeSliderGUIPluginModel model;

	private int amountOfEventsToIgnore;

	public TimeSliderGUIPluginController(final TimeSliderGUIPluginModel model) {
		this.model = model;
		this.amountOfEventsToIgnore = 0;
	}

	@Override
	public void handleSerializableAlgorithmEvent(final IPropertyProcessedAlgorithmEvent algorithmEvent) throws HandleAlgorithmEventException {
		if (this.amountOfEventsToIgnore <= 0) {
			this.model.increaseCurrentTimeStep();
			this.model.increaseMaximumTimeStep();
		} else {
			this.amountOfEventsToIgnore--;
		}
	}

	@Override
	public void handleGUIEvent(final GUIEvent guiEvent) {
		if (guiEvent instanceof ResetEvent) {
			this.model.reset();
		} else if (guiEvent instanceof PauseEvent) {
			this.model.pause();
		} else if (guiEvent instanceof PlayEvent) {
			this.model.unpause();
		} else if (guiEvent instanceof GoToTimeStepEvent) {
			int newTimeStep = ((GoToTimeStepEvent) guiEvent).getNewTimeStep();
			this.amountOfEventsToIgnore = newTimeStep;
			this.model.setCurrentTimeStep(newTimeStep);
		}
	}

}
