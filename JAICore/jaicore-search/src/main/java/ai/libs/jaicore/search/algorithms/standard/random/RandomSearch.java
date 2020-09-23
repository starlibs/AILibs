package ai.libs.jaicore.search.algorithms.standard.random;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.api4.java.ai.graphsearch.problem.IPathSearchInput;
import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.IPathGoalTester;
import org.api4.java.algorithm.events.IAlgorithmEvent;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.api4.java.common.control.ILoggingCustomizable;
import org.api4.java.common.control.IRandomConfigurable;
import org.api4.java.datastructure.graph.ILabeledPath;
import org.api4.java.datastructure.graph.implicit.ILazySuccessorGenerator;
import org.api4.java.datastructure.graph.implicit.INewNodeDescription;
import org.api4.java.datastructure.graph.implicit.ISingleRootGenerator;
import org.api4.java.datastructure.graph.implicit.ISuccessorGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.sets.SetUtil;
import ai.libs.jaicore.graph.Graph;
import ai.libs.jaicore.graph.LabeledGraph;
import ai.libs.jaicore.graphvisualizer.events.graph.GraphInitializedEvent;
import ai.libs.jaicore.graphvisualizer.events.graph.NodeAddedEvent;
import ai.libs.jaicore.graphvisualizer.events.graph.NodeTypeSwitchEvent;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.events.GraphSearchSolutionCandidateFoundEvent;
import ai.libs.jaicore.search.algorithms.standard.random.exception.IllegalArgumentForPathExtensionException;
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
public class RandomSearch<N, A> extends AAnyPathInORGraphSearch<IPathSearchInput<N, A>, SearchGraphPath<N, A>, N, A> implements ILoggingCustomizable {

	/* logging */
	private String loggerName;
	private Logger logger = LoggerFactory.getLogger(RandomSearch.class);

	private final ILabeledPath<N, A> root;
	private final ISuccessorGenerator<N, A> gen;
	private final boolean isRandomizableSingleNodeSuccessorGenerator;
	private final IPathGoalTester<N, A> goalTester;
	private final LabeledGraph<N, A> exploredGraph = new LabeledGraph<>();
	private final Set<N> closed = new HashSet<>();
	private final Predicate<N> priorityPredicate;
	private final Set<N> prioritizedNodes = new HashSet<>();
	private final Set<N> exhausted = new HashSet<>(); // the set of nodes of which all solution paths have been computed
	private final Random random;
	private final Map<N, Iterator<INewNodeDescription<N, A>>> successorGenerators = new HashMap<>();

	private int iterations = 0;

	public RandomSearch(final IPathSearchInput<N, A> problem) {
		this(problem, 0);
	}

	public RandomSearch(final IPathSearchInput<N, A> problem, final int seed) {
		this(problem, new Random(seed));
	}

	public RandomSearch(final IPathSearchInput<N, A> problem, final Random random) {
		this(problem, null, random);
	}

	public RandomSearch(final IPathSearchInput<N, A> problem, final Predicate<N> priorityPredicate, final Random random) {
		super(problem);
		N rootNode = ((ISingleRootGenerator<N>) problem.getGraphGenerator().getRootGenerator()).getRoot();
		this.gen = problem.getGraphGenerator().getSuccessorGenerator();
		this.isRandomizableSingleNodeSuccessorGenerator = this.gen instanceof ILazySuccessorGenerator && this.gen instanceof IRandomConfigurable;
		this.goalTester = problem.getGoalTester();
		this.exploredGraph.addItem(rootNode);
		this.root = new SearchGraphPath<>(rootNode);
		this.random = random;
		this.priorityPredicate = priorityPredicate;
		if (this.isRandomizableSingleNodeSuccessorGenerator) {
			((IRandomConfigurable) this.gen).setRandom(random);
		}
	}

