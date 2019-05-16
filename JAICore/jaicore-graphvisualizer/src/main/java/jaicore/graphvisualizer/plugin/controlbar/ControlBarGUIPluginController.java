package jaicore.graphvisualizer.plugin.controlbar;

import jaicore.basic.algorithm.events.serializable.PropertyProcessedAlgorithmEvent;
import jaicore.graphvisualizer.events.graph.bus.HandleAlgorithmEventException;
import jaicore.graphvisualizer.events.gui.GUIEvent;
import jaicore.graphvisualizer.plugin.IGUIPluginController;

public class ControlBarGUIPluginController implements IGUIPluginController {

	private ControlBarGUIPluginModel model;

	public ControlBarGUIPluginController(ControlBarGUIPluginModel model) {
		this.model = model;
	}

	@Override
	public void handleSerializableAlgorithmEvent(PropertyProcessedAlgorithmEvent algorithmEvent) throws HandleAlgorithmEventException {
		// nothing to do here as the control bar does not need to handle any algorithm event
	}

	@Override
	public void handleGUIEvent(GUIEvent guiEvent) {
		if (guiEvent instanceof PauseEvent || guiEvent instanceof ResetEvent) {
			model.setPaused();
		} else if (guiEvent instanceof PlayEvent) {
			model.setUnpaused();
		}
	}

}
