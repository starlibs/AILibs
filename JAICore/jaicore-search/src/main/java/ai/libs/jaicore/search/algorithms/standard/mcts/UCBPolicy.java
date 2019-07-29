package ai.libs.jaicore.search.algorithms.standard.mcts;

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
		this.logger = LoggerFactory.getLogger(name);
	}

	@Override
	public double getScore(final NodeLabel labelOfNode, final NodeLabel labelOfChild) {
		return labelOfChild.scores.getMean() + (this.isMaximize() ? 1 : -1) * this.explorationConstant * Math.sqrt(Math.log(labelOfNode.visits) / labelOfChild.visits);
	}

	public double getExplorationConstant() {
		return this.explorationConstant;
	}

	public void setExplorationConstant(final double explorationConstant) {
		this.explorationConstant = explorationConstant;
	}
}
