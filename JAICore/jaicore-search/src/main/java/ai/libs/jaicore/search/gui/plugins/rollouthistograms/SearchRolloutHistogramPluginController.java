package ai.libs.jaicore.search.gui.plugins.rollouthistograms;

import ai.libs.jaicore.basic.algorithm.events.AlgorithmEvent;
import ai.libs.jaicore.graphvisualizer.events.gui.GUIEvent;
import ai.libs.jaicore.graphvisualizer.plugin.ASimpleMVCPluginController;
import ai.libs.jaicore.graphvisualizer.plugin.controlbar.ResetEvent;
import ai.libs.jaicore.graphvisualizer.plugin.graphview.NodeClickedEvent;
import ai.libs.jaicore.graphvisualizer.plugin.timeslider.GoToTimeStepEvent;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.events.RolloutEvent;

public class SearchRolloutHistogramPluginController<N> extends ASimpleMVCPluginController<SearchRolloutHistogramPluginModel<N>, SearchRolloutHistogramPluginView<N>> {

	public SearchRolloutHistogramPluginController(SearchRolloutHistogramPluginModel<N> model, SearchRolloutHistogramPluginView<N> view) {
		super(model, view);
	}

	@Override
	public void handleGUIEvent(GUIEvent guiEvent) {
		if (guiEvent instanceof ResetEvent || guiEvent instanceof GoToTimeStepEvent) {
			getModel().clear();
		}
		
		else if (guiEvent instanceof NodeClickedEvent) {
			getModel().setCurrentlySelectedNode((N) ((NodeClickedEvent) guiEvent).getSearchGraphNode());
			getView().update();
		}
	}

	@Override
	public void handleAlgorithmEventInternally(AlgorithmEvent algorithmEvent) {
//		String eventName = algorithmEvent.getClass().getSimpleName();
		if (RolloutEvent.class.isInstance(algorithmEvent)) {
			RolloutEvent<N, Double> event = (RolloutEvent<N, Double>) algorithmEvent;
			event.getPath().forEach(n -> getModel().addEntry(n, event.getScore()));
		}
	}

}
