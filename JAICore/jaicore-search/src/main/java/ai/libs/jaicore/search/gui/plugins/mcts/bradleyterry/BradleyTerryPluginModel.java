package ai.libs.jaicore.search.gui.plugins.mcts.bradleyterry;

import java.util.HashMap;
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
public class BradleyTerryPluginModel extends ASimpleMVCPluginModel<BradleyTerryPluginView, BradleyTerryPluginController> {

	private String currentlySelectedNode = "0";
	private final Map<String, String> parents = new HashMap<>();
	private final Map<String, List<String>> listsOfKnownSuccessors = new HashMap<>();
	private final Map<String, BradleyTerryUpdate> btUpdates = new HashMap<>();

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

	public void setNodeStats(final BradleyTerryUpdate update) {
		if (update == null) {
			throw new IllegalArgumentException("Cannot process NULL update");
		}
		String node = update.getNode();
		if (!this.listsOfKnownSuccessors.containsKey(node)) {
			throw new IllegalArgumentException("Cannot receive update for an unknown node. Make sure that Rollout events are processed!");
		}
		this.btUpdates.put(node, update);
		if (node.equals(this.getCurrentlySelectedNode())) {
			this.getView().update();
		}
	}

	public Map<String, BradleyTerryUpdate> getBtUpdates() {
		return this.btUpdates;
	}

	public BradleyTerryUpdate getUpdateOfSelectedNode() {
		return this.btUpdates.get(this.getCurrentlySelectedNode());
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