	/**
	 * This expansion either generates all successors of a node (if the successor generator function is not able to provide single successor) or only one new successor.
	 *
	 * The node is put on CLOSED once all successors have been generated.
	 *
	 * Note that the fact that a new successor is generated does not mean that the algorithm will choose the newly generated successor to be appended to the paths.
	 *
	 * @param node
	 * @throws InterruptedException
	 * @throws AlgorithmExecutionCanceledException
	 * @throws AlgorithmTimeoutedException
	 */
	private void expandPath(final ILabeledPath<N, A> path) throws InterruptedException, AlgorithmTimeoutedException, AlgorithmExecutionCanceledException {
		synchronized (this.exploredGraph) {
			assert this.exploredGraph.isGraphSane();
			assert !this.goalTester.isGoal(path) : "Goal nodes cannot be expanded!";
			N node = path.getHead();
			assert this.exploredGraph.hasItem(node) : "Node that shall be expanded is not part of the graph: " + node;
			assert !this.closed.contains(node);
			if (this.logger.isDebugEnabled()) {
				this.logger.debug("Expanding next node with hash code {}", node.hashCode());
			}
			boolean closeNodeAfterwards = false;
			if (this.isRandomizableSingleNodeSuccessorGenerator) {

				/* generate the next successor */
				Iterator<INewNodeDescription<N, A>> iterator = this.successorGenerators.computeIfAbsent(node, ((ILazySuccessorGenerator<N, A>) this.gen)::getIterativeGenerator);
				if (!iterator.hasNext()) {
					throw new IllegalArgumentForPathExtensionException(
							"The path cannot be expanded since the head has no successors. However, it is also not marked as a goal node. Output of goal check is: " + this.goalTester.isGoal(path) + ".", path);
				}
				INewNodeDescription<N, A> successor = iterator.next();
				assert this.exploredGraph.isGraphSane();
				Objects.requireNonNull(successor, "Received null object as a successor");
				assert this.exploredGraph.hasItem(node) : "Parent node of successor is not part of the explored graph.";
				if (this.exploredGraph.getSuccessors(node).contains(successor.getTo())) {
					throw new IllegalStateException("Single node generator has generated a known successor. Generating another candidate.");
				}
				assert !this.exploredGraph.hasItem(successor.getTo()) : "Successor " + successor.getTo() + " has been reached before. Predecessors of that node are: " + this.exploredGraph.getPredecessors(successor.getTo());
				this.addNodeToLocalModel(path, successor.getTo(), successor.getArcLabel());

				/* if this was the last successor, set the close node flag to 1 */
				closeNodeAfterwards = !iterator.hasNext();
				if (closeNodeAfterwards) {
					this.successorGenerators.remove(node);
				}
			}

			/* if the successor generator cannot produce random sequences of successors, generate all successors and draw randomly from them */
			else {
				long start = System.currentTimeMillis();
				List<INewNodeDescription<N, A>> successors = this.gen.generateSuccessors(node); // could have been interrupted here
				this.logger.debug("Identified {} successor(s) in {}ms, which are now appended.", successors.size(), System.currentTimeMillis() - start);
				Collection<N> knownSuccessors = this.exploredGraph.getSuccessors(node);
				long lastTerminationCheck = 0;
				int addedSuccessors = 0;
				for (INewNodeDescription<N, A> successor : successors) {
					if (System.currentTimeMillis() - lastTerminationCheck > 100) {
						this.checkAndConductTermination();
						lastTerminationCheck = System.currentTimeMillis();
					}
					if (knownSuccessors.contains(successor.getTo())) {
						this.logger.debug("Skipping successor {}, which is already part of the model.", successor.getTo());
					} else {
						this.addNodeToLocalModel(path, successor.getTo(), successor.getArcLabel());
						addedSuccessors++;
					}
				}
				this.logger.debug("{} nodes have been added to the local model. Now checking prioritization.", addedSuccessors);

				/* if the node has successors but none of them is prioritized, remove the node from the priority list */
				if (this.prioritizedNodes.contains(node) && SetUtil.intersection(this.exploredGraph.getSuccessors(node), this.prioritizedNodes).isEmpty()) {
					this.prioritizedNodes.remove(node);
					if (this.logger.isDebugEnabled()) {
						this.logger.debug("Removed node with code {} from set of prioritized nodes.", node.hashCode());
					}
					this.updateExhaustedAndPrioritizedState(node);
				}
				closeNodeAfterwards = true;
			}

			if (closeNodeAfterwards) {

				/* if the node was not prioritized, change its state */
				if (!this.prioritizedNodes.contains(node)) {
					this.post(new NodeTypeSwitchEvent<N>(this, node, "or_closed"));
				}
				this.closed.add(node);
			}
			this.logger.debug("Finished node expansion. Sizes of explored graph and CLOSED are {} and {} respectively.", this.exploredGraph.getItems().size(), this.closed.size());
		}
	}

