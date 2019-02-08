package jaicore.graphvisualizer.plugin.timeslider;

import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.graphvisualizer.events.graph.bus.HandleAlgorithmEventException;
import jaicore.graphvisualizer.events.gui.GUIEvent;
import jaicore.graphvisualizer.plugin.GUIPluginController;
import jaicore.graphvisualizer.plugin.controlbar.ResetEvent;

public class TimeSliderGUIPluginController implements GUIPluginController {

	private TimeSliderGUIPluginModel model;

	public TimeSliderGUIPluginController(TimeSliderGUIPluginModel model) {
		this.model = model;
	}

	@Override
	public void handleAlgorithmEvent(AlgorithmEvent algorithmEvent) throws HandleAlgorithmEventException {
		model.increaseCurrentTimeStep();
		model.increaseMaximumTimeStep();
	}

	@Override
	public void handleGUIEvent(GUIEvent guiEvent) {
		if (guiEvent instanceof ResetEvent) {
			model.reset();
		}
	}

}
