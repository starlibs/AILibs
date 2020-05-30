package ai.libs.jaicore.search.algorithms.mdp.mcts.uct;

import java.util.Map;
import java.util.Map.Entry;

import org.api4.java.common.control.ILoggingCustomizable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.search.algorithms.mdp.mcts.NodeLabel;

public class UCBPolicy<T, A> extends AUpdatingPolicy<T, A> implements ILoggingCustomizable {

	private String loggerName;
	private Logger logger = LoggerFactory.getLogger(UCBPolicy.class);
	private double explorationConstant;

	public UCBPolicy(final double gamma, final double explorationConstant, final boolean maximize) {
		super(gamma, maximize);
		this.explorationConstant = explorationConstant;
	}

	public UCBPolicy(final double gamma, final boolean maximize) {
		this(gamma, Math.sqrt(2), maximize);
	}

	@Override
	public String getLoggerName() {
		return this.loggerName;
	}

	@Override
	public void setLoggerName(final String name) {
		this.loggerName = name;
		super.setLoggerName(name + "._updating");
		this.logger = LoggerFactory.getLogger(name);
	}

	public double getEmpiricalMean(final T node, final A action) {
		NodeLabel<A> nodeLabel = this.getLabelOfNode(node);
		if (nodeLabel == null || nodeLabel.getNumPulls(action) == 0) {
			return (this.isMaximize() ? -1 : 1) * Double.MAX_VALUE;
		}
		int timesThisActionHasBeenChosen = nodeLabel.getNumPulls(action);
		return nodeLabel.getAccumulatedRewardsOfAction(action) / timesThisActionHasBeenChosen;
	}

	public double getExplorationTerm(final T node, final A action) {
		NodeLabel<A> nodeLabel = this.getLabelOfNode(node);
		if (nodeLabel == null || nodeLabel.getNumPulls(action) == 0) {
			return (this.isMaximize() ? -1 : 1) * Double.MAX_VALUE;
		}
		int timesThisActionHasBeenChosen = nodeLabel.getNumPulls(action);
		return (this.isMaximize() ? 1 : -1) * this.explorationConstant * Math.sqrt(Math.log(nodeLabel.getVisits()) / timesThisActionHasBeenChosen);
	}

	@Override
	public double getScore(final T node, final A action) {
		NodeLabel<A> nodeLabel = this.getLabelOfNode(node);
		if (nodeLabel == null || nodeLabel.isVirgin(action)) {
			return (this.isMaximize() ? -1 : 1) * Double.MAX_VALUE;
		}
		int timesThisActionHasBeenChosen = nodeLabel.getNumPulls(action);
		double averageScoreForThisAction = nodeLabel.getAccumulatedRewardsOfAction(action) / timesThisActionHasBeenChosen;
		double explorationTerm = (this.isMaximize() ? 1 : -1) * this.explorationConstant * Math.sqrt(Math.log(nodeLabel.getVisits()) / timesThisActionHasBeenChosen);
		double score = averageScoreForThisAction + explorationTerm;
		this.logger.trace("Computed UCB score {} = {} + {} * {} * sqrt(log({})/{}). That is, exploration term is {}", score, averageScoreForThisAction, this.isMaximize() ? 1 : -1, this.explorationConstant, nodeLabel.getVisits(),
				timesThisActionHasBeenChosen, explorationTerm);
		return score;
	}

	public double getExplorationConstant() {
		return this.explorationConstant;
	}

	public void setExplorationConstant(final double explorationConstant) {
		this.explorationConstant = explorationConstant;
	}

	@Override
	public A getActionBasedOnScores(final Map<A, Double> scores) {
		A choice = null;
		if (scores.isEmpty()) {
			throw new IllegalArgumentException("An empty set of scored actions has been given to UCB to decide!");
		}
		this.logger.debug("Getting action for scores {}", scores);
		double best = (this.isMaximize() ? (-1) : 1) * Double.MAX_VALUE;
		for (Entry<A, Double> entry : scores.entrySet()) {
			A action = entry.getKey();
			double score = entry.getValue();
			if (choice == null || (this.isMaximize() && (score > best) || !this.isMaximize() && (score < best))) {
				this.logger.trace("Updating best choice {} with {} since it is better than the current solution with performance {}", choice, action, best);
				best = score;
				choice = action;
			} else {
				this.logger.trace("Skipping current solution {} since its score {} is not better than the currently best {}.", action, score, best);
			}
		}
		if (choice == null) {
			throw new IllegalStateException("UCB would return NULL action, which must not be the case!");
		}
		return choice;
	}
}
