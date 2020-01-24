package ai.libs.jaicore.graphvisualizer.plugin.controlbar;

import org.api4.java.algorithm.events.serializable.IPropertyProcessedAlgorithmEvent;

import ai.libs.jaicore.graphvisualizer.events.graph.bus.HandleAlgorithmEventException;
import ai.libs.jaicore.graphvisualizer.events.gui.GUIEvent;
import ai.libs.jaicore.graphvisualizer.plugin.ASimpleMVCPluginController;

public class ControlBarGUIPluginController extends ASimpleMVCPluginController<ControlBarGUIPluginModel, ControlBarGUIPluginView> {

	public ControlBarGUIPluginController(final ControlBarGUIPluginModel model, final ControlBarGUIPluginView view) {
		super(model, view);
	}

	@Override
	public void handleGUIEvent(final GUIEvent guiEvent) {
		if (guiEvent instanceof PauseEvent || guiEvent instanceof ResetEvent) {
			this.getModel().setPaused();
		} else if (guiEvent instanceof PlayEvent) {
			this.getModel().setUnpaused();
		}
	}

	@Override
	protected void handleAlgorithmEventInternally(final IPropertyProcessedAlgorithmEvent algorithmEvent) throws HandleAlgorithmEventException {
		// nothing to do here as the control bar does not need to handle any algorithm event
	}

}
