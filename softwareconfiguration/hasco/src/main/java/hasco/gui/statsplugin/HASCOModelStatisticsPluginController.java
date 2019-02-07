package hasco.gui.statsplugin;


import hasco.events.HASCOSolutionEvent;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.graphvisualizer.events.graph.bus.HandleAlgorithmEventException;
import jaicore.graphvisualizer.events.gui.GUIEvent;
import jaicore.graphvisualizer.plugin.GUIPluginController;

public class HASCOModelStatisticsPluginController implements GUIPluginController {

	private HASCOModelStatisticsPluginModel model;

	public HASCOModelStatisticsPluginController(HASCOModelStatisticsPluginModel model) {
		this.model = model;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void handleAlgorithmEvent(AlgorithmEvent algorithmEvent) throws HandleAlgorithmEventException {
		if (algorithmEvent instanceof HASCOSolutionEvent) {
			model.addEntry((HASCOSolutionEvent<Double>)algorithmEvent);
		}
	}

	@Override
	public void handleGUIEvent(GUIEvent guiEvent) {
		
		/* this view does not react to GUI events */
	}


}
