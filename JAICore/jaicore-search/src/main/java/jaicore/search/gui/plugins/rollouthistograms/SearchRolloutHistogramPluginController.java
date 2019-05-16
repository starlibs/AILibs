package jaicore.search.gui.plugins.rollouthistograms;

import jaicore.basic.algorithm.events.serializable.PropertyProcessedAlgorithmEvent;
import jaicore.graphvisualizer.events.gui.GUIEvent;
import jaicore.graphvisualizer.plugin.ASimpleMVCPluginController;
import jaicore.graphvisualizer.plugin.controlbar.ResetEvent;
import jaicore.graphvisualizer.plugin.graphview.NodeClickedEvent;
import jaicore.graphvisualizer.plugin.timeslider.GoToTimeStepEvent;
import jaicore.search.algorithms.standard.bestfirst.events.RolloutEvent;

public class SearchRolloutHistogramPluginController extends ASimpleMVCPluginController<SearchRolloutHistogramPluginModel, SearchRolloutHistogramPluginView> {

	public SearchRolloutHistogramPluginController(SearchRolloutHistogramPluginModel model, SearchRolloutHistogramPluginView view) {
		super(model, view);
	}

	@Override
	public void handleGUIEvent(GUIEvent guiEvent) {
		if (guiEvent instanceof ResetEvent || guiEvent instanceof GoToTimeStepEvent) {
			getModel().clear();
		} else if (guiEvent instanceof NodeClickedEvent) {
			getModel().setCurrentlySelectedNode(((NodeClickedEvent) guiEvent).getSearchGraphNode());
			getView().update();
		}
	}

	@Override
	public void handleAlgorithmEventInternally(PropertyProcessedAlgorithmEvent algorithmEvent) {
		// String eventName = algorithmEvent.getClass().getSimpleName();
		if (algorithmEvent.correspondsToEventOfClass(RolloutEvent.class)) {

			RolloutInfo rolloutInfo = (RolloutInfo) algorithmEvent.getProperty(RolloutInfoAlgorithmEventPropertyComputer.ROLLOUT_SCORE_PROPERTY_NAME);

			rolloutInfo.getPath().forEach(n -> getModel().addEntry(n, (double) rolloutInfo.getScore()));
		}
	}

}
