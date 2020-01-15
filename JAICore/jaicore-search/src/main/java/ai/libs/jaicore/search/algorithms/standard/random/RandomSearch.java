package ai.libs.jaicore.search.algorithms.standard.random;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.api4.java.ai.graphsearch.problem.IGraphSearchInput;
import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.PathGoalTester;
import org.api4.java.algorithm.events.AlgorithmEvent;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.api4.java.common.control.ILoggingCustomizable;
import org.api4.java.datastructure.graph.IPath;
import org.api4.java.datastructure.graph.implicit.NodeExpansionDescription;
import org.api4.java.datastructure.graph.implicit.SingleRootGenerator;
import org.api4.java.datastructure.graph.implicit.SingleSuccessorGenerator;
import org.api4.java.datastructure.graph.implicit.SuccessorGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.sets.SetUtil;
import ai.libs.jaicore.graph.Graph;
import ai.libs.jaicore.graph.LabeledGraph;
import ai.libs.jaicore.graphvisualizer.events.graph.GraphInitializedEvent;
import ai.libs.jaicore.graphvisualizer.events.graph.NodeAddedEvent;
import ai.libs.jaicore.graphvisualizer.events.graph.NodeTypeSwitchEvent;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.events.GraphSearchSolutionCandidateFoundEvent;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.events.RolloutEvent;
import ai.libs.jaicore.search.core.interfaces.AAnyPathInORGraphSearch;
import ai.libs.jaicore.search.model.other.SearchGraphPath;

/**
 * This search randomly draws paths from the root. At every node, each successor is chosen with the same probability except if a priority predicate is defined. A priority predicate says whether or not a node lies on a path that has
 * priority. A node only has priority until all successors that have priority are exhausted.
 *
 * @author fmohr
 *
 * @param <N>
 * @param <A>
 */
public class RandomSearch<N, A> extends AAnyPathInORGraphSearch<IGraphSearchInput<N, A>, SearchGraphPath<N, A>, N, A> implements ILoggingCustomizable {

	/* logging */
	private String loggerName;
	private Logger logger = LoggerFactory.getLogger(RandomSearch.class);

	private final IPath<N, A> root;
	private final SuccessorGenerator<N, A> gen;
	private final boolean isSingleNodeSuccessorGenerator;
	private final PathGoalTester<N, A> goalTester;
	private final LabeledGraph<N, A> exploredGraph = new LabeledGraph<>();
	private final Set<N> closed = new HashSet<>();
	private final Predicate<N> priorityPredicate;
	private final Set<N> prioritizedNodes = new HashSet<>();
	private final Set<N> exhausted = new HashSet<>(); // the set of nodes of which all solution paths have been computed
	private final Random random;

	private final Queue<SearchGraphPath<N, A>> foundSolutions = new LinkedList<>();

	public RandomSearch(final IGraphSearchInput<N, A> problem) {
		this(problem, 0);
	}

	public RandomSearch(final IGraphSearchInput<N, A> problem, final int seed) {
		this(problem, new Random(seed));
	}

	public RandomSearch(final IGraphSearchInput<N, A> problem, final Random random) {
		this(problem, null, random);
	}

	public RandomSearch(final IGraphSearchInput<N, A> problem, final Predicate<N> priorityPredicate, final Random random) {
		super(problem);
		N rootNode = ((SingleRootGenerator<N>) problem.getGraphGenerator().getRootGenerator()).getRoot();
		this.gen = problem.getGraphGenerator().getSuccessorGenerator();
		this.isSingleNodeSuccessorGenerator = this.gen instanceof SingleSuccessorGenerator;
		this.goalTester = problem.getGoalTester();
		this.exploredGraph.addItem(rootNode);
		this.root = new SearchGraphPath<>(rootNode);
		this.logger.debug("Added root node {} with the path {} to the model.", rootNode, this.root);
		this.random = random;
		this.priorityPredicate = priorityPredicate;
	}

