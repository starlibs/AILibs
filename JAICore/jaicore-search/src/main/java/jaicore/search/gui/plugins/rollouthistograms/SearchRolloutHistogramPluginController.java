package jaicore.search.gui.plugins.rollouthistograms;

import jaicore.basic.ScoredItem;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.events.SolutionCandidateFoundEvent;
import jaicore.graphvisualizer.events.graph.bus.HandleAlgorithmEventException;
import jaicore.graphvisualizer.events.gui.GUIEvent;
import jaicore.graphvisualizer.plugin.ASimpleMVCPluginController;

public class SearchRolloutHistogramPluginController extends ASimpleMVCPluginController<SearchRolloutHistogramPluginModel, SearchRolloutHistogramPluginView> {

	public SearchRolloutHistogramPluginController(SearchRolloutHistogramPluginModel model, SearchRolloutHistogramPluginView view) {
		super(model, view);
		System.out.println("HASLL");
	}

	@SuppressWarnings("unchecked")
	@Override
	public void handleAlgorithmEvent(AlgorithmEvent algorithmEvent) throws HandleAlgorithmEventException {
		
		System.out.println(algorithmEvent.getClass().getSimpleName());
		if (algorithmEvent instanceof SolutionCandidateFoundEvent) {
			System.err.println(algorithmEvent);
			getModel().addEntry((SolutionCandidateFoundEvent<? extends ScoredItem<Double>>) algorithmEvent);
		}
	}

	@Override
	public void handleGUIEvent(GUIEvent guiEvent) {
		
		/* this view does not react to GUI events */
	}


}
