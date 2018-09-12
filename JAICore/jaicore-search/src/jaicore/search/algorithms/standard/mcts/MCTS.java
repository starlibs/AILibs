package jaicore.search.algorithms.standard.mcts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.algorithm.AlgorithmEvent;
import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.AlgorithmFinishedEvent;
import jaicore.basic.algorithm.AlgorithmInitializedEvent;
import jaicore.basic.algorithm.AlgorithmState;
import jaicore.basic.sets.SetUtil;
import jaicore.graph.LabeledGraph;
import jaicore.graphvisualizer.events.graphEvents.GraphInitializedEvent;
import jaicore.graphvisualizer.events.graphEvents.NodeReachedEvent;
import jaicore.graphvisualizer.events.graphEvents.NodeTypeSwitchEvent;
import jaicore.search.algorithms.standard.AbstractORGraphSearch;
import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.core.interfaces.ISolutionEvaluator;
import jaicore.search.model.other.EvaluatedSearchGraphPath;
import jaicore.search.model.probleminputs.GraphSearchProblemInput;
import jaicore.search.model.travesaltree.Node;
import jaicore.search.model.travesaltree.NodeExpansionDescription;
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
public class MCTS<N, A, V extends Comparable<V>> extends AbstractORGraphSearch<GraphSearchProblemInput<N, A, V>, Object, N, A, V, Node<N, V>, A> implements IPolicy<N, A, V> {

	private static final Logger logger = LoggerFactory.getLogger(MCTS.class);

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
	private Collection<N> nodesConsideredInAPlayout = new HashSet<>();
	private Collection<N> unexpandedNodes = new HashSet<>();
	protected final LabeledGraph<N, A> exploredGraph;
	private final Collection<N> deadLeafNodes = new HashSet<>();

	public MCTS(GraphSearchProblemInput<N, A, V> problem, IPathUpdatablePolicy<N, A, V> treePolicy, IPolicy<N, A, V> defaultPolicy) {
		super(problem);
		this.graphGenerator = problem.getGraphGenerator();
		this.rootGenerator = graphGenerator.getRootGenerator();
		this.successorGenerator = graphGenerator.getSuccessorGenerator();
		checkGoalPropertyOnEntirePath = !(graphGenerator.getGoalTester() instanceof NodeGoalTester);
		if (checkGoalPropertyOnEntirePath) {
			this.nodeGoalTester = null;
			this.pathGoalTester = (PathGoalTester<N>) graphGenerator.getGoalTester();
			;
		} else {
			this.nodeGoalTester = (NodeGoalTester<N>) graphGenerator.getGoalTester();
			this.pathGoalTester = null;
		}

		this.treePolicy = treePolicy;
		this.defaultPolicy = defaultPolicy;
		this.playoutSimulator = problem.getPathEvaluator();
		this.exploredGraph = new LabeledGraph<>();
		this.root = ((SingleRootGenerator<N>) rootGenerator).getRoot();
		this.unexpandedNodes.add(root);
		this.exploredGraph.addItem(root);
	}

