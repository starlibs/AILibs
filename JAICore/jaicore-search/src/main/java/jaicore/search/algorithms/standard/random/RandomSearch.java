package jaicore.search.algorithms.standard.random;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.ILoggingCustomizable;
import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.sets.SetUtil;
import jaicore.graph.Graph;
import jaicore.graphvisualizer.events.graph.GraphInitializedEvent;
import jaicore.graphvisualizer.events.graph.NodeAddedEvent;
import jaicore.graphvisualizer.events.graph.NodeTypeSwitchEvent;
import jaicore.search.algorithms.standard.bestfirst.events.GraphSearchSolutionCandidateFoundEvent;
import jaicore.search.core.interfaces.AAnyPathInORGraphSearch;
import jaicore.search.model.other.SearchGraphPath;
import jaicore.search.model.travesaltree.NodeExpansionDescription;
import jaicore.search.probleminputs.GraphSearchInput;
import jaicore.search.structure.graphgenerator.NodeGoalTester;
import jaicore.search.structure.graphgenerator.SingleRootGenerator;
import jaicore.search.structure.graphgenerator.SingleSuccessorGenerator;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;

/**
 * This search randomly draws paths from the root. At every node, each successor is chosen with the same probability except if a priority predicate is defined. A priority predicate says whether or not a node lies on a path that has
 * priority. A node only has priority until all successors that have priority are exhausted.
 *
 * @author fmohr
 *
 * @param <N>
 * @param <A>
 */
public class RandomSearch<N, A> extends AAnyPathInORGraphSearch<GraphSearchInput<N, A>, SearchGraphPath<N, A>, N, A> implements ILoggingCustomizable {

	/* logging */
	private String loggerName;
	private Logger logger = LoggerFactory.getLogger(RandomSearch.class);

	private final N root;
	private final SuccessorGenerator<N, A> gen;
	private final boolean isSingleNodeSuccessorGenerator;
	private final NodeGoalTester<N> goalTester;
	private final Graph<N> exploredGraph = new Graph<>();
	private final Set<N> closed = new HashSet<>();
	private final Predicate<N> priorityPredicate;
	private final Set<N> prioritizedNodes = new HashSet<>();
	private final Set<N> exhausted = new HashSet<>(); // the set of nodes of which all solution paths have been computed
	private final Random random;

	public RandomSearch(final GraphSearchInput<N, A> problem) {
		this(problem, 0);
	}

	public RandomSearch(final GraphSearchInput<N, A> problem, final int seed) {
		this(problem, new Random(seed));
	}

	public RandomSearch(final GraphSearchInput<N, A> problem, final Random random) {
		this(problem, null, random);
	}