	/**
	 * This expansion either generates all successors of a node (if the successor generator function is not able to provide single successor) or only one new successor. The node is put on CLOSED once all successors have been generated.
	 *
	 * Note that the fact that a new successor is generated does not mean that the algorithm will choose the newly generated successor to be appended to the paths.
	 *
	 * @param node
	 * @throws InterruptedException
	 * @throws AlgorithmExecutionCanceledException
	 * @throws AlgorithmTimeoutedException
	 */
	private void expandPath(final IPath<N, A> path) throws InterruptedException, AlgorithmTimeoutedException, AlgorithmExecutionCanceledException {
		this.logger.debug("Starting expansion of path {}", path);
		synchronized (this.exploredGraph) {
			assert this.exploredGraph.isGraphSane();
			assert !this.goalTester.isGoal(path) : "Goal nodes cannot be expanded!";
			N node = path.getHead();
			assert this.exploredGraph.hasItem(node) : "Node that shall be expanded is not part of the graph: " + node;
			assert !this.closed.contains(node);
			this.logger.debug("Expanding next node {}", node);
			boolean closeNodeAfterwards = false;
			boolean nodeAdded = false;
			if (this.isSingleNodeSuccessorGenerator) {

				/* generate the next successor */
				SingleSuccessorGenerator<N, A> cGen = ((SingleSuccessorGenerator<N, A>) this.gen);
				for (int i = 0; i < 3 && !nodeAdded; i++) {
					assert this.exploredGraph.isGraphSane();
					NodeExpansionDescription<N, A> successor = cGen.generateSuccessor(node, this.random.nextInt(Integer.MAX_VALUE));
					assert this.exploredGraph.isGraphSane();
					if (successor == null) {
						continue;
					}
					assert this.exploredGraph.hasItem(node) : "Parent node of successor is not part of the explored graph.";
					if (this.exploredGraph.getSuccessors(node).contains(successor.getTo())) {
						this.logger.trace("Single node generator has generated a known successor. Generating another candidate.");
						continue;
					}
					assert !this.exploredGraph.hasItem(successor.getTo()) : "Successor " + successor.getTo() + " has been reached before. Predecessors of that node are: " + this.exploredGraph.getPredecessors(successor.getTo());
					this.addNodeToLocalModel(path, successor.getTo(), successor.getAction());
					nodeAdded = true;
				}

				/* if this was the last successor, set the close node flag to 1 */
				closeNodeAfterwards = cGen.allSuccessorsComputed(node);
			}

			/* if no node has been added yet (either because this is not a SingleNodeGenerator or because the SingleNodeGenerator did not produce any new successor) */
			if (!nodeAdded) {
				long start = System.currentTimeMillis();
				List<NodeExpansionDescription<N, A>> successors = this.gen.generateSuccessors(node); // could have been interrupted here
				this.logger.debug("Identified {} successor(s) in {}ms, which are now appended.", successors.size(), System.currentTimeMillis() - start);
				Collection<N> knownSuccessors = this.exploredGraph.getSuccessors(node);
				long lastTerminationCheck = 0;
				int addedSuccessors = 0;
				for (NodeExpansionDescription<N, A> successor : successors) {
					if (System.currentTimeMillis() - lastTerminationCheck > 100) {
						this.checkAndConductTermination();
						lastTerminationCheck = System.currentTimeMillis();
					}
					if (knownSuccessors.contains(successor.getTo())) {
						this.logger.debug("Skipping successor {}, which is already part of the model.", successor.getTo());
					} else {
						this.addNodeToLocalModel(path, successor.getTo(), successor.getAction());
						addedSuccessors++;
					}
				}
				this.logger.debug("{} nodes have been added to the local model. Now checking prioritization.", addedSuccessors);

				/* if the node has successors but none of them is prioritized, remove the node from the priority list */
				if (!this.exploredGraph.getSuccessors(node).isEmpty() && this.prioritizedNodes.contains(node)) {
					this.prioritizedNodes.remove(node);
					this.updateExhaustedAndPrioritizedState(node);
				}
				closeNodeAfterwards = true;
			}

			if (closeNodeAfterwards) {

				/* if the node was not prioritized, change its state */
				if (!this.prioritizedNodes.contains(node)) {
					this.post(new NodeTypeSwitchEvent<N>(this.getId(), node, "or_closed"));
				}
				this.closed.add(node);
			}
			this.logger.debug("Finished node expansion. Sizes of explored graph and CLOSED are {} and {} respectively.", this.exploredGraph.getItems().size(), this.closed.size());
		}
	}