	private List<N> getPlayout() throws Exception {
		logger.info("Computing a new playout ...");
		N current = root;
		N next;
		Collection<N> childrenOfCurrent;
		List<N> path = new ArrayList<>();
		path.add(current);

		/* if all children of the current node have been used at least once child that has not been used in a playout, just use any of them according to the tree policy */
		boolean currentNodeIsDeadEnd = false;
		while (!(childrenOfCurrent = exploredGraph.getSuccessors(current)).isEmpty() && (SetUtil.difference(childrenOfCurrent, nodesConsideredInAPlayout)).isEmpty()) {
			checkTermination();
			logger.debug("Using tree policy to compute choice for successor of {} among {}", current, childrenOfCurrent);
			List<A> availableActions = new ArrayList<>();
			Map<A, N> successorStates = new HashMap<>();
			for (N child : childrenOfCurrent) {
				if (deadLeafNodes.contains(child)) {
					logger.debug("Ignoring child {}, which is known to be a dead end", child);
					continue;
				}
				A action = exploredGraph.getEdgeLabel(current, child);
				availableActions.add(action);
				successorStates.put(action, child);
			}
			if (availableActions.isEmpty()) {
				logger.debug("Node {} has only dead-end successors and hence is a dead-end itself. Adding it to the list of dead ends.", current);
				currentNodeIsDeadEnd = true;
				deadLeafNodes.add(current);
				break;
			}
			logger.trace("Available actions of expanded node {}: {}. Corresponding successor states: {}", current, availableActions, successorStates);

			A chosenAction = treePolicy.getAction(current, successorStates);
			if (chosenAction == null)
				throw new IllegalStateException("Chosen action is null!");
			next = successorStates.get(chosenAction);
			if (next == null)
				throw new IllegalStateException("Next action is null!");
			logger.trace("Chosen action: {}. Successor: {}", chosenAction, next);
			current = next;
			postEvent(new NodeTypeSwitchEvent<N>(next, "expanding"));
			path.add(current);
			logger.debug("Tree policy decides to expand {} taking action {} to {}", current, chosenAction, next);
		}
		logger.info("Determined non-fully-expanded node {} of traversal tree using tree policy. Untried successors are: {}. Now selecting an untried successor.", current,
				SetUtil.difference(childrenOfCurrent, nodesConsideredInAPlayout));

		/* ask the tree policy among one of the remaining options */
		checkTermination();
		if (!currentNodeIsDeadEnd) {
			Map<A, N> successorStates = new HashMap<>();
			if (unexpandedNodes.contains(current)) {
				successorStates.putAll(expandNode(current));
			} else {
				for (N child : SetUtil.difference(childrenOfCurrent, nodesConsideredInAPlayout)) {
					A action = exploredGraph.getEdgeLabel(current, child);
					successorStates.put(action, child);
				}
			}
			if (!successorStates.isEmpty()) {
				current = successorStates.get(treePolicy.getAction(current, successorStates));
				nodesConsideredInAPlayout.add(current);
				path.add(current);
				logger.info("Selected {} as the untried successor. Now completing rest playout from this situation.", current);
			} else {
				currentNodeIsDeadEnd = true;
				deadLeafNodes.add(current);
				logger.info("Found leaf node {}. Adding to dead end list.", current);
			}
		}

		/* use default policy to proceed to a goal node */
		while (!currentNodeIsDeadEnd && !isGoal(current)) {
			checkTermination();
			Map<A, N> successorStates = new HashMap<>();
			logger.debug("Determining possible moves for {}.", current);
			if (unexpandedNodes.contains(current)) {
				successorStates.putAll(expandNode(current));
			} else {
				for (N successor : exploredGraph.getSuccessors(current)) {
					successorStates.put(exploredGraph.getEdgeLabel(current, successor), successor);
				}
			}

			/* if the default policy has led us into a state where we cannot do anything, stop playout */
			if (successorStates.isEmpty())
				break;
			current = successorStates.get(defaultPolicy.getAction(current, successorStates));
			nodesConsideredInAPlayout.add(current);
			path.add(current);
		}
		logger.info("Drawn playout path is: {}.", path);

		/* change all node types on path to closed again */
		while (true) {
			if (exploredGraph.getPredecessors(current).isEmpty())
				break;
			current = exploredGraph.getPredecessors(current).iterator().next();
			postEvent(new NodeTypeSwitchEvent<N>(current, "or_closed"));
		}
		return path;
	}