	public RandomSearch(final GraphSearchInput<N, A> problem, final Predicate<N> priorityPredicate, final Random random) {
		super(problem);
		this.root = ((SingleRootGenerator<N>) problem.getGraphGenerator().getRootGenerator()).getRoot();
		this.gen = problem.getGraphGenerator().getSuccessorGenerator();
		this.isSingleNodeSuccessorGenerator = this.gen instanceof SingleSuccessorGenerator;
		this.goalTester = (NodeGoalTester<N>) problem.getGraphGenerator().getGoalTester();
		this.exploredGraph.addItem(this.root);
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
	 */
	private void expandNode(final N node) throws InterruptedException {
		synchronized (this.exploredGraph) {

			assert this.exploredGraph.hasItem(node) : "Node that shall be expanded is not part of the graph";
			assert !this.closed.contains(node) && !this.goalTester.isGoal(node);
			this.logger.debug("Expanding next node {}", node);
			boolean closeNodeAfterwards = false;
			if (this.isSingleNodeSuccessorGenerator) {

				/* generate the next successor */
				SingleSuccessorGenerator<N, A> cGen = ((SingleSuccessorGenerator<N, A>) this.gen);
				NodeExpansionDescription<N, A> successor = cGen.generateSuccessor(node, this.random.nextInt(Integer.MAX_VALUE));
				if (successor != null) {
					assert this.exploredGraph.hasItem(successor.getFrom()) : "Parent node of successor is not part of the explored graph.";
					assert !this.exploredGraph.hasItem(successor.getTo()) : "Successor " + successor.getTo() + " has been reached before.";
					this.addNodeToLocalModel(successor.getFrom(), successor.getTo());
				}

				/* if this was the last successor, set the close node flag to 1 */
				closeNodeAfterwards = cGen.allSuccessorsComputed(node);
			} else {
				long start = System.currentTimeMillis();
				List<NodeExpansionDescription<N, A>> successors = this.gen.generateSuccessors(node); // could have been interrupted here
				this.logger.debug("Identified {} successor(s) in {}ms, which are now appended.", successors.size(), System.currentTimeMillis() - start);
				boolean atLeastOneSuccessorPrioritized = false;
				for (NodeExpansionDescription<N, A> successor : successors) {
					this.addNodeToLocalModel(successor.getFrom(), successor.getTo());
				}

				/*
				 * if the node has successors but none of them is prioritized, remove the node
				 * from the priority list
				 */
				if (!this.exploredGraph.getSuccessors(node).isEmpty() && this.prioritizedNodes.contains(node) && !atLeastOneSuccessorPrioritized) {
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

	private void addNodeToLocalModel(final N from, final N to) {
		assert from != null;
		assert to != null;
		assert !this.exploredGraph.hasItem(to);
		assert this.exploredGraph.hasItem(from);
		this.exploredGraph.addItem(to);
		boolean isPrioritized = this.priorityPredicate != null && this.priorityPredicate.test(to);
		if (isPrioritized) {
			this.prioritizedNodes.add(to);
		}
		this.exploredGraph.addEdge(from, to);
		boolean isGoalNode = this.goalTester.isGoal(to);
		if (isGoalNode) {
			this.logger.debug("Found goal node {}!", to);
		}
		this.post(new NodeAddedEvent<>(this.getId(), from, to, isGoalNode ? "or_solution" : (isPrioritized ? "or_prioritized" : "or_open")));
	}

	@Override
	public AlgorithmEvent nextWithException() throws InterruptedException, AlgorithmExecutionCanceledException  {

		switch (this.getState()) {
		case created: {
			this.post(new GraphInitializedEvent<>(this.getId(), this.root));
			this.logger.info("Starting random search ...");
			return this.activate();
		}
		case active: {

			/* if the root is exhausted, cancel */
			SearchGraphPath<N, A> drawnPath = null;
			try {
				drawnPath = this.nextSolutionUnderNode(this.root);
			} catch (TimeoutException e) {
				e.printStackTrace();
			}
			if (drawnPath == null) {
				this.logger.info("Drew NULL path, terminating");
				return this.terminate();
			}
			this.logger.info("Drew path of length {}. Posting this event. For more details on the path, enable TRACE", drawnPath.getNodes().size());
			this.logger.trace("The drawn path is {}", drawnPath);
			AlgorithmEvent event = new GraphSearchSolutionCandidateFoundEvent<>(this.getId(), drawnPath);
			this.logger.info("Identified new solution ...");
			this.post(event);
			return event;
		}
		default: {
			throw new IllegalStateException("Cannot do anything in state " + this.getState());
		}
		}
	}

	public boolean knowsNode(final N node) {
		synchronized (this.exploredGraph) {
			return this.exploredGraph.getItems().contains(node);
		}
	}

	public void appendPathToNode(final List<N> nodes) throws InterruptedException {
		N parent = null;
		for (N node : nodes) {
			if (!this.exploredGraph.getItems().contains(node)) {
				this.addNodeToLocalModel(parent, node);
			}
			parent = node;
		}
	}

	public SearchGraphPath<N, A> nextSolutionUnderNode(final N node) throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException {
		this.logger.info("Looking for next solution under node {}. Remaining time is {}ms.", node, this.getRemainingTimeToDeadline());
		this.checkAndConductTermination();

		/* if the root is exhausted, cancel */
		if (this.exhausted.contains(node)) {
			return null;
		}

		/* conduct a random walk from the root to a goal */
		List<N> path = new ArrayList<>();
		path.add(node);
		N head = node;
		synchronized (this.exploredGraph) {
			while (!this.goalTester.isGoal(head)) {

				this.checkAndConductTermination();

				/* expand node if this has not happened yet. */
				if (!this.closed.contains(head)) {
					this.expandNode(head);
				}

				/* get unexhausted successors */
				List<N> successors = this.exploredGraph.getSuccessors(head).stream().filter(n -> !this.exhausted.contains(n)).collect(Collectors.toList());

				/*
				 * if we are in a dead end, mark the node as exhausted and remove the head again
				 */
				if (successors.isEmpty()) {
					this.exhausted.add(head);
					this.prioritizedNodes.remove(head); // remove prioritized node from list if it is in
					path.remove(head);
					if (path.isEmpty()) {
						return null;
					}
					head = path.get(path.size() - 1);
					continue;
				}

				/*
				 * if at least one of the successors is prioritized, choose one of those;
				 * otherwise choose one at random
				 */
				assert SetUtil.intersection(this.exhausted, this.prioritizedNodes).isEmpty() : "There are nodes that are both exhausted and prioritized, which must not be the case:"
				+ SetUtil.intersection(this.exhausted, this.prioritizedNodes).stream().map(n -> "\n\t" + n).collect(Collectors.joining());
				Collection<N> prioritizedSuccessors = SetUtil.intersection(successors, this.prioritizedNodes);
				if (!prioritizedSuccessors.isEmpty()) {
					head = prioritizedSuccessors.iterator().next();
				} else {
					int n = successors.size();
					assert n != 0 : "Ended up in a situation where only exhausted nodes can be chosen.";
					int k = this.random.nextInt(n);
					head = successors.get(k);
					final N tmpHead = head; // needed for stream in assertion
					assert !path.contains(head) : "Going in circles ... " + path.stream().map(pn -> "\n\t[" + (pn.equals(tmpHead) ? "*" : " ") + "]" + pn.toString()).collect(Collectors.joining()) + "\n\t[*]" + head;
				}
				path.add(head);
			}
		}

		/* propagate exhausted state */
		this.logger.trace("Head node {} has been exhausted.", head);
		this.exhausted.add(head);
		this.prioritizedNodes.remove(head);
		this.updateExhaustedAndPrioritizedState(head);
		return new SearchGraphPath<>(path, null);
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
				boolean currentIsCompletelyExpanded = !this.isSingleNodeSuccessorGenerator || ((SingleSuccessorGenerator<N,A>)this.gen).allSuccessorsComputed(current);
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