	private SearchGraphPath<N, A> addNodeToLocalModel(final IPath<N, A> path, final N to, final A label) {
		assert this.exploredGraph.isGraphSane();
		N from = path.getHead();
		assert from != null;
		assert to != null;
		assert !this.exploredGraph.hasItem(to) : "Cannot attach node " + to + " to path " + path.getNodes() + " of local model, because it is already contained in the explored graph! Known nodes: " + this.exploredGraph.getItems().stream().map(s -> "\n\t" + s).collect(Collectors.joining());
		assert this.exploredGraph.hasItem(from) : "The head " + from + " of the path with " + path.getNumberOfNodes() + " nodes is not part of the explored graph! Here is the path: \n\t"
		+ path.getNodes().stream().map(Object::toString).collect(Collectors.joining("\n\t"));
		this.exploredGraph.addItem(to);
		this.logger.debug("Added node {} to graph.", to);
		assert this.exploredGraph.hasItem(to);
		assert this.exploredGraph.isGraphSane();
		boolean isPrioritized = this.priorityPredicate != null && this.priorityPredicate.test(to);
		if (isPrioritized) {
			this.prioritizedNodes.add(to);
		}
		this.exploredGraph.addEdge(from, to, label);
		SearchGraphPath<N, A> extendedPath = new SearchGraphPath<>(path, to, label);
		boolean isGoalNode = this.goalTester.isGoal(extendedPath);
		NodeAddedEvent<N> event = new NodeAddedEvent<>(this.getId(), from, to, isGoalNode ? "or_solution" : (isPrioritized ? "or_prioritized" : "or_open"));
		this.logger.debug("Added node {} as a successor of {} with edge label {} to the model.", to, from, label);
		this.post(event);
		this.logger.trace("Sent {} for algorithm {} and node {}", event.getClass().getSimpleName(), event.getAlgorithmId(), event.getNode());
		if (isGoalNode) {
			this.logger.info("Found solution");
			this.foundSolutions.add(extendedPath);
		}
		return extendedPath;
	}

	@Override
	public AlgorithmEvent nextWithException() throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException, AlgorithmException {
		try {
			this.registerActiveThread();
			this.logger.debug("Starting next algorithm step.");
			assert this.exploredGraph.isGraphSane();
			switch (this.getState()) {
			case CREATED:
				GraphInitializedEvent<N> initEvent = new GraphInitializedEvent<>(this.getId(), this.root.getRoot());
				this.post(initEvent);
				this.logger.trace("Sent {} for id {} with root {}", initEvent.getClass().getSimpleName(), initEvent.getAlgorithmId(), initEvent.getRoot());
				this.logger.info("Starting random search ...");
				assert this.exploredGraph.isGraphSane();
				return this.activate();

			case ACTIVE:

				/* if there are still known paths, just return them */
				if (!this.foundSolutions.isEmpty()) {
					AlgorithmEvent event = new GraphSearchSolutionCandidateFoundEvent<>(this.getId(), this.foundSolutions.poll());
					this.logger.info("Identified new solution. Event is {}", event);
					this.post(event);
					return event;
				}

				/* if the root is exhausted, cancel */
				SearchGraphPath<N, A> drawnPath = null;
				drawnPath = this.nextSolutionUnderSubPath(this.root);
				if (drawnPath == null) {
					this.logger.info("Drew NULL path, terminating");
					return this.terminate();
				}
				assert !drawnPath.getNodes().isEmpty() && this.goalTester.isGoal(drawnPath) : "The drawn path is empty or its leaf node is not a goal!";
				this.logger.info("Drew path of length {}. Posting this event. For more details on the path, enable TRACE", drawnPath.getNodes().size());
				this.logger.trace("The drawn path is {}", drawnPath);
				AlgorithmEvent event = new GraphSearchSolutionCandidateFoundEvent<>(this.getId(), drawnPath);
				this.foundSolutions.remove(drawnPath);
				this.logger.info("Identified new solution. Event is {}", event);
				this.post(event);
				assert this.exploredGraph.isGraphSane();
				return event;

			default:
				throw new IllegalStateException("Cannot do anything in state " + this.getState());
			}
		} catch (InterruptedException e) {
			if (this.hasThreadBeenInterruptedDuringShutdown(Thread.currentThread())) {
				this.checkTermination(false);
				assert false : "The thread has been interrupted due to shutdown but apparently no stopping criterion is satisfied!";
				throw new AlgorithmException("This part should never be reached!");
			} else {
				throw e;
			}
		} finally {
			this.unregisterActiveThread();
		}
	}

