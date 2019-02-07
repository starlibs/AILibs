package jaicore.graphvisualizer.plugin.controlbar;

import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.graphvisualizer.events.graph.bus.HandleAlgorithmEventException;
import jaicore.graphvisualizer.events.gui.GUIEvent;
import jaicore.graphvisualizer.plugin.GUIPluginController;

public class ControlBarGUIPluginController implements GUIPluginController {

	private ControlBarGUIPluginModel model;

	public ControlBarGUIPluginController(ControlBarGUIPluginModel model) {
		this.model = model;
	}

	@Override
	public void handleAlgorithmEvent(AlgorithmEvent algorithmEvent) throws HandleAlgorithmEventException {
		// TODO Auto-generated method stub

	}

	@Override
	public void handleGUIEvent(GUIEvent guiEvent) {
		if (guiEvent instanceof PauseEvent) {
			model.setPaused();
		} else if (guiEvent instanceof PlayEvent || guiEvent instanceof ResetEvent) {
			model.setUnpaused();
		}
	}

}
