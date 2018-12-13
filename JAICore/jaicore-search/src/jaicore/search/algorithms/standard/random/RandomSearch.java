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
import jaicore.search.algorithms.standard.bestfirst.events.GraphSearchSolutionCandidateFoundEvent;
import jaicore.search.model.other.SearchGraphPath;
import jaicore.search.model.probleminputs.GraphSearchInput;
import jaicore.search.model.travesaltree.NodeExpansionDescription;
import jaicore.search.structure.graphgenerator.NodeGoalTester;
import jaicore.search.structure.graphgenerator.SingleRootGenerator;
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
public class RandomSearch<N, A> extends AbstractORGraphSearch<GraphSearchInput<N, A>, Object, N, A, Double, N, A> implements ILoggingCustomizable {

	private Logger logger = LoggerFactory.getLogger(RandomSearch.class);
	private String loggerName;

	private final N root;
	private final SuccessorGenerator<N, A> gen;
	private final NodeGoalTester<N> goalTester;
	private final LabeledGraph<N, A> exploredGraph = new LabeledGraph<>();
	private final Set<N> closed = new HashSet<>();
	private final Predicate<N> priorityPredicate;
	private final Set<N> prioritizedNodes = new HashSet<>();
	private final Set<N> exhausted = new HashSet<>();
	private final Random random;

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
		this.goalTester = (NodeGoalTester<N>) problem.getGraphGenerator().getGoalTester();
		this.exploredGraph.addItem(this.root);
		this.random = random;
		this.priorityPredicate = priorityPredicate;
	}

	private void expandNode(final N node) throws InterruptedException {
		assert !this.closed.contains(node) && !this.goalTester.isGoal(node);
		this.logger.debug("Expanding next node {}", node);
		long start = System.currentTimeMillis();
		List<NodeExpansionDescription<N, A>> successors = this.gen.generateSuccessors(node); // could have been interrupted here
		this.logger.debug("Identified {} successor(s) in {}ms, which are now appended.", successors.size(), System.currentTimeMillis() - start);
		boolean atLeastOneSuccessorPrioritized = false;
		for (NodeExpansionDescription<N, A> successor : successors) {
			this.exploredGraph.addItem(successor.getTo());
			boolean isPrioritized = this.priorityPredicate != null && this.priorityPredicate.test(successor.getTo());
			if (isPrioritized) {
				atLeastOneSuccessorPrioritized = true;
				this.prioritizedNodes.add(successor.getTo());
			}
			this.exploredGraph.addEdge(node, successor.getTo(), successor.getAction());
			boolean isGoalNode = this.goalTester.isGoal(successor.getTo());
			if (isGoalNode) {
				this.logger.debug("Found goal node {}!", successor);
			}
			this.post(new NodeReachedEvent<>(successor.getFrom(), successor.getTo(), isGoalNode ? "or_solution" : (isPrioritized ? "or_prioritized" : "or_open")));
		}
		if (!successors.isEmpty() && this.prioritizedNodes.contains(node) && !atLeastOneSuccessorPrioritized) {
			this.prioritizedNodes.remove(node);
			this.updateExhaustedAndPrioritizedState(node);
		}
		if (!this.prioritizedNodes.contains(node)) {
			this.post(new NodeTypeSwitchEvent<N>(node, "or_closed"));
		}
		this.closed.add(node);
		this.logger.debug("Finished node expansion. Sizes of explored graph and CLOSED are {} and {} respectively.", this.exploredGraph.getItems().size(), this.closed.size());
	}

	@Override
	public AlgorithmEvent nextWithException() throws Exception {

		switch (this.getState()) {
		case created: {
			this.activateTimeoutTimer("RandomSearch-Timeouter");
			this.switchState(AlgorithmState.active);
			this.post(new GraphInitializedEvent<>(this.root));
			AlgorithmEvent event = new AlgorithmInitializedEvent();
			this.post(event);
			this.logger.info("Starting random search ...");
			return event;
		}
		case active: {

			/* if the root is exhausted, cancel */
			SearchGraphPath<N, A> drawnPath = null;
			try {
				drawnPath = this.nextSolutionUnderNode(this.root);
			} catch (TimeoutException e) {

			}
			if (drawnPath == null) {
				this.shutdown();
				AlgorithmEvent event = new AlgorithmFinishedEvent();
				this.post(event);
				return event;
			}
			AlgorithmEvent event = new GraphSearchSolutionCandidateFoundEvent<>(drawnPath);
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
		return this.exploredGraph.getItems().contains(node);
	}

	public void appendPathToNode(final List<N> nodes) throws InterruptedException {
		for (N node : nodes) {
			if (!this.closed.contains(node)) {
				this.expandNode(node);
			}
		}
	}

	public SearchGraphPath<N, A> nextSolutionUnderNode(final N node) throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException {
		this.logger.info("Looking for next solution under node {}", node);
		this.checkTermination();

		/* if the root is exhausted, cancel */
		if (this.exhausted.contains(node)) {
			return null;
		}

		/* conduct a random walk from the root to a goal */
		List<N> path = new ArrayList<>();
		path.add(node);
		N head = node;
		while (!this.goalTester.isGoal(head)) {

			this.checkTermination();

			/* expand node if this has not happened yet */
			if (!this.closed.contains(head)) {
				this.expandNode(head);
			}

			/* get unexhausted successors */
			List<N> successors = this.exploredGraph.getSuccessors(head).stream().filter(n -> !this.exhausted.contains(n)).collect(Collectors.toList());

			/* if we are in a dead end, mark the node as exhausted and remove the head again */
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

			/* if at least one of the successors is prioritized, choose one of those; otherwise choose one at random */
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

		/* propagate exhausted state */
		this.exhausted.add(head);
		this.prioritizedNodes.remove(head);
		this.updateExhaustedAndPrioritizedState(head);
		return new SearchGraphPath<>(path, null);
	}

	private void updateExhaustedAndPrioritizedState(final N node) {
		N current = node;
		Collection<N> predecessors;
		while (!(predecessors = this.exploredGraph.getPredecessors(current)).isEmpty()) {
			assert predecessors.size() == 1;
			current = predecessors.iterator().next();
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
				this.exhausted.add(current);
			}
			if (currentIsPrioritized && allPrioritizedChildrenExhausted) {
				int sizeBefore = this.prioritizedNodes.size();
				this.prioritizedNodes.remove(current);
				this.post(new NodeTypeSwitchEvent<N>(current, "or_closed"));
				int sizeAfter = this.prioritizedNodes.size();
				assert sizeAfter == sizeBefore - 1;
			}
		}
	}

	@Override
	public Object getSolutionProvidedToCall() {
		return null;
	}

	@Override
	public String getLoggerName() {
		return this.loggerName;
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger.info("Switching logger from {} to {}", this.logger.getName(), name);
		this.logger = LoggerFactory.getLogger(name);
		this.logger.info("Activated logger {} with name {}", name, this.logger.getName());
		if (this.goalTester instanceof ILoggingCustomizable) {
			((ILoggingCustomizable) this.goalTester).setLoggerName(name + ".goaltester");
		}
		if (this.gen instanceof ILoggingCustomizable) {
			((ILoggingCustomizable) this.gen).setLoggerName(name + ".generator");
		}
		super.setLoggerName(this.loggerName + "._orgraphsearch");
	}

}
