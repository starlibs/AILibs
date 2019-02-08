package jaicore.search.gui.plugins.rollouthistograms;

import java.util.ArrayList;
import java.util.List;

import jaicore.basic.ScoredItem;
import jaicore.basic.algorithm.events.SolutionCandidateFoundEvent;
import jaicore.graphvisualizer.plugin.ASimpleMVCPluginModel;

/**
 * 
 * @author fmohr
 *
 * @param <N>
 *            The node type class.
 */
public class SearchRolloutHistogramPluginModel extends ASimpleMVCPluginModel<SearchRolloutHistogramPluginView, SearchRolloutHistogramPluginController> {

	private final List<Double> observedPerformances = new ArrayList<>();
	
	public final void addEntry(SolutionCandidateFoundEvent<? extends ScoredItem<Double>> solutionEvent) {
		observedPerformances.add(solutionEvent.getSolutionCandidate().getScore());
		getView().update();
	}

	public List<Double> getObservedPerformances() {
		return observedPerformances;
	}
}