	public boolean knowsNode(final N node) {
		synchronized (this.exploredGraph) {
			return this.exploredGraph.getItems().contains(node);
		}
	}

	public void appendPathToModel(final IPath<N, A> path) {
		IPath<N, A> cPath = new SearchGraphPath<>(path.getRoot());
		for (N node : path.getNodes()) {
			if (!this.exploredGraph.getItems().contains(node)) {
				cPath = this.addNodeToLocalModel(cPath, node, path.getInArc(node));
			}
		}
	}

	public SearchGraphPath<N, A> nextSolutionUnderSubPath(final IPath<N, A> path) throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException {
		this.logger.info("Looking for next solution under node {}. Remaining time is {}.", path, this.getRemainingTimeToDeadline());
		this.checkAndConductTermination();
		assert this.exploredGraph.isGraphSane();

		/* if the root is exhausted, cancel */
		if (this.exhausted.contains(path.getHead())) {
			return null;
		}

		/* conduct a random walk from the root to a goal */
		SearchGraphPath<N, A> cPath = new SearchGraphPath<>(path);
		N head = cPath.getHead();
		synchronized (this.exploredGraph) {
			while (!this.goalTester.isGoal(cPath)) {
				this.checkAndConductTermination();
				assert this.checkThatNodeExistsInExploredGraph(head);
				assert this.exploredGraph.isGraphSane();

				/* expand node if this has not happened yet. */
				if (!this.closed.contains(head)) {
					this.logger.debug("Current head {} has not been expanded yet, expanding it now.", head);
					this.expandPath(cPath);
				}

				/* get unexhausted successors */
				List<N> successors = this.exploredGraph.getSuccessors(head).stream().filter(n -> !this.exhausted.contains(n)).collect(Collectors.toList());
				assert this.exploredGraph.getSuccessors(head).stream().filter(n -> !this.exploredGraph.hasItem(n)).collect(Collectors.toList()).isEmpty() : "Corrupt exploration graph: Some successors cannot be found again in the graph: "
				+ this.exploredGraph.getSuccessors(head).stream().filter(n -> !this.exploredGraph.hasItem(n)).collect(Collectors.toList());

				/* if we are in a dead end, mark the node as exhausted and remove the head again */
				if (successors.isEmpty()) {
					this.logger.debug("Detected a dead-end in {}.", head);
					this.exhausted.add(head);
					this.prioritizedNodes.remove(head); // remove prioritized node from list if it is in
					if (cPath.isPoint()) {
						this.logger.debug("The graph has been exhausted.");
						return null;
					}
					cPath = cPath.getPathToParentOfHead();
					head = cPath.getHead();
					this.logger.debug("Reset head due to dead-end to parent. New head: {}.", head);
					continue;
				}

				/* if at least one of the successors is prioritized, choose one of those; otherwise choose one at random */
				assert SetUtil.intersection(this.exhausted, this.prioritizedNodes).isEmpty() : "There are nodes that are both exhausted and prioritized, which must not be the case:"
				+ SetUtil.intersection(this.exhausted, this.prioritizedNodes).stream().map(n -> "\n\t" + n).collect(Collectors.joining());
				Collection<N> prioritizedSuccessors = SetUtil.intersection(successors, this.prioritizedNodes);
				N lastHead = head;
				if (!prioritizedSuccessors.isEmpty()) {
					head = prioritizedSuccessors.iterator().next();
				} else {
					int n = successors.size();
					assert n != 0 : "Ended up in a situation where only exhausted nodes can be chosen.";
					int k = this.random.nextInt(n);
					head = successors.get(k);
					final N tmpHead = head; // needed for stream in assertion
					assert !cPath.containsNode(head) : "Going in circles ... " + cPath.getNodes().stream().map(pn -> "\n\t[" + (pn.equals(tmpHead) ? "*" : " ") + "]" + pn.toString()).collect(Collectors.joining()) + "\n\t[*]" + head;
					this.logger.trace("Selected {} as new head.", head);
					assert this.checkThatNodeExistsInExploredGraph(head);
				}
				cPath = new SearchGraphPath<>(cPath, head, this.exploredGraph.getEdgeLabel(lastHead, head));
			}
		}

		/* propagate exhausted state */
		this.logger.trace("Head node {} has been exhausted.", head);
		this.exhausted.add(head);
		this.prioritizedNodes.remove(head);
		this.updateExhaustedAndPrioritizedState(head);
		if (head != this.root) {
			this.post(new RolloutEvent<N, Double>(this.getId(), cPath.getNodes(), null));
		}
		return head == this.root ? null : cPath;
	}

