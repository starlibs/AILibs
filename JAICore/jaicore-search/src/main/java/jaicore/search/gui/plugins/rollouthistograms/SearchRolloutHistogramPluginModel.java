package jaicore.search.gui.plugins.rollouthistograms;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import jaicore.graphvisualizer.plugin.ASimpleMVCPluginModel;

/**
 * 
 * @author fmohr
 *
 * @param <N>
 *            The node type class.
 */
public class SearchRolloutHistogramPluginModel<N> extends ASimpleMVCPluginModel<SearchRolloutHistogramPluginView<N>, SearchRolloutHistogramPluginController<N>> {

	private N currentlySelectedNode;
	private final Map<N, List<Double>> observedPerformances = new HashMap<>();

	public final void addEntry(N node, double score) {
		if (!observedPerformances.containsKey(node))
			observedPerformances.put(node, Collections.synchronizedList(new LinkedList<>()));
		observedPerformances.get(node).add(score);
		getView().update();
	}

	public Map<N, List<Double>> getObservedPerformances() {
		return observedPerformances;
	}
	
	public List<Double> getObservedPerformancesUnderSelectedNode (){
		return observedPerformances.get(currentlySelectedNode);
	}
	
	public void clear() {
		observedPerformances.clear();
		getView().clear();
	}
	
	public void setCurrentlySelectedNode(N currentlySelectedNode) {
		this.currentlySelectedNode = currentlySelectedNode;
		
		getView().update();
	}

	public N getCurrentlySelectedNode() {
		return currentlySelectedNode;
	}
}
