package ai.libs.jaicore.search.algorithms.mdp.mcts.tag;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Queue;

import org.api4.java.common.control.ILoggingCustomizable;
import org.api4.java.datastructure.graph.ILabeledPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.search.algorithms.mdp.mcts.ActionPredictionFailedException;
import ai.libs.jaicore.search.algorithms.mdp.mcts.IPathUpdatablePolicy;

public class TAGPolicy<T, A> implements IPathUpdatablePolicy<T, A, Double>, ILoggingCustomizable {

	private String loggerName;
	private Logger logger = LoggerFactory.getLogger(TAGPolicy.class);
	private double explorationConstant = Math.sqrt(2);
	private final int s;
	private final Map<T, Double> thresholdPerNode = new HashMap<>();
	private final double delta;
	private final double thresholdIncrement; // the constant by which the threshold is incremented
	private final boolean isMaximize;
	private final Map<T, Map<A, PriorityQueue<Double>>> statsPerNode = new HashMap<>();
	private final Map<T, Map<A, Integer>> pullsPerNodeAction = new HashMap<>();
	private final Map<T, Integer> visitsPerNode = new HashMap<>();

	public TAGPolicy() {
		this(false);
	}

	public TAGPolicy(final double explorationConstant, final int s, final double delta, final double thresholdIncrement, final boolean isMaximize) {
		super();
		this.explorationConstant = explorationConstant;
		this.s = s;
		this.delta = delta;
		this.thresholdIncrement = thresholdIncrement;
		this.isMaximize = isMaximize;
	}

	public TAGPolicy(final boolean maximize) {
		this(Math.sqrt(2), 10, 1.0, 0.01, maximize);
	}

	@Override
	public A getAction(final T node, final Collection<A> actions) throws ActionPredictionFailedException {

		/* make sure that each successor has been visited at least once in this stats (by the default policy) */
		Map<A, Integer> pullMap = this.pullsPerNodeAction.computeIfAbsent(node, n -> new HashMap<>());
		actions.forEach(a -> pullMap.computeIfAbsent(a, action -> 1));
		this.visitsPerNode.put(node, this.visitsPerNode.computeIfAbsent(node, n -> 0) + 1);

		this.adjustThreshold(node); // first adjust threshold of node

		A choice = null;
		double best = (this.isMaximize ? -1 : 1) * Double.MAX_VALUE;
		int k = actions.size();
		for (A action : actions) {
			double score = this.getUtilityOfAction(node, action, k);
			if (this.isMaximize && score > best || !this.isMaximize && score < best) {
				this.logger.trace("Updating best choice {} with {} since it is better than the current solution with performance {}", choice, action, best);
				best = score;
				choice = action;
			} else {
				this.logger.trace("Skipping current solution {} since its score {} is not better than the currently best {}.", action, score, best);
			}
		}

		/* augment pulls for this action by one */
		pullMap.put(choice, pullMap.get(choice) + 1);
		return choice;
	}

	public void adjustThreshold(final T node) {
		Map<A, PriorityQueue<Double>> observations = this.statsPerNode.get(node);
		double t = this.thresholdPerNode.computeIfAbsent(node, n -> this.isMaximize ? 0.0 : 100.0);
		int sum;
		boolean first = true;
		do {
			if (!first) {
				t += this.thresholdIncrement * (this.isMaximize ? 1 : -1);
			}
			sum = 0;
			for (Entry<A, PriorityQueue<Double>> entry : observations.entrySet()) {
				final double localT = t;
				entry.getValue().removeIf(d -> this.isMaximize && d < localT || !this.isMaximize && d > localT);
				sum += entry.getValue().size();
			}
			first = false;
		} while (sum > this.s);
		this.thresholdPerNode.put(node, t);
	}

	/**
	 * This method computes the part (b) in the Streeter paper
	 *
	 * @param node
	 * @param action
	 * @return
	 */
	public double getUtilityOfAction(final T node, final A action, final int k) {

		/* compute nominator */
		double alpha = Math.log(2 * this.visitsPerNode.get(node) * k / this.delta);
		Queue<Double> memorizedScoresForArm = this.statsPerNode.get(node).get(action);
		int sChild = memorizedScoresForArm.size();
		if (alpha < 0) {
			throw new IllegalStateException("Alpha must not be negative. Check delta value (must be smaller than 1)");
		}
		double nominator = sChild + alpha + Math.sqrt(2 * sChild * alpha + Math.pow(alpha, 2));

		/* compute denominator (only child visits) */
		int armPulls = this.pullsPerNodeAction.get(node).get(action);
		if (armPulls == 0) {
			throw new IllegalArgumentException("Cannot compute score for child with no visits!");
		}

		double h = nominator / armPulls;
		this.logger.trace("Compute TAG score of {}", h);
		return h;
	}

	public double getExplorationConstant() {
		return this.explorationConstant;
	}

	public void setExplorationConstant(final double explorationConstant) {
		this.explorationConstant = explorationConstant;
	}

	@Override
	public void updatePath(final ILabeledPath<T, A> path, final List<Double> scores) {
		int l = path.getNumberOfNodes() - 1;
		List<T> nodes = path.getNodes();
		List<A> arcs = path.getArcs();
		double accumulatedScores = 0;
		for (int i = l - 1; i >= 0; i--) {
			T node = nodes.get(i);
			A action = arcs.get(i);

			/* update list of best observed scores */
			Map<A, PriorityQueue<Double>> actionMap = this.statsPerNode.computeIfAbsent(node, n -> new HashMap<>());
			PriorityQueue<Double> bestScores = actionMap.computeIfAbsent(action, a -> this.isMaximize ? new PriorityQueue<>((c1, c2) -> Double.compare(c2, c1)) : new PriorityQueue<>());
			accumulatedScores += scores.get(i); // no discounting used here
			if (bestScores.size() < this.s) {
				bestScores.add(accumulatedScores);
			} else if (bestScores.peek() < accumulatedScores) {
				bestScores.poll();
				bestScores.add(accumulatedScores);
			}
		}
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
}
