package ai.libs.jaicore.search.algorithms.standard.mcts;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.api4.java.common.control.ILoggingCustomizable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.IRandomizable;


public class UniformRandomPolicy<T, A, V extends Comparable<V>> implements IPolicy<T, A, V>, IRandomizable, ILoggingCustomizable {

	private Logger logger = LoggerFactory.getLogger(UniformRandomPolicy.class);
	private final Random r;

	public UniformRandomPolicy(final Random r) {
		super();
		this.r = r;
	}

	@Override
	public A getAction(final T node, final Map<A,T> actionsWithTheirSuccessors) {
		this.logger.debug("Deriving action for node {}. Options are: {}", node, actionsWithTheirSuccessors);

		if (actionsWithTheirSuccessors.isEmpty()) {
			throw new IllegalArgumentException("Cannot determine action if no actions are given!");
		}
		if (actionsWithTheirSuccessors.size() == 1) {
			return actionsWithTheirSuccessors.keySet().iterator().next();
		}
		List<A> keys = new ArrayList<>(actionsWithTheirSuccessors.keySet());
		A choice = keys.get(this.r.nextInt(keys.size()));
		this.logger.info("Recommending action {}", choice);
		return choice;
	}

	public void updatePath(final List<T> path, final V score) {
		this.logger.debug("Updating path {} with score {}", path, score);
	}

	@Override
	public String getLoggerName() {
		return this.logger.getName();
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger = LoggerFactory.getLogger(name);
	}

	@Override
	public Random getRandom() {
		return this.r;
	}

	@Override
	public void setRandom(final Random random) {
		throw new UnsupportedOperationException("Random source cannot be overwritten.");
	}
}
