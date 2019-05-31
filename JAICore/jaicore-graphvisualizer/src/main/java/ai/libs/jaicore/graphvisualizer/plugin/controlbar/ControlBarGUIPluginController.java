package ai.libs.jaicore.graphvisualizer.plugin.controlbar;

import ai.libs.jaicore.basic.algorithm.events.AlgorithmEvent;
import ai.libs.jaicore.graphvisualizer.events.graph.bus.HandleAlgorithmEventException;
import ai.libs.jaicore.graphvisualizer.events.gui.GUIEvent;
import ai.libs.jaicore.graphvisualizer.plugin.IGUIPluginController;

public class ControlBarGUIPluginController implements IGUIPluginController {

	private ControlBarGUIPluginModel model;

	public ControlBarGUIPluginController(ControlBarGUIPluginModel model) {
		this.model = model;
	}

	@Override
	public void handleAlgorithmEvent(AlgorithmEvent algorithmEvent) throws HandleAlgorithmEventException {
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
