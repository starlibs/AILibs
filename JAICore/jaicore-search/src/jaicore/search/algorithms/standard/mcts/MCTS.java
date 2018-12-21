package jaicore.search.algorithms.standard.mcts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.AlgorithmState;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.events.AlgorithmFinishedEvent;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.basic.algorithm.exceptions.ObjectEvaluationFailedException;
import jaicore.basic.sets.SetUtil;
import jaicore.graph.LabeledGraph;
import jaicore.graphvisualizer.events.graphEvents.GraphInitializedEvent;
import jaicore.graphvisualizer.events.graphEvents.NodeReachedEvent;
import jaicore.graphvisualizer.events.graphEvents.NodeTypeSwitchEvent;
import jaicore.search.core.interfaces.AOptimalPathInORGraphSearch;
import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.core.interfaces.ISolutionEvaluator;
import jaicore.search.model.other.EvaluatedSearchGraphPath;
import jaicore.search.model.travesaltree.Node;
import jaicore.search.model.travesaltree.NodeExpansionDescription;
import jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;
import jaicore.search.structure.graphgenerator.NodeGoalTester;
import jaicore.search.structure.graphgenerator.PathGoalTester;
import jaicore.search.structure.graphgenerator.RootGenerator;
import jaicore.search.structure.graphgenerator.SingleRootGenerator;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;

/**
 * MCTS algorithm implementation.
 *
 * @author Felix Mohr
 */
public class MCTS<N, A, V extends Comparable<V>> extends AOptimalPathInORGraphSearch<GraphSearchWithPathEvaluationsInput<N, A, V>, N, A, V, Node<N, V>, A> implements IPolicy<N, A, V> {

	private Logger logger = LoggerFactory.getLogger(MCTS.class);
	private String loggerName;

	/* communication */
	protected final Map<N, Node<N, V>> ext2int = new HashMap<>();

	protected final GraphGenerator<N, A> graphGenerator;
	protected final RootGenerator<N> rootGenerator;
	protected final SuccessorGenerator<N, A> successorGenerator;
	protected final boolean checkGoalPropertyOnEntirePath;
	protected final PathGoalTester<N> pathGoalTester;
	protected final NodeGoalTester<N> nodeGoalTester;

	protected final IPathUpdatablePolicy<N, A, V> treePolicy;
	protected final IPolicy<N, A, V> defaultPolicy;
	protected final ISolutionEvaluator<N, V> playoutSimulator;

	protected final Map<List<N>, V> playouts = new HashMap<>();
	private final Map<List<N>, V> scoreCache = new HashMap<>(); // @TODO: doppelt?

	private final N root;
	private final Collection<N> nodesConsideredInAPlayout = new HashSet<>();
	private final Collection<N> unexpandedNodes = new HashSet<>();
	protected final LabeledGraph<N, A> exploredGraph;
	private final Collection<N> deadLeafNodes = new HashSet<>();

	public MCTS(final GraphSearchWithPathEvaluationsInput<N, A, V> problem, final IPathUpdatablePolicy<N, A, V> treePolicy, final IPolicy<N, A, V> defaultPolicy) {
		super(problem);
		this.graphGenerator = problem.getGraphGenerator();
		this.rootGenerator = this.graphGenerator.getRootGenerator();
		this.successorGenerator = this.graphGenerator.getSuccessorGenerator();
		this.checkGoalPropertyOnEntirePath = !(this.graphGenerator.getGoalTester() instanceof NodeGoalTester);
		if (this.checkGoalPropertyOnEntirePath) {
			this.nodeGoalTester = null;
			this.pathGoalTester = (PathGoalTester<N>) this.graphGenerator.getGoalTester();
			;
		} else {
			this.nodeGoalTester = (NodeGoalTester<N>) this.graphGenerator.getGoalTester();
			this.pathGoalTester = null;
		}

		this.treePolicy = treePolicy;
		this.defaultPolicy = defaultPolicy;
		this.playoutSimulator = problem.getPathEvaluator();
		this.exploredGraph = new LabeledGraph<>();
		this.root = ((SingleRootGenerator<N>) this.rootGenerator).getRoot();
		this.unexpandedNodes.add(this.root);
		this.exploredGraph.addItem(this.root);
	}

