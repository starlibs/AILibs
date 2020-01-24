package ai.libs.jaicore.search.algorithms.standard.mcts.tag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.stream.Collectors;

import org.api4.java.common.control.ILoggingCustomizable;
import org.api4.java.datastructure.graph.ILabeledPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.graph.LabeledGraph;
import ai.libs.jaicore.search.algorithms.standard.mcts.ActionPredictionFailedException;
import ai.libs.jaicore.search.algorithms.standard.mcts.IGraphDependentPolicy;
import ai.libs.jaicore.search.algorithms.standard.mcts.IPathUpdatablePolicy;

public class TAGPolicy<T, A> implements IPathUpdatablePolicy<T, A, Double>, IGraphDependentPolicy<T, A>, ILoggingCustomizable {

	private String loggerName;
	private Logger logger = LoggerFactory.getLogger(TAGPolicy.class);
	private LabeledGraph<T, A> explorationGraph;
	private double explorationConstant = Math.sqrt(2);
	private final int s;
	private final double delta;
	private final boolean isMaximize;
	private final Map<T, PriorityQueue<Double>> statsPerNode = new HashMap<>();
	private final Map<T, Integer> visitsPerNode = new HashMap<>();

	public TAGPolicy() {
		this(false);
	}

	public TAGPolicy(final double explorationConstant, final int s, final double delta, final boolean isMaximize) {
		super();
		this.explorationConstant = explorationConstant;
		this.s = s;
		this.delta = delta;
		this.isMaximize = isMaximize;
	}

	public TAGPolicy(final boolean maximize) {
		this(Math.sqrt(2), 10, 0.01, maximize);
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

	public double getScoreOfChild(final T node, final T consideredChild) {

		Queue<Double> bestScoresObservedInparent = this.statsPerNode.get(node);
		double worstObservationAmongSBestInparent = bestScoresObservedInparent.peek();
		List<Double> relevantScoresOfChild = this.statsPerNode.get(consideredChild).stream().filter(v -> this.isMaximize ? v >= worstObservationAmongSBestInparent : v <= worstObservationAmongSBestInparent).collect(Collectors.toList());
		int sChild = relevantScoresOfChild.size();
		double k = this.explorationGraph.getSuccessors(node).size();
		double alpha = Math.log(2 * this.visitsPerNode.get(node) * k / this.delta);
		if (alpha < 0) {
			throw new IllegalStateException("Alpha must not be negative. Check delta value (must be smaller than 1)");
		}
		int childVisits = this.visitsPerNode.get(consideredChild);
		if (childVisits == 0) {
			throw new IllegalArgumentException("Cannot compute score for child with no visits!");
		}

		Double h = (sChild + alpha + Math.sqrt(2 * sChild * alpha + Math.pow(alpha, 2))) / childVisits;
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
	public A getAction(final T node, final Map<A, T> actionsWithSuccessors) throws ActionPredictionFailedException {
		A choice = null;
		double best = (this.isMaximize ? -1 : 1) * Double.MAX_VALUE;
		for (Entry<A, T> entry : actionsWithSuccessors.entrySet()) {
			A action = entry.getKey();
			T childNode = entry.getValue();
			Double score = this.getScoreOfChild(node, childNode);
			if (score.isNaN()) {
				throw new IllegalStateException("Score for option " + action + " is NaN");
			}
			if (this.isMaximize && score > best || !this.isMaximize && score < best) {
				this.logger.trace("Updating best choice {} with {} since it is better than the current solution with performance {}", choice, action, best);
				best = score;
				choice = action;
			} else {
				this.logger.trace("Skipping current solution {} since its score {} is not better than the currently best {}.", action, score, best);
			}
		}
		return choice;
	}

	@Override
	public void updatePath(final ILabeledPath<T, A> path, final Double playout, final int playoutLength) {
		for (T node : path.getNodes()) {
			this.visitsPerNode.put(node, this.visitsPerNode.computeIfAbsent(node, n -> 0) + 1);

			/* update list of best observed scores */
			PriorityQueue<Double> bestScores = this.statsPerNode.computeIfAbsent(node, n -> this.isMaximize ? new PriorityQueue<>((c1,c2) -> Double.compare(c2, c1)) : new PriorityQueue<>());
			if (bestScores.size() < this.s) {
				bestScores.add(playout);
			}
			else if (bestScores.peek() < playout) {
				bestScores.poll();
				bestScores.add(playout);
			}
		}
	}

	@Override
	public void setGraph(final LabeledGraph<T, A> graph) {
		this.explorationGraph = graph;
	}
}
