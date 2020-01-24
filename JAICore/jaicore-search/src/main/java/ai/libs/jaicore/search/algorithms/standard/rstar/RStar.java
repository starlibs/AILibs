package ai.libs.jaicore.search.algorithms.standard.rstar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.api4.java.ai.graphsearch.problem.IPathSearchInput;
import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.INodeGoalTester;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.PathEvaluationException;
import org.api4.java.algorithm.IAlgorithm;
import org.api4.java.algorithm.Timeout;
import org.api4.java.algorithm.events.IAlgorithmEvent;
import org.api4.java.algorithm.events.result.ISolutionCandidateFoundEvent;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.api4.java.common.control.ILoggingCustomizable;
import org.api4.java.common.math.IMetric;
import org.api4.java.datastructure.graph.implicit.IRootGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.algorithm.AlgorithmInitializedEvent;
import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.basic.sets.SetUtil;
import ai.libs.jaicore.search.algorithms.standard.astar.AStar;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.events.EvaluatedSearchSolutionCandidateFoundEvent;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.events.NodeExpansionCompletedEvent;
import ai.libs.jaicore.search.core.interfaces.AOptimalPathInORGraphSearch;
import ai.libs.jaicore.search.model.other.EvaluatedSearchGraphPath;
import ai.libs.jaicore.search.model.other.SearchGraphPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchInput;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithNumberBasedAdditivePathEvaluation;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithNumberBasedAdditivePathEvaluationAndSubPathHeuristic;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithNumberBasedAdditivePathEvaluationAndSubPathHeuristic.DistantSuccessorGenerator;

/**
 * Implementation of the R* algorithm.
 *
 * @author fischor, fmohr, mwever
 *
 * @param <T> a nodes external label i.e. a state of a problem
 * @param <A> action (action space of problem)
 */
public class RStar<I extends GraphSearchWithNumberBasedAdditivePathEvaluationAndSubPathHeuristic<T, A>, T, A> extends AOptimalPathInORGraphSearch<I, T, A, Double> {

	/* Open list. */
	protected PriorityQueue<GammaNode<T, A>> open = new PriorityQueue<>((n1, n2) -> (n1.getScore().compareTo(n2.getScore())));

	/* Closed list of already expanded states. */
	protected ArrayList<GammaNode<T, A>> closed = new ArrayList<>();

	private final IPathEvaluator<T, A, Double> h;
	private final GraphSearchWithNumberBasedAdditivePathEvaluationAndSubPathHeuristic.PathCostEstimator<T, A> hPath;

	/* For actual search problem */
	private final int k;
	protected final double w;
	private final double delta;
	private final IMetric<T> metricOverStates;

	private GammaNode<T, A> bestSeenGoalNode;
	private final Map<Pair<GammaNode<T, A>, GammaNode<T, A>>, SearchGraphPath<T, A>> externalPathsBetweenGammaNodes = new HashMap<>(); // the pairs should always be in a parent-child relation

	private List<ISolutionCandidateFoundEvent<EvaluatedSearchGraphPath<T, A, Double>>> unreturnedSolutionEvents = new LinkedList<>();

	private Collection<AStar<T, A>> activeAStarSubroutines = new ArrayList<>();

	private Logger logger = LoggerFactory.getLogger(RStar.class);

	/**
	 *
	 * @param gammaGraphGenerator
	 * @param w
	 * @param k
	 * @param delta
	 */
	public RStar(final I problem, final double w, final int k, final double delta) {
		super(problem);
		this.h = ((GraphSearchWithNumberBasedAdditivePathEvaluation.FComputer<T, A>) this.getInput().getPathEvaluator()).getH();
		this.hPath = ((GraphSearchWithNumberBasedAdditivePathEvaluationAndSubPathHeuristic.SubPathEvaluationBasedFComputer<T, A>) this.getInput().getPathEvaluator()).gethPath();
		this.w = w;
		this.k = k;
		this.metricOverStates = this.getInput().getMetricOverStates();
		this.delta = delta;
	}

	/**
	 * Updates a state i.e. node n in the open list. Lines 1 - 5 in the paper.
	 *
	 * @param n
	 * @throws InterruptedException
	 * @throws PathEvaluationException
	 */
	private void updateState(final GammaNode<T, A> n) throws PathEvaluationException, InterruptedException {
		if ((n.getG() > this.w * this.h.evaluate(n)) || ((n.getParent() == null || !this.isPathRealizationKnownForAbstractEdgeToNode(n)) && n.getAvoid())) {
			n.setScore(new RStarK(true, n.getG() + this.w * this.h.evaluate(n)));
		} else {
			n.setScore(new RStarK(false, n.getG() + this.w * this.h.evaluate(n)));
		}
	}

