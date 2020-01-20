package ai.libs.jaicore.search.algorithms.standard.mcts;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.api4.java.common.control.ILoggingCustomizable;
import org.api4.java.datastructure.graph.ILabeledPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
		protected double mean;
		protected int visits;

		@Override
		public String toString() {
			return "NodeLabel [mean=" + this.mean + ", visits=" + this.visits + "]";
		}
	}

	private final Map<N, NodeLabel> labels = new HashMap<>();

	public NodeLabel getLabelOfNode(final N node) {
		if (!this.labels.containsKey(node)) {
			throw new IllegalArgumentException("No label for node " + node);
		}
		return this.labels.get(node);
	}

	public abstract double getScore(N node, N child);

	public abstract A getActionBasedOnScores(Map<A, Double> scores);

	@Override
	public void updatePath(final ILabeledPath<N, A> path, final Double score, final int lengthOfActualPlayoutPath) {
		this.logger.debug("Updating path {} with score {}", path, score);
		int lastVisits = Integer.MAX_VALUE;
		for (N node : path.getNodes()) {
			NodeLabel label = this.labels.computeIfAbsent(node, n -> new NodeLabel());
			label.mean = (label.visits * label.mean + score) / (label.visits + 1);
			label.visits++;
			this.logger.trace("Updated label of node {}. Visits now {} with mean {}", node, label.visits, label.mean);
			if (label.visits > lastVisits) {
				throw new IllegalStateException("Illegal visits stats of child " + label.visits + " compared to parent " + lastVisits + "\nCheck whether the searched graph is really a tree!");
			}
			lastVisits = label.visits;
		}
		this.logger.debug("Path update completed.");
	}

	@Override
	public A getAction(final N node, final Map<A, N> actionsWithTheirSuccessors) {
		Collection<A> possibleActions = actionsWithTheirSuccessors.keySet();
		this.logger.debug("Deriving action for node {}. The {} options are: {}", node, possibleActions.size(), actionsWithTheirSuccessors);

		/* if an applicable action has not been tried, play it to get some initial idea */
		List<A> actionsThatHaveNotBeenTriedYet = possibleActions.stream().filter(a -> !this.labels.containsKey(actionsWithTheirSuccessors.get(a))).collect(Collectors.toList());
		if (!actionsThatHaveNotBeenTriedYet.isEmpty()) {
			A action = actionsThatHaveNotBeenTriedYet.get(0);
			N child = actionsWithTheirSuccessors.get(action);
			this.labels.put(child, new NodeLabel());
			this.logger.info("Dictating action {}, because this was never played before.", action);
			return action;
		}

		/* otherwise, play best action */
		this.logger.debug("All actions have been tried. Label is: {}", this.labels.get(node));
		Map<A, Double> scores = new HashMap<>();
		for (A action : possibleActions) {
			N child = actionsWithTheirSuccessors.get(action);
			NodeLabel labelOfChild = this.labels.get(child);
			assert labelOfChild.visits != 0 : "Visits of node " + child + " cannot be 0 if we already used this action before!";
			this.logger.trace("Considering action {} whose successor state has stats {} and {} visits", action, labelOfChild.mean, labelOfChild.visits);
			Double score = this.getScore(node, child);
			if (score.isNaN()) {
				throw new IllegalStateException("Score of action " + action + " is NaN, which it must not be!");
			}
			scores.put(action, score);
			assert !score.isNaN() : "The score of action " + action + " is NaN, which cannot be the case. Score mean is " + labelOfChild.mean + ", number of visits is " + labelOfChild.visits;
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
