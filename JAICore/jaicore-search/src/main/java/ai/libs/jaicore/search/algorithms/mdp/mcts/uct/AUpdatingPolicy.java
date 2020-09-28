package ai.libs.jaicore.search.algorithms.mdp.mcts.uct;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.api4.java.common.control.ILoggingCustomizable;
import org.api4.java.datastructure.graph.ILabeledPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.sets.SetUtil;
import ai.libs.jaicore.search.algorithms.mdp.mcts.EBehaviorForNotFullyExploredStates;
import ai.libs.jaicore.search.algorithms.mdp.mcts.IPathUpdatablePolicy;
import ai.libs.jaicore.search.algorithms.mdp.mcts.NodeLabel;

public abstract class AUpdatingPolicy<N, A> implements IPathUpdatablePolicy<N, A, Double>, ILoggingCustomizable {

	private Logger logger = LoggerFactory.getLogger(AUpdatingPolicy.class);

	private final double gamma; // discount factor to consider when interpreting the scores
	private final boolean maximize;

	private EBehaviorForNotFullyExploredStates behaviorWhenActionForNotFullyExploredStateIsRequested;

	private final Map<N, NodeLabel<A>> labels = new HashMap<>();

	public AUpdatingPolicy(final double gamma, final boolean maximize) {
		super();
		this.gamma = gamma;
		this.maximize = maximize;
	}

	public NodeLabel<A> getLabelOfNode(final N node) {
		if (!this.labels.containsKey(node)) {
			throw new IllegalArgumentException("No label for node " + node);
		}
		return this.labels.get(node);
	}

	public abstract double getScore(N node, A action);

	public abstract A getActionBasedOnScores(Map<A, Double> scores);

	/**
	 * Note that this is a transposition-based and hence, only partially path-dependent, update. The labels are associated to nodes of the original MDP (states) and not to nodes in the MCTS search tree (paths)! This means that, in fact,
	 * several paths are (partially) updated simultanously. However, on all other paths crossing the nodes on the updated paths, only those situations are updated and not the situations in higher nodes of the search tree.
	 *
	 */
	@Override
	public void updatePath(final ILabeledPath<N, A> path, final List<Double> scores) {
		this.logger.debug("Updating path {} with score {}", path, scores);
		if (path.isPoint()) {
			throw new IllegalArgumentException("Cannot update path consisting only of the root.");
		}

		List<N> nodes = path.getNodes();
		List<A> arcs = path.getArcs();
		int l = nodes.size();
		double accumulatedDiscountedReward = 0;
		for (int i = l - 2; i >= 0; i--) { // update bottom up
			N node = nodes.get(i);
			A action = arcs.get(i);
			NodeLabel<A> label = this.labels.computeIfAbsent(node, n -> new NodeLabel<>());
			double rewardForThisAction = scores.get(i) != null ? scores.get(i) : Double.NaN;
			accumulatedDiscountedReward = rewardForThisAction + this.gamma * accumulatedDiscountedReward;
			label.addRewardForAction(action, accumulatedDiscountedReward);
			label.addPull(action);
			label.addVisit();
			this.logger.trace("Updated label of node {}. Visits now {}. Action pulls of {} now {}. Observed total rewards for this action: {}", node, label.getVisits(), action, label.getNumPulls(action),
					label.getAccumulatedRewardsOfAction(action));
		}
		this.logger.debug("Path update completed.");
	}

	@Override
	public A getAction(final N node, final Collection<A> possibleActions) {
		this.logger.debug("Deriving action for node {}. The {} options are: {}", node, possibleActions.size(), possibleActions);

		/* if an applicable action has not been tried, play it to get some initial idea */
		List<A> actionsThatHaveNotBeenTriedYet = possibleActions.stream().filter(a -> !this.labels.containsKey(node)).collect(Collectors.toList());
		if (!actionsThatHaveNotBeenTriedYet.isEmpty()) {
			if (this.behaviorWhenActionForNotFullyExploredStateIsRequested == EBehaviorForNotFullyExploredStates.EXCEPTION) {
				throw new IllegalStateException("Tree policy should only be consulted for nodes for which each child has been used at least once.");
			}
			else if (this.behaviorWhenActionForNotFullyExploredStateIsRequested == EBehaviorForNotFullyExploredStates.BEST) {
				throw new UnsupportedOperationException("Can currently only work with RANDOM or EXCEPTION");
			}
			A action = actionsThatHaveNotBeenTriedYet.get(0);
			this.logger.info("Dictating action {}, because this was never played before.", action);
			return action;
		}

		/* otherwise, play best action */
		NodeLabel<A> labelOfNode = this.labels.get(node);
		this.logger.debug("All actions have been tried. Label is: {}", labelOfNode);
		Map<A, Double> scores = new HashMap<>();
		for (A action : possibleActions) {
			assert labelOfNode.getVisits() != 0 : "Visits of action " + action + " cannot be 0 if we already used this action before!";
			this.logger.trace("Considering action {}, which has {} visits and cummulative rewards {}.", action, labelOfNode.getNumPulls(action), labelOfNode.getAccumulatedRewardsOfAction(action));
			Double score = this.getScore(node, action);
			if (!score.isNaN()) {
				scores.put(action, score);
			}
		}

		/* finalize the choice */
		if (scores.isEmpty()) {
			this.logger.warn("All children have score NaN. Returning a random one.");
			return SetUtil.getRandomElement(possibleActions, 0);
		}
		A choice = this.getActionBasedOnScores(scores);
		Objects.requireNonNull(choice, "Would return null, but this must not be the case! Check the method that chooses an action given the scores.");
		this.logger.info("Recommending action {}.", choice);
		return choice;
	}

	public boolean isMaximize() {
		return this.maximize;
	}

	@Override
	public String getLoggerName() {
		return this.logger.getName();
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger = LoggerFactory.getLogger(name);
		this.logger.info("Set logger of {} to {}", this, name);
	}

	public double getGamma() {
		return this.gamma;
	}

	public EBehaviorForNotFullyExploredStates getBehaviorWhenActionForNotFullyExploredStateIsRequested() {
		return this.behaviorWhenActionForNotFullyExploredStateIsRequested;
	}

	public void setBehaviorWhenActionForNotFullyExploredStateIsRequested(final EBehaviorForNotFullyExploredStates behaviorWhenActionForNotFullyExploredStateIsRequested) {
		this.behaviorWhenActionForNotFullyExploredStateIsRequested = behaviorWhenActionForNotFullyExploredStateIsRequested;
	}
}