	private boolean checkThatNodeExistsInExploredGraph(final N node) {
		assert this.exploredGraph.hasItem(node) : "Head node of random path is not in explored graph: " + node;
		return true;
	}

	/**
	 * This method goes from the given node up to the root and checks, for each node on this path, whether all of its children have been exhausted.
	 *
	 * @param node
	 */
	private void updateExhaustedAndPrioritizedState(final N node) {
		synchronized (this.exploredGraph) {
			N current = node;
			Collection<N> predecessors;
			while (!(predecessors = this.exploredGraph.getPredecessors(current)).isEmpty()) {
				assert predecessors.size() == 1;
				current = predecessors.iterator().next();

				/* if the currently considered node is not even fully expanded, it is certainly not exhausted */
				boolean currentIsCompletelyExpanded = !this.isSingleNodeSuccessorGenerator || ((SingleSuccessorGenerator<N, A>) this.gen).allSuccessorsComputed(current);
				if (!currentIsCompletelyExpanded) {
					this.logger.trace("Leaving update routine at node {}, which has not been expanded completely.", current);
					return;
				}

				boolean currentIsPrioritized = this.prioritizedNodes.contains(current);
				boolean allChildrenExhausted = true;
				boolean allPrioritizedChildrenExhausted = true;
				for (N successor : this.exploredGraph.getSuccessors(current)) {
					if (!this.exhausted.contains(successor)) {
						allChildrenExhausted = false;
						if (currentIsPrioritized && this.prioritizedNodes.contains(successor)) {
							allPrioritizedChildrenExhausted = false;
							break;
						} else if (!currentIsPrioritized) {
							break;
						}
					}
				}
				if (allChildrenExhausted) {
					this.logger.trace("Update state of {} as being exhausted since all its children have been exhausted.", current);
					this.exhausted.add(current);
				}
				if (currentIsPrioritized && allPrioritizedChildrenExhausted) {
					int sizeBefore = this.prioritizedNodes.size();
					this.prioritizedNodes.remove(current);
					this.post(new NodeTypeSwitchEvent<N>(this.getId(), current, "or_closed"));
					int sizeAfter = this.prioritizedNodes.size();
					assert sizeAfter == sizeBefore - 1;
				}
			}
		}
	}

	public Graph<N> getExploredGraph() {
		return this.exploredGraph;
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger.info("Switch logger name from {} to {}", this.loggerName, name);
		this.loggerName = name;
		this.logger = LoggerFactory.getLogger(this.loggerName);
		if (this.getGraphGenerator() instanceof ILoggingCustomizable) {
			((ILoggingCustomizable) this.getGraphGenerator()).setLoggerName(name + ".graphgen");
		}
		this.logger.info("Switched logger name to {}", this.loggerName);
		super.setLoggerName(this.loggerName + "._algorithm");
	}

	@Override
	public String getLoggerName() {
		return this.loggerName;
	}
}