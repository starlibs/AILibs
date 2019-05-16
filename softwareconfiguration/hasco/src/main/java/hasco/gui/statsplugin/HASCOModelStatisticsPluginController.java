package hasco.gui.statsplugin;

import hasco.events.HASCOSolutionEvent;
import jaicore.basic.algorithm.events.serializable.PropertyProcessedAlgorithmEvent;
import jaicore.graphvisualizer.plugin.ASimpleMVCPluginController;

/**
 * 
 * @author fmohr
 * 
 *         The controller of the HASCOModelStatisticsPlugin
 *
 */
public class HASCOModelStatisticsPluginController extends ASimpleMVCPluginController<HASCOModelStatisticsPluginModel, HASCOModelStatisticsPluginView> {

	public HASCOModelStatisticsPluginController(HASCOModelStatisticsPluginModel model, HASCOModelStatisticsPluginView view) {
		super(model, view);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void handleAlgorithmEventInternally(PropertyProcessedAlgorithmEvent algorithmEvent) {
		if (algorithmEvent instanceof HASCOSolutionEvent) {
			getModel().addEntry((HASCOSolutionEvent<Double>) algorithmEvent);
		}
	}

}
