package ai.libs.jaicore.search.gui.plugins.rolloutboxplots;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import ai.libs.jaicore.graphvisualizer.plugin.ASimpleMVCPluginModel;

/**
 *
 * @author fmohr
 *
 * @param <BalancedTreeNode>
 *            The node type class.
 */
public class SearchRolloutBoxplotPluginModel extends ASimpleMVCPluginModel<SearchRolloutBoxplotPluginView, SearchRolloutBoxplotPluginController> {

	private String currentlySelectedNode = "0";
	private final Map<String, String> parents = new HashMap<>();
	private final Map<String, List<String>> listsOfKnownSuccessors = new HashMap<>();
	private final Map<String, DescriptiveStatistics> observedPerformances = new HashMap<>();

	public final void addEntry(final String node, final double score) {
		this.observedPerformances.computeIfAbsent(node, n -> new DescriptiveStatistics()).addValue(score);
		this.getView().update();
	}

	public Map<String, DescriptiveStatistics> getObservedPerformances() {
		return this.observedPerformances;
	}

	public DescriptiveStatistics getObservedPerformancesUnderSelectedNode() {
		return this.observedPerformances.get(this.currentlySelectedNode);
	}

	@Override
	public void clear() {
		this.observedPerformances.clear();
		this.getView().clear();
	}

	public void setCurrentlySelectedNode(final String currentlySelectedNode) {
		this.currentlySelectedNode = currentlySelectedNode;
		this.getView().clear();
		this.getView().update();
	}

	public String getCurrentlySelectedNode() {
		return this.currentlySelectedNode;
	}

	public Map<String, List<String>> getListsOfKnownSuccessors() {
		return this.listsOfKnownSuccessors;
	}

	public List<String> getListOfKnownSuccessorsOfCurrentlySelectedNode() {
		return this.listsOfKnownSuccessors.get(this.getCurrentlySelectedNode());
	}

	public Map<String, String> getParents() {
		return this.parents;
	}

	public String getParentOfCurrentNode() {
		return this.parents.get(this.getCurrentlySelectedNode());
	}
}
