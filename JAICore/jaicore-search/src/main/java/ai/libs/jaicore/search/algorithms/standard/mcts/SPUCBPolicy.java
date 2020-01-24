package ai.libs.jaicore.search.algorithms.standard.mcts;

import java.util.HashMap;
import java.util.Map;

import org.api4.java.common.control.ILoggingCustomizable;
import org.api4.java.datastructure.graph.ILabeledPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SPUCBPolicy<N, A> extends UCBPolicy<N, A> implements ILoggingCustomizable {
	private String loggerName;
	private Logger logger = LoggerFactory.getLogger(SPUCBPolicy.class);
	private final double bigD;
	private Map<NodeLabel, Double> squaredObservations = new HashMap<>();

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
		super.setLoggerName(name + "._updating");
		this.logger = LoggerFactory.getLogger(name);
	}

	@Override
	public void updatePath(final ILabeledPath<N, A> path, final Double score, final int pathLength) {
		super.updatePath(path, score, pathLength); // careful! the visits stats has already been updated here!
		for (N node : path.getNodes()) {
			NodeLabel nl = this.getLabelOfNode(node);
			this.squaredObservations.put(nl, this.squaredObservations.computeIfAbsent(nl, l -> 0.0) + Math.pow(score, 2));
		}
	}

	@Override
	public double getScore(final N node, final N child) {

		/* get ucb term */
		double ucb = super.getScore(node, child);

		/* get single player term added */
		int visitsOfChild = this.getLabelOfNode(child).visits; // the t-parameter in the paper
		NodeLabel labelOfNode = this.getLabelOfNode(node);
		double squaredResults = this.squaredObservations.containsKey(labelOfNode) ? this.squaredObservations.get(labelOfNode) : 0.0;
		double expectedResults = visitsOfChild * Math.pow(labelOfNode.mean, 2);
		double spTerm = (this.isMaximize() ? 1 : -1) * Math.sqrt((squaredResults - expectedResults + this.bigD) / visitsOfChild);
		double score = ucb + spTerm;
		this.logger.debug("Computed score for child {}: {} = {} + {}", child, score, ucb, spTerm);
		return score;
	}
}
