package jaicore.search.algorithms.standard.mcts;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UCBPolicy<T,A> implements IPathUpdatablePolicy<T,A,Double> {
	
	private static final Logger logger = LoggerFactory.getLogger(UCBPolicy.class);
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
	}
	
	private final Map<T, NodeLabel> labels = new HashMap<>();

	public void updatePath(List<T> path, Double score) {
		logger.info("Updating path {} with score {}", path, score);
		for (T node : path) {
			if (!labels.containsKey(node)) {
				labels.put(node, new NodeLabel());
			}
			NodeLabel label = labels.get(node);
			label.visits++;
			label.scores.addValue(score);
		}
	}
	
	@Override
	public A getAction(T node, Map<A,T> actionsWithTheirSuccessors) {
		logger.info("Deriving action for node {}. Options are: {}", node, actionsWithTheirSuccessors);
		Collection<A> possibleActions = actionsWithTheirSuccessors.keySet();
		
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
		double best = maximize ? 0 : Double.MAX_VALUE;
		logger.debug("All actions have been tried. Label is: {}", labels.get(node));
		int n = labels.get(node).visits;
		A choice = null;
		for (A action : possibleActions) {
			T child = actionsWithTheirSuccessors.get(action);
			NodeLabel label = labels.get(child);
			logger.info("Considering action {} whose successor state has stats {} and {} visits", action, label.scores.getMean(), label.visits);
			double ucb = label.scores.getMean() + (maximize ? 1 : -1) * Math.sqrt(2 * Math.log(n) / label.visits);
			if (maximize && (ucb > best) || !maximize && (ucb < best)) {
				best = ucb;
				choice = action;
			}
		}
		
		/* quick sanity check */
		if (choice == null)
			throw new IllegalStateException("Would return null, but this must not be the case!");
		return choice;
	}
}
