package jaicore.graphvisualizer.plugin.speedslider;

import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.graphvisualizer.events.graph.bus.HandleAlgorithmEventException;
import jaicore.graphvisualizer.events.gui.GUIEvent;
import jaicore.graphvisualizer.plugin.IGUIPluginController;

public class SpeedSliderGUIPluginController implements IGUIPluginController {

	private SpeedSliderGUIPluginModel model;

	public SpeedSliderGUIPluginController(SpeedSliderGUIPluginModel model) {
		this.model = model;
	}

	@Override
	public void handleAlgorithmEvent(AlgorithmEvent algorithmEvent) throws HandleAlgorithmEventException {
		// no need to handle any algorithm events
	}

	@Override
	public void handleGUIEvent(GUIEvent guiEvent) {
		if (guiEvent instanceof ChangeSpeedEvent) {
			ChangeSpeedEvent changeSpeedEvent = (ChangeSpeedEvent) guiEvent;
			model.setCurrentSpeedPercentage(changeSpeedEvent.getNewSpeedPercentage());
		}
	}

}