	private SearchGraphPath<N, A> addNodeToLocalModel(final ILabeledPath<N, A> path, final N to, final A label) {
		assert this.exploredGraph.isGraphSane();
		N from = path.getHead();
		assert from != null;
		assert to != null;
		if (this.exploredGraph.hasItem(to)) {
			throw new IllegalArgumentException("Cannot add node " + to + " to local model, because it is already contained in it.\n\tThe most probable explanation for this exception is that the underlying graph is not a tree!");
		}
		assert this.exploredGraph.hasItem(from) : "The head " + from + " of the path with " + path.getNumberOfNodes() + " nodes is not part of the explored graph! Here is the path: \n\t"
		+ path.getNodes().stream().map(Object::toString).collect(Collectors.joining("\n\t"));
		this.exploredGraph.addItem(to);
		if (this.logger.isDebugEnabled()) {
			this.logger.debug("Added node with hash code {} to graph.", to.hashCode());
		}
		assert this.exploredGraph.hasItem(to);
		assert this.exploredGraph.isGraphSane();
		boolean isPrioritized = this.priorityPredicate != null && this.priorityPredicate.test(to);
		if (isPrioritized) {
			this.prioritizedNodes.add(to);
		}
		this.exploredGraph.addEdge(from, to, label);
		SearchGraphPath<N, A> extendedPath = new SearchGraphPath<>(path, to, label);
		boolean isGoalNode = this.goalTester.isGoal(extendedPath);
		if (this.logger.isDebugEnabled()) {
			this.logger.debug("Added node {} as a successor of {} with edge label {} to the model. Contained in prioritized: {}", to.hashCode(), from.hashCode(), label, this.prioritizedNodes.contains(to));
		}
		this.post(new NodeAddedEvent<>(this, from, to, isGoalNode ? "or_solution" : (isPrioritized ? "or_prioritized" : "or_open")));
		return extendedPath;
	}

