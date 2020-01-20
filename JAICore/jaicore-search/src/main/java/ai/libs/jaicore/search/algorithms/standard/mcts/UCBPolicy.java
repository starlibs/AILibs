package ai.libs.jaicore.search.algorithms.standard.mcts;

import java.util.Map;
import java.util.Map.Entry;

import org.api4.java.common.control.ILoggingCustomizable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UCBPolicy<T, A> extends AUpdatingPolicy<T, A> implements ILoggingCustomizable {

	private String loggerName;
	private Logger logger = LoggerFactory.getLogger(UCBPolicy.class);
	private double explorationConstant = Math.sqrt(2);

	public UCBPolicy() {
		super();
	}

	public UCBPolicy(final double explorationConstant) {
		this.explorationConstant = explorationConstant;
	}

	public UCBPolicy(final boolean maximize) {
		super(maximize);
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

	@Override
	public double getScore(final T node, final T child) {
		NodeLabel labelOfNode = this.getLabelOfNode(node);
		NodeLabel labelOfChild = this.getLabelOfNode(child);
		double explorationTerm = (this.isMaximize() ? 1 : -1) * this.explorationConstant * Math.sqrt(Math.log(labelOfNode.visits) / labelOfChild.visits);
		double score = labelOfChild.mean + explorationTerm;
		this.logger.trace("Computed UCB score {} = {} + {} * {} * sqrt(log({})/{}). That is, exploration term is {}", score, labelOfChild.mean, this.isMaximize() ? 1 : -1, this.explorationConstant, labelOfNode.visits, labelOfChild.visits, explorationTerm);
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
		this.logger.debug("Getting action for scores {}", scores);
		double best = (this.isMaximize() ? (-1) : 1) * Double.MAX_VALUE;
		for (Entry<A, Double> entry : scores.entrySet()) {
			A action = entry.getKey();
			double score = entry.getValue();
			if (this.isMaximize() && (score > best) || !this.isMaximize() && (score < best)) {
				this.logger.trace("Updating best choice {} with {} since it is better than the current solution with performance {}", choice, action, best);
				best = score;
				choice = action;
			} else {
				this.logger.trace("Skipping current solution {} since its score {} is not better than the currently best {}.", action, score, best);
			}
		}
		return choice;
	}
}
