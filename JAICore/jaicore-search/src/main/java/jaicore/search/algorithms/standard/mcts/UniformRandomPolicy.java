package jaicore.search.algorithms.standard.mcts;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class UniformRandomPolicy<T, A, V extends Comparable<V>> implements IPolicy<T, A, V> {

	private static final Logger logger = LoggerFactory.getLogger(UniformRandomPolicy.class);
	private final Random r;

	public UniformRandomPolicy(Random r) {
		super();
		this.r = r;
	}

	@Override
	public A getAction(T node, Map<A,T> actionsWithTheirSuccessors) {
		logger.debug("Deriving action for node {}. Options are: {}", node, actionsWithTheirSuccessors);
		
		if (actionsWithTheirSuccessors.isEmpty())
			throw new IllegalArgumentException("Cannot determine action if no actions are given!");
		if (actionsWithTheirSuccessors.size() == 1)
			return actionsWithTheirSuccessors.keySet().iterator().next();
		List<A> keys = new ArrayList<>(actionsWithTheirSuccessors.keySet());
		A choice = keys.get(r.nextInt(keys.size() - 1));
		logger.info("Recommending action {}", choice);
		return choice;
	}
	
	public void updatePath(List<T> path, V score) {
		logger.debug("Updating path {} with score {}", path, score);
	}
}
