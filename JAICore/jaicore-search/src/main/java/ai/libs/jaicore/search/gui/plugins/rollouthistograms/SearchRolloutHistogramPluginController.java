package ai.libs.jaicore.search.gui.plugins.rollouthistograms;

import org.api4.java.algorithm.events.serializable.IPropertyProcessedAlgorithmEvent;

import ai.libs.jaicore.graphvisualizer.events.gui.GUIEvent;
import ai.libs.jaicore.graphvisualizer.plugin.ASimpleMVCPluginController;
import ai.libs.jaicore.graphvisualizer.plugin.controlbar.ResetEvent;
import ai.libs.jaicore.graphvisualizer.plugin.graphview.NodeClickedEvent;
import ai.libs.jaicore.graphvisualizer.plugin.timeslider.GoToTimeStepEvent;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.events.RolloutEvent;

public class SearchRolloutHistogramPluginController extends ASimpleMVCPluginController<SearchRolloutHistogramPluginModel, SearchRolloutHistogramPluginView> {

	public SearchRolloutHistogramPluginController(final SearchRolloutHistogramPluginModel model, final SearchRolloutHistogramPluginView view) {
		super(model, view);
	}

	@Override
	public void handleGUIEvent(final GUIEvent guiEvent) {
		if (guiEvent instanceof ResetEvent || guiEvent instanceof GoToTimeStepEvent) {
			this.getModel().clear();
		} else if (guiEvent instanceof NodeClickedEvent) {
			this.getModel().setCurrentlySelectedNode(((NodeClickedEvent) guiEvent).getSearchGraphNode());
			this.getView().update();
		}
	}

	@Override
	public void handleAlgorithmEventInternally(final IPropertyProcessedAlgorithmEvent algorithmEvent) {
		if (algorithmEvent.correspondsToEventOfClass(RolloutEvent.class)) {

			RolloutInfo rolloutInfo = (RolloutInfo) algorithmEvent.getProperty(RolloutInfoAlgorithmEventPropertyComputer.ROLLOUT_SCORE_PROPERTY_NAME);

			rolloutInfo.getPath().forEach(n -> this.getModel().addEntry(n, (double) rolloutInfo.getScore()));
		}
	}

}