	private Map<A, N> expandNode(N node) throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException {
		checkTermination();
		if (!unexpandedNodes.contains(node))
			throw new IllegalArgumentException();
		logger.debug("Situation {} has never been analyzed before, expanding the graph at the respective point.", node);
		unexpandedNodes.remove(node);
		Collection<NodeExpansionDescription<N, A>> availableActions = null;
		try {
			availableActions = successorGenerator.generateSuccessors(node);
		} catch (InterruptedException e) {
			checkTermination();
		}
		Map<A, N> successorStates = new HashMap<>();
		for (NodeExpansionDescription<N, A> d : availableActions) {
			checkTermination();
			successorStates.put(d.getAction(), d.getTo());
			logger.debug("Adding edge {} -> {} with label {}", d.getFrom(), d.getTo(), d.getAction());
			exploredGraph.addItem(d.getTo());
			unexpandedNodes.add(d.getTo());
			exploredGraph.addEdge(d.getFrom(), d.getTo(), d.getAction());
			postEvent(new NodeReachedEvent<>(d.getFrom(), d.getTo(), isGoal(d.getTo()) ? "or_solution" : "or_open"));
		}
		return successorStates;
	}

	private boolean isGoal(N node) {
		return nodeGoalTester.isGoal(node);
	}

	@Override
	public A getAction(N node, Map<A, N> actionsWithSuccessors) {

		try {
			/* compute next solution */
			nextSolution();

			/* choose action in root that has best reward */
			return treePolicy.getAction(root, actionsWithSuccessors);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public AlgorithmEvent nextWithException() throws Exception {
		switch (getState()) {
		case created:
			activateTimeoutTimer("MCTS-Timeouter");
			postEvent(new GraphInitializedEvent<N>(root));
			switchState(AlgorithmState.active);
			AlgorithmEvent initEvent = new AlgorithmInitializedEvent();
			postEvent(initEvent);
			return initEvent;

		case active:
			if (playoutSimulator == null)
				throw new IllegalStateException("no simulator has been set!");
			logger.debug("Next algorithm iteration. Number of unexpanded nodes: {}", unexpandedNodes.size());
			try {
				registerActiveThread();
				while (getState() == AlgorithmState.active) {
					checkTermination();
					if (unexpandedNodes.isEmpty()) {
						unregisterThreadAndShutdown();
						AlgorithmEvent finishEvent = new AlgorithmFinishedEvent();
						logger.info("Finishing MCTS as all nodes have been expanded; the search graph has been exhausted.");
						postEvent(finishEvent);
						return finishEvent;
					} else {
						logger.info("There are {} known unexpanded nodes. Starting computation of next playout path.", unexpandedNodes.size());
						List<N> path = getPlayout();
						V playoutScore;
						if (!scoreCache.containsKey(path)) {
							logger.debug("Obtained path {}. Now starting computation of the score for this playout.", path);
							playoutScore = playoutSimulator.evaluateSolution(path);
							boolean isSolutionPlayout = nodeGoalTester.isGoal(path.get(path.size() - 1));
							logger.debug("Determined playout score {}. Is goal: {}. Now updating the path.", playoutScore, isSolutionPlayout);
							scoreCache.put(path, playoutScore);
							treePolicy.updatePath(path, playoutScore);
							if (isSolutionPlayout) {
								AlgorithmEvent solutionEvent = registerSolution(new EvaluatedSearchGraphPath<>(path, null, playoutScore));
								return solutionEvent;
							}
						} else {
							playoutScore = scoreCache.get(path);
							logger.debug("Looking up score {} for the already evaluated path {}", playoutScore, path);
							treePolicy.updatePath(path, playoutScore);
						}
					}
				}
			} catch (TimeoutException e) {
				unregisterThreadAndShutdown();
				Thread.interrupted(); // unset interrupted flag
				AlgorithmEvent finishEvent = new AlgorithmFinishedEvent();
				logger.info("Finishing MCTS due to timeout.");
				postEvent(finishEvent);
				return finishEvent;
			} finally {

				/* unregister this thread in order to avoid interruptions */
				unregisterActiveThread();
			}

		default:
			throw new UnsupportedOperationException("Cannot do anything in state " + getState());
		}
	}

	@Override
	public void setNumCPUs(int numberOfCPUs) {
		logger.warn("Currently no support for parallelization");
	}

	@Override
	public int getNumCPUs() {
		return 1;
	}

	@Override
	public Object getSolutionProvidedToCall() {
		return null;
	}
}