	private List<N> getPlayout() throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException  {
		this.logger.info("Computing a new playout ...");
		N current = this.root;
		N next;
		Collection<N> childrenOfCurrent;
		List<N> path = new ArrayList<>();
		path.add(current);

		/* if all children of the current node have been used at least once child that has not been used in a playout, just use any of them according to the tree policy */
		boolean currentNodeIsDeadEnd = false;
		while (!(childrenOfCurrent = this.exploredGraph.getSuccessors(current)).isEmpty() && (SetUtil.difference(childrenOfCurrent, this.nodesConsideredInAPlayout)).isEmpty()) {
			this.checkTermination();
			this.logger.debug("Using tree policy to compute choice for successor of {} among {}", current, childrenOfCurrent);
			List<A> availableActions = new ArrayList<>();
			Map<A, N> successorStates = new HashMap<>();
			for (N child : childrenOfCurrent) {
				if (this.deadLeafNodes.contains(child)) {
					this.logger.debug("Ignoring child {}, which is known to be a dead end", child);
					continue;
				}
				A action = this.exploredGraph.getEdgeLabel(current, child);
				availableActions.add(action);
				successorStates.put(action, child);
			}
			if (availableActions.isEmpty()) {
				this.logger.debug("Node {} has only dead-end successors and hence is a dead-end itself. Adding it to the list of dead ends.", current);
				currentNodeIsDeadEnd = true;
				this.deadLeafNodes.add(current);
				break;
			}
			this.logger.trace("Available actions of expanded node {}: {}. Corresponding successor states: {}", current, availableActions, successorStates);

			A chosenAction = this.treePolicy.getAction(current, successorStates);
			if (chosenAction == null) {
				throw new IllegalStateException("Chosen action is null!");
			}
			next = successorStates.get(chosenAction);
			if (next == null) {
				throw new IllegalStateException("Next action is null!");
			}
			this.logger.trace("Chosen action: {}. Successor: {}", chosenAction, next);
			current = next;
			this.post(new NodeTypeSwitchEvent<N>(next, "expanding"));
			path.add(current);
			this.logger.debug("Tree policy decides to expand {} taking action {} to {}", current, chosenAction, next);
		}
		this.logger.info("Determined non-fully-expanded node {} of traversal tree using tree policy. Untried successors are: {}. Now selecting an untried successor.", current,
				SetUtil.difference(childrenOfCurrent, this.nodesConsideredInAPlayout));

		/* ask the tree policy among one of the remaining options */
		this.checkTermination();
		if (!currentNodeIsDeadEnd) {
			Map<A, N> successorStates = new HashMap<>();
			if (this.unexpandedNodes.contains(current)) {
				successorStates.putAll(this.expandNode(current));
			} else {
				for (N child : SetUtil.difference(childrenOfCurrent, this.nodesConsideredInAPlayout)) {
					A action = this.exploredGraph.getEdgeLabel(current, child);
					successorStates.put(action, child);
				}
			}
			if (!successorStates.isEmpty()) {
				current = successorStates.get(this.treePolicy.getAction(current, successorStates));
				this.nodesConsideredInAPlayout.add(current);
				path.add(current);
				this.logger.info("Selected {} as the untried successor. Now completing rest playout from this situation.", current);
			} else {
				currentNodeIsDeadEnd = true;
				this.deadLeafNodes.add(current);
				this.logger.info("Found leaf node {}. Adding to dead end list.", current);
			}
		}

		/* use default policy to proceed to a goal node */
		while (!currentNodeIsDeadEnd && !this.isGoal(current)) {
			this.checkTermination();
			Map<A, N> successorStates = new HashMap<>();
			this.logger.debug("Determining possible moves for {}.", current);
			if (this.unexpandedNodes.contains(current)) {
				successorStates.putAll(this.expandNode(current));
			} else {
				for (N successor : this.exploredGraph.getSuccessors(current)) {
					successorStates.put(this.exploredGraph.getEdgeLabel(current, successor), successor);
				}
			}

			/* if the default policy has led us into a state where we cannot do anything, stop playout */
			if (successorStates.isEmpty()) {
				break;
			}
			current = successorStates.get(this.defaultPolicy.getAction(current, successorStates));
			this.nodesConsideredInAPlayout.add(current);
			path.add(current);
		}
		this.logger.info("Drawn playout path is: {}.", path);

		/* change all node types on path to closed again */
		while (true) {
			if (this.exploredGraph.getPredecessors(current).isEmpty()) {
				break;
			}
			current = this.exploredGraph.getPredecessors(current).iterator().next();
			this.post(new NodeTypeSwitchEvent<N>(current, "or_closed"));
		}
		return path;
	}

