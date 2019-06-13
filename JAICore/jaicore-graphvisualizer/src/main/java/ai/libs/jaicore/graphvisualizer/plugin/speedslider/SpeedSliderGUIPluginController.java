package ai.libs.jaicore.graphvisualizer.plugin.speedslider;

import ai.libs.jaicore.basic.algorithm.events.AlgorithmEvent;
import ai.libs.jaicore.graphvisualizer.events.graph.bus.HandleAlgorithmEventException;
import ai.libs.jaicore.graphvisualizer.events.gui.GUIEvent;
import ai.libs.jaicore.graphvisualizer.plugin.IGUIPluginController;

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
