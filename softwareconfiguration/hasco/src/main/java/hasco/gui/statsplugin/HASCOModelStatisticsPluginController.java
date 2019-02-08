package hasco.gui.statsplugin;

import hasco.events.HASCOSolutionEvent;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.graphvisualizer.events.gui.GUIEvent;
import jaicore.graphvisualizer.plugin.ASimpleMVCPluginController;

public class HASCOModelStatisticsPluginController extends ASimpleMVCPluginController<HASCOModelStatisticsPluginModel, HASCOModelStatisticsPluginView> {

	public HASCOModelStatisticsPluginController(HASCOModelStatisticsPluginModel model, HASCOModelStatisticsPluginView view) {
		super(model, view);
	}

	@Override
	public void handleGUIEvent(GUIEvent guiEvent) {

		/* this view does not react to GUI events */
	}

	@Override
	public void handleAlgorithmEventInternally(AlgorithmEvent algorithmEvent) {
		if (algorithmEvent instanceof HASCOSolutionEvent) {
			getModel().addEntry((HASCOSolutionEvent<Double>) algorithmEvent);
		}
	}

}