	@Override
	public IAlgorithmEvent nextWithException() throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException, AlgorithmException {
		try {
			this.registerActiveThread();
			this.logger.debug("Starting next algorithm step.");
			assert this.exploredGraph.isGraphSane();
			switch (this.getState()) {
			case CREATED:
				this.post(new GraphInitializedEvent<>(this, this.root));
				this.logger.info("Starting random search ...");
				assert this.exploredGraph.isGraphSane();
				return this.activate();

			case ACTIVE:

				/* if the root is exhausted, cancel */
				this.iterations++;
				SearchGraphPath<N, A> drawnPath = null;
				drawnPath = this.nextSolutionUnderSubPath(this.root);
				if (drawnPath == null) {
					this.logger.info("Drew NULL path, terminating");
					return this.terminate();
				}
				assert !drawnPath.getNodes().isEmpty() && this.goalTester.isGoal(drawnPath) : "The drawn path is empty or its leaf node is not a goal!";
				this.logger.info("Drew path of length {}. Posting this event. For more details on the path, enable TRACE", drawnPath.getNodes().size());
				this.logger.trace("The drawn path is {}", drawnPath);
				IAlgorithmEvent event = new GraphSearchSolutionCandidateFoundEvent<>(this, drawnPath);
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

	public void appendPathToNode(final ILabeledPath<N, A> path) {
		ILabeledPath<N, A> cPath = new SearchGraphPath<>(path.getRoot());
		for (N node : path.getNodes()) {
			if (!this.exploredGraph.getItems().contains(node)) {
				cPath = this.addNodeToLocalModel(cPath, node, path.getInArc(node));
			}
		}
	}

	/**
	 * Returns a completion of the given path so that the whole path is a goal path. The given path is then a prefix of the returned path.
	 *
	 * @param path
	 * @return
	 * @throws InterruptedException
	 * @throws AlgorithmExecutionCanceledException
	 * @throws AlgorithmTimeoutedException
	 */
	public SearchGraphPath<N, A> nextSolutionUnderSubPath(final ILabeledPath<N, A> path) throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException {
		if (this.logger.isInfoEnabled()) {
			this.logger.info("Looking for next solution under node with hash code {}. Remaining time is {}. Enable TRACE for concrete node description.", path.getHead().hashCode(), this.getRemainingTimeToDeadline());
			this.logger.trace("Description of node under which we search: {}", path.getHead());
		}
		this.checkAndConductTermination();
		assert this.exploredGraph.isGraphSane();

		/* if the root is exhausted, cancel */
		if (this.exhausted.contains(path)) {
			return null;
		}

		/* maintain two variables for prioritized search */
		boolean hasCheckedWhetherPrioritizedPathExists = false;
		boolean chasePrioritizedPath = false;

		/* conduct a random walk from the root to a goal */
		SearchGraphPath<N, A> cPath = new SearchGraphPath<>(path);
		int origLength = cPath.getNumberOfNodes();
		N head = cPath.getHead();
		int triedStepbacks = 0;
		synchronized (this.exploredGraph) {
			while (!this.goalTester.isGoal(cPath)) {
				this.checkAndConductTermination();
				assert RandomSearchUtil.checkValidityOfPathCompletion(path, cPath) : "Completion has become invalid!";
				assert this.checkThatNodeExistsInExploredGraph(head);
				assert this.exploredGraph.isGraphSane();

				/* expand node if this has not happened yet. */
				if (!this.closed.contains(head)) {
					if (this.logger.isDebugEnabled()) {
						this.logger.debug("Current head {} has not been expanded yet, expanding it now.", head.hashCode());
					}
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
					if (this.isExhausted()) {
						this.logger.debug("The graph has been exhausted.");
						return null;
					}
					if (head == path.getHead()) { // do not go above the given path
						return null;
					}
					cPath = cPath.getPathToParentOfHead();
					assert RandomSearchUtil.checkValidityOfPathCompletion(path, cPath) : "Completion has become invalid!";
					head = cPath.getHead();
					this.logger.debug("Reset head due to dead-end to parent. New head: {}.", head);
					continue;
				}

				/* if at least one of the successors is prioritized, choose one of those; otherwise choose one at random */
				assert SetUtil.intersection(this.exhausted, this.prioritizedNodes).isEmpty() : "There are nodes that are both exhausted and prioritized, which must not be the case:"
				+ SetUtil.intersection(this.exhausted, this.prioritizedNodes).stream().map(n -> "\n\t" + n).collect(Collectors.joining());
				Collection<N> prioritizedSuccessors = SetUtil.intersection(successors, this.prioritizedNodes);
				if (this.logger.isDebugEnabled()) {
					this.logger.debug("Number of prioritized successors of node {} is {}", head.hashCode(), prioritizedSuccessors.size());
				}
				N lastHead = head;
				if (!prioritizedSuccessors.isEmpty()) {
					head = prioritizedSuccessors.iterator().next();
					this.logger.debug("Following arc {} to prioritized node {}", this.exploredGraph.getEdgeLabel(lastHead, head), head);

					/* we now know that there exist prioritizes paths. That means that we also want to chase them as long as possible */
					if (!hasCheckedWhetherPrioritizedPathExists) {
						hasCheckedWhetherPrioritizedPathExists = true;
						chasePrioritizedPath = true;
					}
				}

				/* otherwise, if there is no prioritized successors but there ARE prioritized nodes under this path, step back */
				else if (chasePrioritizedPath) {
					if (cPath.getNumberOfNodes() > origLength) {
						this.logger.debug("The current head is not prioritized, but we know that there are prioritized nodes we could follow. Stepping back! Current head: {}", head);
						cPath = cPath.getPathToParentOfHead();
						head = cPath.getHead();
						triedStepbacks++;
						if (triedStepbacks > 50) {
							chasePrioritizedPath = false;
						}
						continue;
					} else {
						this.logger.debug(
								"The current head is not prioritized, and we throught that there should be more prioritized nodes. But we have reached the root and hence know that there are none. Hence, we change the flag and now follow unprioritized nodes!");
						chasePrioritizedPath = false;
						continue; // this will make the RandomSearch try the same node again (now not chasing prioritized nodes anymore)
					}
				}

				else {
					int n = successors.size();
					assert n != 0 : "Ended up in a situation where only exhausted nodes can be chosen.";
					int k = this.random.nextInt(n);
					head = successors.get(k);
					final N tmpHead = head; // needed for stream in assertion
					assert !cPath.containsNode(head) : "Going in circles ... " + cPath.getNodes().stream().map(pn -> "\n\t[" + (pn.equals(tmpHead) ? "*" : " ") + "]" + pn.toString()).collect(Collectors.joining()) + "\n\t[*]" + head;
					this.logger.trace("Selected {} as new head.", head);
					assert this.checkThatNodeExistsInExploredGraph(head);
				}
				cPath.extend(head, this.exploredGraph.getEdgeLabel(lastHead, head));
			}
		}
		assert RandomSearchUtil.checkValidityOfPathCompletion(path, cPath);

		/* propagate exhausted state */
		this.logger.trace("Head node {} has been exhausted.", head);
		this.exhausted.add(head);
		this.prioritizedNodes.remove(head);
		this.updateExhaustedAndPrioritizedState(head);
		if (this.logger.isDebugEnabled()) {
			this.logger.debug("Returning next solution path. Hash code is {}", cPath.hashCode());
		}
		if (cPath.getRoot() != path.getRoot()) {
			throw new IllegalStateException("Root got lost over the path!");
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
				boolean currentIsCompletelyExpanded = !this.isRandomizableSingleNodeSuccessorGenerator || !this.successorGenerators.containsKey(current) || !this.successorGenerators.get(current).hasNext();
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
					if (this.logger.isDebugEnabled()) {
						this.logger.debug("Removing node {} from set of prioritized nodes.", current.hashCode());
					}
					this.prioritizedNodes.remove(current);
					this.post(new NodeTypeSwitchEvent<N>(this, current, "or_closed"));
					int sizeAfter = this.prioritizedNodes.size();
					assert sizeAfter == sizeBefore - 1;
				}
			}
		}
	}

	public boolean isExhausted() {
		return this.exhausted.contains(this.root.getHead());
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

	public Random getRandom() {
		return this.random;
	}

	public int getIterations() {
		return this.iterations;
	}

	public void setIterations(final int iterations) {
		this.iterations = iterations;
	}
}