package ai.libs.jaicore.search.algorithms.mdp.mcts.uct;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.api4.java.common.control.ILoggingCustomizable;
import org.api4.java.datastructure.graph.ILabeledPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.search.algorithms.mdp.mcts.IPathUpdatablePolicy;

public abstract class AUpdatingPolicy<N, A> implements IPathUpdatablePolicy<N, A, Double>, ILoggingCustomizable {

	private String loggerName;
	private Logger logger = LoggerFactory.getLogger(AUpdatingPolicy.class);

	private final boolean maximize;

	public AUpdatingPolicy() {
		this(true);
	}

	public AUpdatingPolicy(final boolean maximize) {
		this.maximize = maximize;
	}

	public class NodeLabel {
		protected int visits;
		protected Map<A, Integer> numberOfChoicesPerAction = new HashMap<>();
		protected Map<A, Double> accumulatedRewardsOfAction = new HashMap<>();
	}

	private final Map<N, NodeLabel> labels = new HashMap<>();

	public NodeLabel getLabelOfNode(final N node) {
		if (!this.labels.containsKey(node)) {
			throw new IllegalArgumentException("No label for node " + node);
		}
		return this.labels.get(node);
	}

	public abstract double getScore(N node, A action);

	public abstract A getActionBasedOnScores(Map<A, Double> scores);

	/**
	 * Note that this is a transposition-based and hence, only partially path-dependent, update.
	 * The labels are associated to nodes of the original MDP (states) and not to nodes in the MCTS search tree (paths)!
	 * This means that, in fact, several paths are (partially) updated simultanously.
	 * However, on all other paths crossing the nodes on the updated paths, only those situations are updated and not the situations
	 * in higher nodes of the search tree.
	 *
	 */
	@Override
	public void updatePath(final ILabeledPath<N, A> path, final Double score) {
		this.logger.debug("Updating path {} with score {}", path, score);
		if (path.isPoint()) {
			throw new IllegalArgumentException("Cannot update path consisting only of the root.");
		}
		for (N node : path.getPathToParentOfHead().getNodes()) {
			A action = path.getOutArc(node);
			NodeLabel label = this.labels.computeIfAbsent(node, n -> new NodeLabel());
			double rewardsBefore = label.accumulatedRewardsOfAction.computeIfAbsent(action, a -> 0.0);
			int numPullsBefore = label.numberOfChoicesPerAction.computeIfAbsent(action, a -> 0);
			label.accumulatedRewardsOfAction.put(action,  rewardsBefore + score);
			label.numberOfChoicesPerAction.put(action, numPullsBefore + 1);
			label.visits++;
			this.logger.trace("Updated label of node {}. Visits now {}. Cummulative changed from {} to {}. Mean changed from {} to {}", node, label.visits, rewardsBefore, rewardsBefore + score, rewardsBefore / numPullsBefore, (rewardsBefore + score) / (numPullsBefore + 1));
		}
		this.logger.debug("Path update completed.");
	}

	@Override
	public A getAction(final N node, final Collection<A> possibleActions) {
		this.logger.debug("Deriving action for node {}. The {} options are: {}", node, possibleActions.size(), possibleActions);

		/* if an applicable action has not been tried, play it to get some initial idea */
		List<A> actionsThatHaveNotBeenTriedYet = possibleActions.stream().filter(a -> !this.labels.containsKey(node)).collect(Collectors.toList());
		if (!actionsThatHaveNotBeenTriedYet.isEmpty()) {
			A action = actionsThatHaveNotBeenTriedYet.get(0);
			this.logger.info("Dictating action {}, because this was never played before.", action);
			return action;
		}

		/* otherwise, play best action */
		NodeLabel labelOfNode = this.labels.get(node);
		this.logger.debug("All actions have been tried. Label is: {}", labelOfNode);
		Map<A, Double> scores = new HashMap<>();
		for (A action : possibleActions) {
			assert labelOfNode.visits != 0 : "Visits of action " + action + " cannot be 0 if we already used this action before!";
			this.logger.trace("Considering action {}, which has {} visits and cummulative rewards {}.", action, labelOfNode.numberOfChoicesPerAction.get(action), labelOfNode.accumulatedRewardsOfAction.get(action));
			Double score = this.getScore(node, action);
			if (score.isNaN()) {
				throw new IllegalStateException("Score of action " + action + " is NaN, which it must not be!");
			}
			scores.put(action, score);
			assert !score.isNaN() : "The score of action " + action + " is NaN, which cannot be the case.";
		}
		A choice = this.getActionBasedOnScores(scores);

		/* quick sanity check */
		if (choice == null) {
			throw new IllegalStateException("Would return null, but this must not be the case! Check the method that chooses an action given the scores.");
		}
		this.logger.info("Recommending action {}.", choice);
		return choice;
	}

	public boolean isMaximize() {
		return this.maximize;
	}

	@Override
	public String getLoggerName() {
		return this.loggerName;
	}

	@Override
	public void setLoggerName(final String name) {
		this.loggerName = name;
		this.logger = LoggerFactory.getLogger(name);
		this.logger.info("Set logger of {} to {}", this, name);
	}

}
