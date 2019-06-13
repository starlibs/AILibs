package ai.libs.hasco.gui.statsplugin;

import ai.libs.hasco.events.HASCOSolutionEvent;
import ai.libs.jaicore.basic.algorithm.events.AlgorithmEvent;
import ai.libs.jaicore.graphvisualizer.plugin.ASimpleMVCPluginController;

/**
 * 
 * @author fmohr
 * 
 * The controller of the HASCOModelStatisticsPlugin
 *
 */
public class HASCOModelStatisticsPluginController extends ASimpleMVCPluginController<HASCOModelStatisticsPluginModel, HASCOModelStatisticsPluginView> {

	public HASCOModelStatisticsPluginController(HASCOModelStatisticsPluginModel model, HASCOModelStatisticsPluginView view) {
		super(model, view);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void handleAlgorithmEventInternally(AlgorithmEvent algorithmEvent) {
		if (algorithmEvent instanceof HASCOSolutionEvent) {
			getModel().addEntry((HASCOSolutionEvent<Double>) algorithmEvent);
		}
	}

}
