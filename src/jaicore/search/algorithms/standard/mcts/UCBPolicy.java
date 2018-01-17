package jaicore.search.algorithms.standard.mcts;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UCBPolicy<T,A> implements IPolicy<T,A,Double> {
	
	private static final Logger logger = LoggerFactory.getLogger(UCBPolicy.class);

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
		int n = labels.get(node).visits;
		double best = 0;
		A choice = null;
		for (A action : actionsWithTheirSuccessors.keySet()) {
			T child = actionsWithTheirSuccessors.get(action);
			
			/* if this node is considered the first time, play it to get some initial idea */
			if (!labels.containsKey(child)) {
				labels.put(child, new NodeLabel());
				logger.info("Initializing node {}", child);
			}
			NodeLabel label = labels.get(child);
			if (label.visits == 0) {
				logger.info("Dictating action {}, because this was never played before.", action);
				return action;
			}
			logger.info("Considering action {} whose successor state has stats {} and {} visits", action, label.scores.getMean(), label.visits);
			double ucb = label.scores.getMean() + Math.sqrt(2 * Math.log(n) / label.visits);
			if (ucb > best) {
				best = ucb;
				choice = action;
			}
		}
		
		if (choice == null)
			throw new IllegalStateException("Would return null, but this must not be the case!");
		return choice;
	}
}
