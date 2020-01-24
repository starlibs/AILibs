package ai.libs.jaicore.graphvisualizer.plugin.speedslider;

import org.api4.java.algorithm.events.serializable.IPropertyProcessedAlgorithmEvent;

import ai.libs.jaicore.graphvisualizer.events.graph.bus.HandleAlgorithmEventException;
import ai.libs.jaicore.graphvisualizer.events.gui.GUIEvent;
import ai.libs.jaicore.graphvisualizer.plugin.ASimpleMVCPluginController;

public class SpeedSliderGUIPluginController extends ASimpleMVCPluginController<SpeedSliderGUIPluginModel, SpeedSliderGUIPluginView> {

	public SpeedSliderGUIPluginController(final SpeedSliderGUIPluginModel model, final SpeedSliderGUIPluginView view) {
		super (model, view);
	}

	@Override
	public void handleGUIEvent(final GUIEvent guiEvent) {
		if (guiEvent instanceof ChangeSpeedEvent) {
			ChangeSpeedEvent changeSpeedEvent = (ChangeSpeedEvent) guiEvent;
			this.getModel().setCurrentSpeedPercentage(changeSpeedEvent.getNewSpeedPercentage());
		}
	}

	@Override
	protected void handleAlgorithmEventInternally(final IPropertyProcessedAlgorithmEvent algorithmEvent) throws HandleAlgorithmEventException {
		/* no need to handle any algorithm events */
	}

}
