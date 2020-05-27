package ai.libs.jaicore.search.algorithms.mdp.mcts.old;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.api4.java.common.control.ILoggingCustomizable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.IRandomizable;
import ai.libs.jaicore.search.algorithms.mdp.mcts.IPolicy;

public class UniformRandomPolicy<T, A, V extends Comparable<V>> implements IPolicy<T, A>, IRandomizable, ILoggingCustomizable {

	private Logger logger = LoggerFactory.getLogger(UniformRandomPolicy.class);
	private final Random r;

	public UniformRandomPolicy() {
		this(new Random());
	}

	public UniformRandomPolicy(final Random r) {
		super();
		this.r = r;
	}

	@Override
	public A getAction(final T node, final Collection<A> actions) {
		this.logger.debug("Deriving action for node {}. Options are: {}", node, actions);

		if (actions.isEmpty()) {
			throw new IllegalArgumentException("Cannot determine action if no actions are given!");
		}
		if (actions.size() == 1) {
			return actions.iterator().next();
		}
		A choice;
		int chosenIndex = this.r.nextInt(actions.size());
		if (actions instanceof List) {
			choice = ((List<A>)actions).get(chosenIndex);
		}
		else {
			Iterator<A> it = actions.iterator();
			for (int i = 0; i < chosenIndex; i++) {
				it.next();
			}
			choice = it.next();
		}
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
