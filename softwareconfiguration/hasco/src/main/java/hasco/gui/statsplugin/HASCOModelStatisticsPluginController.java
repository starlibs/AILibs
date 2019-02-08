package hasco.gui.statsplugin;


import hasco.events.HASCOSolutionEvent;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.graphvisualizer.events.graph.bus.HandleAlgorithmEventException;
import jaicore.graphvisualizer.events.gui.GUIEvent;
import jaicore.graphvisualizer.plugin.ASimpleMVCPluginController;

public class HASCOModelStatisticsPluginController extends ASimpleMVCPluginController<HASCOModelStatisticsPluginModel, HASCOModelStatisticsPluginView> {

	public HASCOModelStatisticsPluginController(HASCOModelStatisticsPluginModel model, HASCOModelStatisticsPluginView view) {
		super(model, view);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void handleAlgorithmEvent(AlgorithmEvent algorithmEvent) throws HandleAlgorithmEventException {
		if (algorithmEvent instanceof HASCOSolutionEvent) {
			getModel().addEntry((HASCOSolutionEvent<Double>)algorithmEvent);
		}
	}

	@Override
	public void handleGUIEvent(GUIEvent guiEvent) {
		
		/* this view does not react to GUI events */
	}


}
