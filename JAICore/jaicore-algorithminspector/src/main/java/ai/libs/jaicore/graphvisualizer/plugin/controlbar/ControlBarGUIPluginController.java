package ai.libs.jaicore.graphvisualizer.plugin.controlbar;

import org.api4.java.algorithm.events.serializable.IPropertyProcessedAlgorithmEvent;

import ai.libs.jaicore.graphvisualizer.events.graph.bus.HandleAlgorithmEventException;
import ai.libs.jaicore.graphvisualizer.events.gui.GUIEvent;
import ai.libs.jaicore.graphvisualizer.plugin.IGUIPluginController;

public class ControlBarGUIPluginController implements IGUIPluginController {

	private ControlBarGUIPluginModel model;

	public ControlBarGUIPluginController(final ControlBarGUIPluginModel model) {
		this.model = model;
	}

	@Override
	public void handleSerializableAlgorithmEvent(final IPropertyProcessedAlgorithmEvent algorithmEvent) throws HandleAlgorithmEventException {
		// nothing to do here as the control bar does not need to handle any algorithm event
	}

	@Override
	public void handleGUIEvent(final GUIEvent guiEvent) {
		if (guiEvent instanceof PauseEvent || guiEvent instanceof ResetEvent) {
			this.model.setPaused();
		} else if (guiEvent instanceof PlayEvent) {
			this.model.setUnpaused();
		}
	}

}
