package ai.libs.jaicore.search.algorithms.standard.mcts;

import java.util.Arrays;

import org.api4.java.common.control.ILoggingCustomizable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SPUCBPolicy<N, A> extends UCBPolicy<N, A> implements ILoggingCustomizable {
	private String loggerName;
	private Logger logger = LoggerFactory.getLogger(SPUCBPolicy.class);
	private final double bigD;

	public SPUCBPolicy(final double bigD) {
		super();
		this.bigD = bigD;
	}

	public SPUCBPolicy(final boolean maximize, final double bigD) {
		super(maximize);
		this.bigD = bigD;
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

		/* get ucb term */
		double ucb = super.getScore(labelOfNode, labelOfChild);

		/* get single player term added */
		double squaredResults = Arrays.stream(labelOfNode.scores.getValues()).reduce(0, (a, b) -> a + Math.pow(b, 2));
		double expectedResults = labelOfChild.visits * Math.pow(labelOfNode.scores.getMean(), 2);
		return ucb + (this.isMaximize() ? 1 : -1) * Math.sqrt((squaredResults - expectedResults + this.bigD) / labelOfChild.visits);
	}
}
