package ai.libs.jaicore.graphvisualizer.plugin.timeslider;

import org.api4.java.algorithm.events.serializable.IPropertyProcessedAlgorithmEvent;

import ai.libs.jaicore.graphvisualizer.events.graph.bus.HandleAlgorithmEventException;
import ai.libs.jaicore.graphvisualizer.events.gui.GUIEvent;
import ai.libs.jaicore.graphvisualizer.plugin.ASimpleMVCPluginController;
import ai.libs.jaicore.graphvisualizer.plugin.controlbar.PauseEvent;
import ai.libs.jaicore.graphvisualizer.plugin.controlbar.PlayEvent;
import ai.libs.jaicore.graphvisualizer.plugin.controlbar.ResetEvent;

public class TimeSliderGUIPluginController extends ASimpleMVCPluginController<TimeSliderGUIPluginModel, TimeSliderGUIPluginView> {

	private int amountOfEventsToIgnore;

	public TimeSliderGUIPluginController(final TimeSliderGUIPluginModel model, final TimeSliderGUIPluginView view) {
		super (model, view);
		this.amountOfEventsToIgnore = 0;
	}

	@Override
	public void handleGUIEvent(final GUIEvent guiEvent) {
		if (guiEvent instanceof ResetEvent) {
			this.getModel().reset();
		} else if (guiEvent instanceof PauseEvent) {
			this.getModel().pause();
		} else if (guiEvent instanceof PlayEvent) {
			this.getModel().unpause();
		} else if (guiEvent instanceof GoToTimeStepEvent) {
			int newTimeStep = ((GoToTimeStepEvent) guiEvent).getNewTimeStep();
			this.amountOfEventsToIgnore = newTimeStep;
			this.getModel().setCurrentTimeStep(newTimeStep);
		}
	}

	@Override
	protected void handleAlgorithmEventInternally(final IPropertyProcessedAlgorithmEvent algorithmEvent) throws HandleAlgorithmEventException {
		if (this.amountOfEventsToIgnore <= 0) {
			this.getModel().increaseCurrentTimeStep();
			this.getModel().increaseMaximumTimeStep();
		} else {
			this.amountOfEventsToIgnore--;
		}
	}

}
