package ai.libs.jaicore.graphvisualizer.plugin.speedslider;

import org.api4.java.algorithm.events.serializable.IPropertyProcessedAlgorithmEvent;

import ai.libs.jaicore.graphvisualizer.events.graph.bus.HandleAlgorithmEventException;
import ai.libs.jaicore.graphvisualizer.events.gui.GUIEvent;
import ai.libs.jaicore.graphvisualizer.plugin.IGUIPluginController;

public class SpeedSliderGUIPluginController implements IGUIPluginController {

	private SpeedSliderGUIPluginModel model;

	public SpeedSliderGUIPluginController(final SpeedSliderGUIPluginModel model) {
		this.model = model;
	}

	@Override
	public void handleSerializableAlgorithmEvent(final IPropertyProcessedAlgorithmEvent algorithmEvent) throws HandleAlgorithmEventException {
		// no need to handle any algorithm events
	}

	@Override
	public void handleGUIEvent(final GUIEvent guiEvent) {
		if (guiEvent instanceof ChangeSpeedEvent) {
			ChangeSpeedEvent changeSpeedEvent = (ChangeSpeedEvent) guiEvent;
			this.model.setCurrentSpeedPercentage(changeSpeedEvent.getNewSpeedPercentage());
		}
	}

}
