package ai.libs.jaicore.search.algorithms.standard.mcts;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.api4.java.common.control.ILoggingCustomizable;
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

	class NodeLabel {
		final DescriptiveStatistics scores = new DescriptiveStatistics();
		int visits;

		@Override
		public String toString() {
			return "NodeLabel [scores=" + this.scores + ", visits=" + this.visits + "]";
		}
	}

	private final Map<N, NodeLabel> labels = new HashMap<>();

	public abstract double getScore(NodeLabel labelOfNode, NodeLabel labelOfChild);

	@Override
	public void updatePath(final List<N> path, final Double score) {
		this.logger.debug("Updating path {} with score {}", path, score);
		for (N node : path) {
			if (!this.labels.containsKey(node)) {
				this.labels.put(node, new NodeLabel());
			}
			NodeLabel label = this.labels.get(node);
			label.visits++;
			label.scores.addValue(score);
			this.logger.trace("Updated label of node {}. Visits now {}, stats contains {} entries with mean {}", node, label.visits, label.scores.getN(), label.scores.getMean());
		}
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
		double best = this.maximize ? Double.MIN_VALUE : Double.MAX_VALUE;
		this.logger.debug("All actions have been tried. Label is: {}", this.labels.get(node));
		NodeLabel labelOfNode = this.labels.get(node);
		A choice = null;
		for (A action : possibleActions) {
			N child = actionsWithTheirSuccessors.get(action);
			NodeLabel labelOfChild = this.labels.get(child);
			assert labelOfChild.visits != 0 : "Visits of node " + child + " cannot be 0 if we already used this action before!";
			assert labelOfChild.scores.getN() != 0 : "Number of observations cannot be 0 if we already visited this node before";
			this.logger.trace("Considering action {} whose successor state has stats {} and {} visits", action, labelOfChild.scores.getMean(), labelOfChild.visits);
			double score = this.getScore(labelOfNode, labelOfChild);
			assert !(new Double(score).equals(Double.NaN)) : "The UCB score is NaN, which cannot be the case. Score mean is " + labelOfChild.scores.getMean() + ", number of visits is " + labelOfChild.visits;
			if (this.maximize && (score > best) || !this.maximize && (score < best)) {
				this.logger.trace("Updating best choice {} with {} since it is better than the current solution with performance {}", choice, action, best);
				best = score;
				choice = action;
			} else {
				this.logger.trace("Skipping current solution {} since its score {} is not better than the currently best {}.", action, score, best);
			}
		}

		/* quick sanity check */
		assert choice != null : "Would return null, but this must not be the case!";
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
	}

}