	/**
	 * Tries to compute the local path
	 * Lines 6 - 12 in the paper.
	 *
	 * @param n
	 * @throws InterruptedException
	 * @throws AlgorithmException
	 * @throws TimeoutException
	 * @throws AlgorithmExecutionCanceledException
	 */
	private void reevaluateState(final GammaNode<T, A> n) throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException, AlgorithmException {

		/* Line 7: Try to compute the local path from bp(n) to n. (we use AStar for this) */
		this.logger.debug("Reevaluating node {}", n);
		if (n.getParent() == null) {
			throw new IllegalArgumentException("Can only re-evaluate nodes that have a parent!");
		}
		IPathSearchInput<T, A> subProblem = new GraphSearchInput<>(new SubPathGraphGenerator<>(this.getInput().getGraphGenerator(), n.getParent().getHead()), c -> c.equals(n.getHead()));
		AStar<T, A> astar = new AStar<>(new GraphSearchWithNumberBasedAdditivePathEvaluation<>(subProblem, (GraphSearchWithNumberBasedAdditivePathEvaluation.FComputer<T, A>) this.getInput().getPathEvaluator()));
		astar.setLoggerName(this.getLoggerName() + ".astar");
		astar.setTimeout(new Timeout(this.getRemainingTimeToDeadline().milliseconds(), TimeUnit.MILLISECONDS));
		this.logger.trace("Invoking AStar with root {} and only goal node {}", n.getParent().getHead(), n.getHead());
		this.activeAStarSubroutines.add(astar);
		EvaluatedSearchGraphPath<T, A, Double> optimalPath = astar.call();
		this.checkAndConductTermination();
		this.activeAStarSubroutines.remove(astar);
		this.externalPathsBetweenGammaNodes.put(new Pair<>(n.getParent(), n), optimalPath);
		double bestKnownValueFromParentToNode = optimalPath != null ? optimalPath.getScore() : Double.MAX_VALUE;
		n.getParent().cLow.put(n, bestKnownValueFromParentToNode);

		/**
		 * If no path bp(n)->n could be computed or
		 * the g = "cost from n_start to bp(n)" + the cost of the found path is greater than w*h(n_start, n)
		 * the state n should be avoided.
		 */
		// Line 8
		if (!n.isGoal() && (optimalPath == null || (n.getParent().getG() + bestKnownValueFromParentToNode > this.w * this.hPath.h(n.getParent(), n)))) {
			n.setParent(this.argminCostToStateOverPredecessors(n));
			n.setAvoid(true);
		}
		n.setG(n.getParent().getG() + n.getParent().cLow.get(n));
		if (!n.isGoal()) {
			try {
				this.updateState(n);
			} catch (PathEvaluationException e) {
				throw new AlgorithmException("Failed due to path evaluation failure.", e);
			}
		}
	}

