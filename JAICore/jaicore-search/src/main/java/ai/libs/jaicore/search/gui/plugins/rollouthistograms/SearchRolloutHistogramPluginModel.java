package ai.libs.jaicore.search.gui.plugins.rollouthistograms;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ai.libs.jaicore.graphvisualizer.plugin.ASimpleMVCPluginModel;

/**
 * 
 * @author fmohr
 *
 * @param <BalancedTreeNode>
 *            The node type class.
 */
public class SearchRolloutHistogramPluginModel extends ASimpleMVCPluginModel<SearchRolloutHistogramPluginView, SearchRolloutHistogramPluginController> {

	private String currentlySelectedNode;
	private final Map<String, List<Double>> observedPerformances = new HashMap<>();

	public final void addEntry(String node, double score) {
		if (!observedPerformances.containsKey(node)) {
			observedPerformances.put(node, Collections.synchronizedList(new LinkedList<>()));
		}
		observedPerformances.get(node).add(score);
		getView().update();
	}

	public Map<String, List<Double>> getObservedPerformances() {
		return observedPerformances;
	}

	public List<Double> getObservedPerformancesUnderSelectedNode() {
		return observedPerformances.get(currentlySelectedNode);
	}

	@Override
	public void clear() {
		observedPerformances.clear();
		getView().clear();
	}

	public void setCurrentlySelectedNode(String currentlySelectedNode) {
		this.currentlySelectedNode = currentlySelectedNode;
		getView().clear();
		getView().update();
	}

	public String getCurrentlySelectedNode() {
		return currentlySelectedNode;
	}
}