	private Map<A, N> expandNode(final N node) throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException {
		this.checkTermination();
		if (!this.unexpandedNodes.contains(node)) {
			throw new IllegalArgumentException();
		}
		this.logger.debug("Situation {} has never been analyzed before, expanding the graph at the respective point.", node);
		this.unexpandedNodes.remove(node);
		Collection<NodeExpansionDescription<N, A>> availableActions = null;
		try {
			availableActions = this.successorGenerator.generateSuccessors(node);
		} catch (InterruptedException e) {
			this.checkTermination();
		}
		Map<A, N> successorStates = new HashMap<>();
		for (NodeExpansionDescription<N, A> d : availableActions) {
			this.checkTermination();
			successorStates.put(d.getAction(), d.getTo());
			this.logger.debug("Adding edge {} -> {} with label {}", d.getFrom(), d.getTo(), d.getAction());
			this.exploredGraph.addItem(d.getTo());
			this.unexpandedNodes.add(d.getTo());
			this.exploredGraph.addEdge(d.getFrom(), d.getTo(), d.getAction());
			this.post(new NodeReachedEvent<>(d.getFrom(), d.getTo(), this.isGoal(d.getTo()) ? "or_solution" : "or_open"));
		}
		return successorStates;
	}

	private boolean isGoal(final N node) {
		return this.nodeGoalTester.isGoal(node);
	}

	@Override
	public A getAction(final N node, final Map<A, N> actionsWithSuccessors) {

		try {
			/* compute next solution */
			this.nextSolutionCandidate();

			/* choose action in root that has best reward */
			return this.treePolicy.getAction(this.root, actionsWithSuccessors);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public AlgorithmEvent nextWithException() throws InterruptedException, AlgorithmExecutionCanceledException, CancellationException, AlgorithmException {
		switch (this.getState()) {
		case created:
			this.post(new GraphInitializedEvent<N>(this.root));
			return activate();

		case active:
			if (this.playoutSimulator == null) {
				throw new IllegalStateException("no simulator has been set!");
			}
			this.logger.debug("Next algorithm iteration. Number of unexpanded nodes: {}", this.unexpandedNodes.size());
			try {
				this.registerActiveThread();
				while (this.getState() == AlgorithmState.active) {
					this.checkTermination();
					if (this.unexpandedNodes.isEmpty()) {
						this.unregisterThreadAndShutdown();
						AlgorithmEvent finishEvent = new AlgorithmFinishedEvent();
						this.logger.info("Finishing MCTS as all nodes have been expanded; the search graph has been exhausted.");
						this.post(finishEvent);
						return finishEvent;
					} else {
						this.logger.info("There are {} known unexpanded nodes. Starting computation of next playout path.", this.unexpandedNodes.size());
						List<N> path = this.getPlayout();
						V playoutScore;
						if (!this.scoreCache.containsKey(path)) {
							this.logger.debug("Obtained path {}. Now starting computation of the score for this playout.", path);
							playoutScore = this.playoutSimulator.evaluateSolution(path);
							boolean isSolutionPlayout = this.nodeGoalTester.isGoal(path.get(path.size() - 1));
							this.logger.debug("Determined playout score {}. Is goal: {}. Now updating the path.", playoutScore, isSolutionPlayout);
							this.scoreCache.put(path, playoutScore);
							this.treePolicy.updatePath(path, playoutScore);
							if (isSolutionPlayout) {
								AlgorithmEvent solutionEvent = this.registerSolution(new EvaluatedSearchGraphPath<>(path, null, playoutScore));
								return solutionEvent;
							}
						} else {
							playoutScore = this.scoreCache.get(path);
							this.logger.debug("Looking up score {} for the already evaluated path {}", playoutScore, path);
							this.treePolicy.updatePath(path, playoutScore);
						}
					}
				}
			} catch (TimeoutException e) {
				this.unregisterThreadAndShutdown();
				Thread.interrupted(); // unset interrupted flag
				AlgorithmEvent finishEvent = new AlgorithmFinishedEvent();
				this.logger.info("Finishing MCTS due to timeout.");
				this.post(finishEvent);
				return finishEvent;
			} catch (ObjectEvaluationFailedException e) {
				throw new AlgorithmException(e, "Could not evaluate playout!");
			} finally {

				/* unregister this thread in order to avoid interruptions */
				this.unregisterActiveThread();
			}

		default:
			throw new UnsupportedOperationException("Cannot do anything in state " + this.getState());
		}
	}

	@Override
	public String getLoggerName() {
		return this.loggerName;
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger.info("Switching logger from {} to {}", this.logger.getName(), name);
		this.loggerName = name;
		this.logger = LoggerFactory.getLogger(name);
		this.logger.info("Activated logger {} with name {}", name, this.logger.getName());
		super.setLoggerName(this.loggerName + "._orgraphsearch");
	}
}