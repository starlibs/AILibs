package hasco.gui.statsplugin;

import hasco.events.HASCOSolutionEvent;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.graphvisualizer.plugin.ASimpleMVCPluginController;

public class HASCOModelStatisticsPluginController extends ASimpleMVCPluginController<HASCOModelStatisticsPluginModel, HASCOModelStatisticsPluginView> {

	public HASCOModelStatisticsPluginController(HASCOModelStatisticsPluginModel model, HASCOModelStatisticsPluginView view) {
		super(model, view);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void handleAlgorithmEventInternally(AlgorithmEvent algorithmEvent) {
		if (algorithmEvent instanceof HASCOSolutionEvent) {
			getModel().addEntry((HASCOSolutionEvent<Double>) algorithmEvent);
		}
	}

}
