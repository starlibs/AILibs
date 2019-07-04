package ai.libs.jaicore.search.gui.plugins.rollouthistograms;

import ai.libs.jaicore.basic.algorithm.events.serializable.PropertyProcessedAlgorithmEvent;
import ai.libs.jaicore.graphvisualizer.events.gui.GUIEvent;
import ai.libs.jaicore.graphvisualizer.plugin.ASimpleMVCPluginController;
import ai.libs.jaicore.graphvisualizer.plugin.controlbar.ResetEvent;
import ai.libs.jaicore.graphvisualizer.plugin.graphview.NodeClickedEvent;
import ai.libs.jaicore.graphvisualizer.plugin.timeslider.GoToTimeStepEvent;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.events.RolloutEvent;

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
