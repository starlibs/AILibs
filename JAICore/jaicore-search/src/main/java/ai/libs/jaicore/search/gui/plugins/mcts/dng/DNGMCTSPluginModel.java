package ai.libs.jaicore.search.gui.plugins.mcts.dng;

import java.util.ArrayList;
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
public class DNGMCTSPluginModel extends ASimpleMVCPluginModel<DNGMCTSPluginView, DNGMCTSPluginController> {

	private String currentlySelectedNode = "0";
	private final Map<String, String> parents = new HashMap<>();
	private final Map<String, List<String>> listsOfKnownSuccessors = new HashMap<>();
	private final Map<String, List<Double>> listOfObersvationsPerNode = new HashMap<>();
	private final Map<String, Map<String, List<Double>>> observedQValues = new HashMap<>();
	private final Map<String, List<DNGBeliefUpdate>> observedUpdates = new HashMap<>();

	@Override
	public void clear() {
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

	public void addObservation(final String node, final double score) {
		this.listOfObersvationsPerNode.computeIfAbsent(node, n -> new ArrayList<>()).add(score);
	}

	public void setNodeStats(final DNGQSample update) {
		if (update == null) {
			throw new IllegalArgumentException("Cannot process NULL update");
		}
		String node = update.getNode();
		if (!this.listsOfKnownSuccessors.containsKey(node)) {
			throw new IllegalArgumentException("Cannot receive update for an unknown node. Make sure that Rollout events are processed!");
		}
		this.observedQValues.computeIfAbsent(node, n -> new HashMap<>()).computeIfAbsent(update.getSuccessor(), n2 -> new ArrayList<>()).add(update.getScore());
		if (node.equals(this.getCurrentlySelectedNode())) {
			this.getView().update();
		}
	}

	public void setNodeStats(final DNGBeliefUpdate update) {
		if (update == null) {
			throw new IllegalArgumentException("Cannot process NULL update");
		}
		String node = update.getNode();
		this.observedUpdates.computeIfAbsent(node, n -> new ArrayList<>()).add(update);
		if (node.equals(this.getCurrentlySelectedNode())) {
			this.getView().update();
		}
	}

	public Map<String, List<Double>> getQValuesOfNode(final String node) {
		return this.observedQValues.get(node);
	}

	public Map<String, List<Double>> getQValuesOfSelectedNode() {
		return this.observedQValues.get(this.getCurrentlySelectedNode());
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

	public Map<String, List<DNGBeliefUpdate>> getObservedMuValues() {
		return this.observedUpdates;
	}

	public List<DNGBeliefUpdate> getObservedMuValuesOfCurrentlySelectedNode() {
		return this.observedUpdates.get(this.getCurrentlySelectedNode());
	}

	public Map<String, List<Double>> getListOfObersvationsPerNode() {
		return this.listOfObersvationsPerNode;
	}

	public DescriptiveStatistics getObservationStatisticsOfNode(final String node) {
		DescriptiveStatistics stats = new DescriptiveStatistics();
		for (double val : this.listOfObersvationsPerNode.get(node)) {
			stats.addValue(val);
		}
		return stats;
	}
}