	@Override
	public IAlgorithmEvent nextWithException() throws InterruptedException, AlgorithmException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException {
		try {

			this.registerActiveThread();
			this.logger.debug("Performing next step. Current state is {}", this.getState());
			this.checkAndConductTermination();
			switch (this.getState()) {
			case CREATED:
				AlgorithmInitializedEvent initializationEvent = this.activate();

				/* Lines 14 to 17 */
				IRootGenerator<T> rootGenerator = this.getInput().getGraphGenerator().getRootGenerator();
				for (T root : rootGenerator.getRoots()) {
					GammaNode<T, A> internalRoot = new GammaNode<>(root);
					internalRoot.setScore(new RStarK(false, this.w * this.h.evaluate(internalRoot)));
					internalRoot.setG(0);
					this.open.add(internalRoot);
				}

				assert !this.open.isEmpty() : "OPEN must not be empty after initialization!";
				return initializationEvent;

			case ACTIVE:

				/* return unreturned solutions if such exist */
				if (!this.unreturnedSolutionEvents.isEmpty()) {
					this.logger.info("Returning known solution from solution cache!");
					return this.unreturnedSolutionEvents.remove(0);
				}

				/**
				 * Run while the open list is not empty and there exists a node in the open list
				 * with higher priority i.e. less k than k_n_goal (if the highest priority is a
				 * goal node, then we return in th next lines).
				 */
				// Lines 18 & 19
				GammaNode<T, A> n = this.open.poll();
				this.logger.debug("Selected {} for expansion.", n);
				if (n == null || (this.bestSeenGoalNode != null && n.getScore().compareTo(this.bestSeenGoalNode.getScore()) > 0)) {
					this.logger.info("Terminating RStar.");
					return this.terminate();
				}

				// Lines 20 & 21
				if (n.getParent() != null && !this.isPathRealizationKnownForAbstractEdgeToNode(n)) {

					/*
					 * The path that corresponds to the edge bp(s)->s has not been computed yet. Try
					 * to compute it using reevaluateState.
					 */
					this.reevaluateState(n);

					/* put the node on OPEN again */
					this.logger.debug("Putting node {} on OPEN again", n);
					this.open.add(n);

				} else { // The path from bp(s)->s has already been computed.

					// Line 23.
					this.closed.add(n);

					/* Line 24 to 27: Compute successors */
					this.logger.debug("Starting generation of successors of {}", n);
					Collection<GammaNode<T, A>> successors = this.generateGammaSuccessors(n);
					this.logger.debug("Generated {} successors.", successors.size());
					for (GammaNode<T, A> n_ : successors) { // Line 28

						/*
						 * Line 29: Initialize successors by setting the path from s to s_ to null, and
						 * by estimating the lowest cost from s to s_ with the heuristic h(s, s_).
						 */
						n.cLow.put(n_, this.hPath.h(n, n_));

						/*
						 * Lines 30 and 31 of the algorithm can be omitted here. They contain further
						 * initialization of the successors, but This is done implicitly in the
						 * generation process of the Gamma successors.
						 */

						/*
						 * If the generated successor n_ i.e. s_ has never been visited yet
						 * (n_.getParent() == null) or the actual cost to s (n.g) plus the (estimated)
						 * cost from s to s_ (c_low(s, s_)) is better than the actual known cost (n_.g)
						 * to s_, then we have to update these values for s_ (because with s we found a
						 * better predecessor for s_).
						 */
						// Line 32
						boolean isNewNode = n_.getParent() == null;
						if (isNewNode || (n.getG() + n.cLow.get(n_) < n_.getG())) {
							n_.setG(n.getG() + n.cLow.get(n_));
							n_.setParent(n);
							this.updateState(n_); // updates priority of n_ in open list.
							if (isNewNode) {
								this.logger.debug("Adding new node {} to OPEN.", n_);
								this.open.add(n_);
							}
						}
					}
				}
				return new NodeExpansionCompletedEvent<>(this, n.getHead());

			default:
				throw new IllegalStateException("Cannot do anything in state " + this.getState());
			}
		} catch (PathEvaluationException e) {
			throw new AlgorithmException("Failed due to path evaluation failure.", e);
		} finally {
			this.unregisterActiveThread();
		}
	}

	private boolean isPathRealizationKnownForAbstractEdgeToNode(final GammaNode<T, A> node) {
		return this.externalPathsBetweenGammaNodes.containsKey(new Pair<>(node.getParent(), node));
	}

	/**
	 * Calculates the path in the original graph that corresponds to the reduced
	 * gamma graph using the established path witnesses.
	 *
	 * @param n
	 * @return
	 */
	private EvaluatedSearchGraphPath<T, A, Double> getFullExternalPath(final GammaNode<T, A> n) {
		List<T> nodes = new ArrayList<>();
		List<A> edges = new ArrayList<>();
		GammaNode<T, A> current = n;
		nodes.add(n.getHead());
		while (current.getParent() != null) {
			Pair<GammaNode<T, A>, GammaNode<T, A>> pair = new Pair<>(current.getParent(), current);
			assert this.externalPathsBetweenGammaNodes.containsKey(pair);
			SearchGraphPath<T, A> externalPath = this.externalPathsBetweenGammaNodes.get(pair);
			nodes.addAll(0, externalPath.getNodes());
			List<A> concreteEdges = externalPath.getArcs();
			if (concreteEdges == null) {
				concreteEdges = new ArrayList<>();
				int m = externalPath.getNodes().size();
				for (int i = 0; i < m; i++) {
					concreteEdges.add(null);
				}
			}
			edges.addAll(0, concreteEdges);
			current = current.getParent();
		}
		return new EvaluatedSearchGraphPath<>(nodes, edges, n.getG());
	}

	/**
	 *
	 * @param n
	 * @return
	 */
	private GammaNode<T, A> argminCostToStateOverPredecessors(final GammaNode<T, A> n) {
		GammaNode<T, A> argmin = null;
		for (GammaNode<T, A> p : n.getPredecessors()) {
			if ((argmin == null) || (p.getG() + p.cLow.get(n) < argmin.getG() + argmin.cLow.get(n))) {
				argmin = p;
			}
		}
		return argmin;
	}

