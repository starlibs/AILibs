package jaicore.search.algorithms.standard.mcts;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.ILoggingCustomizable;

public class UCBPolicy<T,A> implements IPathUpdatablePolicy<T,A,Double>, ILoggingCustomizable {
	
	private String loggerName;
	private Logger logger = LoggerFactory.getLogger(UCBPolicy.class);
	
	private final boolean maximize;

	public UCBPolicy() {
		this(true);
	}
	
	public UCBPolicy(boolean maximize) {
		this.maximize = maximize;
	}
	
	class NodeLabel {
		private final DescriptiveStatistics scores = new DescriptiveStatistics();
		private int visits;
		@Override
		public String toString() {
			return "NodeLabel [scores=" + scores + ", visits=" + visits + "]";
		}
	}
	
	private final Map<T, NodeLabel> labels = new HashMap<>();

	public void updatePath(List<T> path, Double score) {
		logger.debug("Updating path {} with score {}", path, score);
		for (T node : path) {
			if (!labels.containsKey(node)) {
				labels.put(node, new NodeLabel());
			}
			NodeLabel label = labels.get(node);
			label.visits++;
			label.scores.addValue(score);
			logger.trace("Updated label of node {}. Visits now {}, stats contains {} entries with mean {}", node, label.visits, label.scores.getN(), label.scores.getMean());
		}
	}
	
	@Override
	public A getAction(T node, Map<A,T> actionsWithTheirSuccessors) {
		Collection<A> possibleActions = actionsWithTheirSuccessors.keySet();
		logger.debug("Deriving action for node {}. The {} options are: {}", node, possibleActions.size(), actionsWithTheirSuccessors);
		
		/* if an applicable action has not been tried, play it to get some initial idea */
		List<A> actionsThatHaveNotBeenTriedYet = possibleActions.stream().filter(a -> !labels.containsKey(actionsWithTheirSuccessors.get(a))).collect(Collectors.toList());
		if (!actionsThatHaveNotBeenTriedYet.isEmpty()) {
			A action = actionsThatHaveNotBeenTriedYet.get(0);
			T child = actionsWithTheirSuccessors.get(action);
			labels.put(child, new NodeLabel());
			logger.info("Dictating action {}, because this was never played before.", action);
			return action;
		}
		
		/* otherwise, play best action */
		double best = maximize ? Double.MIN_VALUE : Double.MAX_VALUE;
		logger.debug("All actions have been tried. Label is: {}", labels.get(node));
		int n = labels.get(node).visits;
		A choice = null;
		for (A action : possibleActions) {
			T child = actionsWithTheirSuccessors.get(action);
			NodeLabel label = labels.get(child);
			assert label.visits != 0 : "Visits of node " + child + " cannot be 0 if we already used this action before!";
			assert label.scores.getN() != 0 : "Number of observations cannot be 0 if we already visited this node before";
			logger.trace("Considering action {} whose successor state has stats {} and {} visits", action, label.scores.getMean(), label.visits);
			double ucb = label.scores.getMean() + (maximize ? 1 : -1) * Math.sqrt(2 * Math.log(n) / label.visits);
			assert !(new Double(ucb).equals(Double.NaN)) : "The UCB score is NaN, which cannot be the case. Score mean is " + label.scores.getMean() + ", number of visits is " + label.visits;
			if (maximize && (ucb > best) || !maximize && (ucb < best)) {
				logger.trace("Updating best choice {} with {} since it is better than the current solution with performance {}", choice, action, best);
				best = ucb;
				choice = action;
			}
			else
				logger.trace("Skipping current solution {} since its score {} is not better than the currently best {}.", action, ucb, best);	
		}
		
		/* quick sanity check */
		assert choice != null : "Would return null, but this must not be the case!";
		logger.info("Recommending action {}.", choice);
		return choice;
	}

	@Override
	public String getLoggerName() {
		return loggerName;
	}

	@Override
	public void setLoggerName(String name) {
		this.loggerName = name;
		this.logger = LoggerFactory.getLogger(name);
	}
}
