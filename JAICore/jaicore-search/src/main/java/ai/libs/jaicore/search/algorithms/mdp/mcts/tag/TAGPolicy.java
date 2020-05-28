package ai.libs.jaicore.search.algorithms.mdp.mcts.tag;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.stream.Collectors;

import org.api4.java.common.control.ILoggingCustomizable;
import org.api4.java.datastructure.graph.ILabeledPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.graph.LabeledGraph;
import ai.libs.jaicore.search.algorithms.mdp.mcts.ActionPredictionFailedException;
import ai.libs.jaicore.search.algorithms.mdp.mcts.IGraphDependentPolicy;
import ai.libs.jaicore.search.algorithms.mdp.mcts.IPathUpdatablePolicy;

public class TAGPolicy<T, A> implements IPathUpdatablePolicy<T, A, Double>, IGraphDependentPolicy<T, A>, ILoggingCustomizable {

	private String loggerName;
	private Logger logger = LoggerFactory.getLogger(TAGPolicy.class);
	private LabeledGraph<T, A> explorationGraph = new LabeledGraph<>();
	private double explorationConstant = Math.sqrt(2);
	private final int s;
	private final double delta;
	private final boolean isMaximize;
	private final Map<T, Map<A, PriorityQueue<Double>>> statsPerNode = new HashMap<>();
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

	public double getScoreOfAction(final T node, final A action) {

		Queue<Double> bestScoresObservedInparent = this.statsPerNode.get(node).get(action);
		double worstObservationAmongSBestInparent = bestScoresObservedInparent.peek();
		List<Double> relevantScoresOfChild = bestScoresObservedInparent.stream().filter(v -> this.isMaximize ? v >= worstObservationAmongSBestInparent : v <= worstObservationAmongSBestInparent).collect(Collectors.toList());
		int sChild = relevantScoresOfChild.size();
		double k = this.explorationGraph.getSuccessors(node).size();
		double alpha = Math.log(2 * this.visitsPerNode.get(node) * k / this.delta);
		if (alpha < 0) {
			throw new IllegalStateException("Alpha must not be negative. Check delta value (must be smaller than 1)");
		}
		int childVisits = this.visitsPerNode.get(node);
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
	public A getAction(final T node, final Collection<A> actions) throws ActionPredictionFailedException {
		A choice = null;
		double best = (this.isMaximize ? -1 : 1) * Double.MAX_VALUE;
		for (A action : actions) {
			Double score = this.getScoreOfAction(node, action);
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
	public void updatePath(final ILabeledPath<T, A> path, final List<Double> scores) {
		int l = path.getNumberOfNodes() - 1;
		List<T> nodes = path.getNodes();
		List<A> arcs = path.getArcs();
		double accumulatedScores = 0;
		for (int i = l - 2; i >= 0; i--) {
			T node = nodes.get(i);
			A action = arcs.get(i);
			this.visitsPerNode.put(node, this.visitsPerNode.computeIfAbsent(node, n -> 0) + 1);
			this.explorationGraph.addEdge(node, nodes.get(i + 1));

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
	public void setGraph(final LabeledGraph<T, A> graph) {
		this.explorationGraph = graph;
	}
}