	/**
	 * @throws AlgorithmExecutionCanceledException @throws
	 *             AlgorithmException @throws AlgorithmTimeoutedException Generates this.RStarK
	 *             Gamma graph successors for a state s within distance this.delta. Queries the
	 *             this.gammaSuccessorGenerator and checks if a generate state has been visited
	 *             i.e. generated in Gamma before. If yes, it takes the old reference from the
	 *             this.alreadyGeneratedStates list. Also maintains the predecessor set of
	 *             nodes.
	 *
	 * @param n Gamma node to generate successors for. @return List of Gamma
	 *            nodes. @throws InterruptedException @throws
	 */
	private Collection<GammaNode<T, A>> generateGammaSuccessors(final GammaNode<T, A> n) throws InterruptedException, AlgorithmTimeoutedException, AlgorithmException, AlgorithmExecutionCanceledException {

		/*
		 * first create a list of k nodes that are in reach of delta of the current node
		 */
		this.logger.trace("Invoking distant successor generator timeout-aware.");
		List<T> randomDistantSuccessors = this.computeTimeoutAware(() -> this.getInput().getDistantSuccessorGenerator().getDistantSuccessors(n.getHead(), this.k, this.metricOverStates, this.delta), "Computing distant successors", true);
		assert randomDistantSuccessors.size() == new HashSet<>(randomDistantSuccessors).size() : "Distant successor generator has created the same successor ar least twice: \n\t "
				+ SetUtil.getMultiplyContainedItems(randomDistantSuccessors).stream().map(T::toString).collect(Collectors.joining("\n\t"));
		this.logger.trace("Distant successor generator generated {}/{} successors.", randomDistantSuccessors.size(), this.k);

		/*
		 * remove nodes for which a node is already on CLOSED (no reopening in this
		 * algorithm)
		 */
		randomDistantSuccessors.removeIf(childNode -> this.closed.stream().anyMatch(closedNode -> closedNode.getHead().equals(childNode)));
		this.logger.trace("{} successors are still considered after having removed nodes that already are on CLOSED, which holds {} item(s).", randomDistantSuccessors.size(), this.closed.size());

		/* now transform these node into (possibly existing) GammaNode objects */
		ArrayList<GammaNode<T, A>> succWithoutClosed = new ArrayList<>();
		for (T childNode : randomDistantSuccessors) {
			Optional<GammaNode<T, A>> representantOnOpen = this.open.stream().filter(closedNode -> closedNode.getHead().equals(childNode)).findFirst();
			GammaNode<T, A> gammaNodeForThisChild;
			if (representantOnOpen.isPresent()) {
				gammaNodeForThisChild = representantOnOpen.get();
			} else {
				gammaNodeForThisChild = new GammaNode<>(childNode);
				gammaNodeForThisChild.setGoal(((INodeGoalTester<T, A>) this.getInput().getGoalTester()).isGoal(childNode));
			}

			/* if this is a solution, add it as a new solution */
			if (gammaNodeForThisChild.isGoal()) {
				this.logger.info("Found new solution. Adding it to the solution set.");
				if (this.bestSeenGoalNode == null || this.bestSeenGoalNode.getG() > n.getG()) {
					this.bestSeenGoalNode = n;
					this.updateBestSeenSolution(this.getFullExternalPath(n));
				}
				EvaluatedSearchSolutionCandidateFoundEvent<T, A, Double> solutionEvent = new EvaluatedSearchSolutionCandidateFoundEvent<>(this, this.getFullExternalPath(gammaNodeForThisChild));
				this.post(solutionEvent);
				this.unreturnedSolutionEvents.add(solutionEvent);
			}
			gammaNodeForThisChild.addPredecessor(n);
			succWithoutClosed.add(gammaNodeForThisChild);
		}
		return succWithoutClosed;
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger = LoggerFactory.getLogger(name);
		super.setLoggerName(name + "._orgraphsearch");

		/* set logger name of the graph generator */
		if (this.getGraphGenerator() instanceof ILoggingCustomizable) {
			((ILoggingCustomizable) this.getGraphGenerator()).setLoggerName(name + ".graphgenerator");
		}

		/* set logger name of the distant graph generator */
		DistantSuccessorGenerator<T> distantSuccessorGenerator = this.getInput().getDistantSuccessorGenerator();
		if (distantSuccessorGenerator instanceof ILoggingCustomizable) {
			((ILoggingCustomizable) distantSuccessorGenerator).setLoggerName(name + ".distantsuccessorgenerator");
		}
	}

	@Override
	public String getLoggerName() {
		return this.logger.getName();
	}

	@Override
	public void cancel() {
		this.logger.info("RStar received cancel. Now invoking shutdown routing and cancel the AStar subroutines.");
		super.cancel();
		this.activeAStarSubroutines.forEach(IAlgorithm::cancel);
	}
}
