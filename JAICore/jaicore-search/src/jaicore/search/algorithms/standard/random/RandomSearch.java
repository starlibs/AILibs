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
public class RandomSearch<N, A> extends AbstractORGraphSearch<GraphSearchInput<N, A>, Object, N, A, Double, N, A> {

	private final Logger logger = LoggerFactory.getLogger(RandomSearch.class);

	private final N root;
	private final SuccessorGenerator<N, A> gen;
	private final NodeGoalTester<N> goalTester;
	private final LabeledGraph<N, A> exploredGraph = new LabeledGraph<>();
	private final Set<N> closed = new HashSet<>();
	private final Predicate<N> priorityPredicate;
	private final Set<N> prioritizedNodes = new HashSet<>();
	private final Set<N> exhausted = new HashSet<>();
	private final Random random;

	public RandomSearch(GraphSearchInput<N, A> problem, int seed) {
		this(problem, new Random(seed));
	}

	public RandomSearch(GraphSearchInput<N, A> problem, Random random) {
		this(problem, null, random);
	}

	public RandomSearch(GraphSearchInput<N, A> problem, Predicate<N> priorityPredicate, Random random) {
		super(problem);
		this.root = ((SingleRootGenerator<N>) problem.getGraphGenerator().getRootGenerator()).getRoot();
		this.gen = problem.getGraphGenerator().getSuccessorGenerator();
		this.goalTester = (NodeGoalTester<N>) problem.getGraphGenerator().getGoalTester();
		exploredGraph.addItem(root);
		this.random = random;
		this.priorityPredicate = priorityPredicate;
	}

	private void expandNode(N node) throws InterruptedException {
		assert !closed.contains(node) && !goalTester.isGoal(node);
		logger.info("Expanding next node {}", node);
		List<NodeExpansionDescription<N, A>> successors = gen.generateSuccessors(node); // could have been interrupted here
		logger.info("Identified {} successor(s), which are now appended.", successors.size());
		boolean atLeastOneSuccessorPrioritized = false;
		for (NodeExpansionDescription<N, A> successor : successors) {
			exploredGraph.addItem(successor.getTo());
			boolean isPrioritized = priorityPredicate != null && priorityPredicate.test(successor.getTo());
			if (isPrioritized) {
				atLeastOneSuccessorPrioritized = true;
				prioritizedNodes.add(successor.getTo());
			}
			exploredGraph.addEdge(node, successor.getTo(), successor.getAction());
			boolean isGoalNode = goalTester.isGoal(successor.getTo());
			if (isGoalNode)
				logger.info("Found goal node {}!", successor);
			postEvent(new NodeReachedEvent<>(successor.getFrom(), successor.getTo(), isGoalNode ? "or_solution" : (isPrioritized ? "or_prioritized" : "or_open")));
		}
		if (!successors.isEmpty() && prioritizedNodes.contains(node) && !atLeastOneSuccessorPrioritized) {
			prioritizedNodes.remove(node);
			updateExhaustedAndPrioritizedState(node);
		}
		if (!prioritizedNodes.contains(node))
			postEvent(new NodeTypeSwitchEvent<N>(node, "or_closed"));
		closed.add(node);
	}

	@Override
	public AlgorithmEvent nextWithException() throws Exception {

		switch (getState()) {
		case created: {
			activateTimeoutTimer("RandomSearch-Timeouter");
			switchState(AlgorithmState.active);
			postEvent(new GraphInitializedEvent<>(root));
			AlgorithmEvent event = new AlgorithmInitializedEvent();
			postEvent(event);
			return event;
		}
		case active: {

			/* if the root is exhausted, cancel */
			SearchGraphPath<N, A> drawnPath = null;
			try {
				drawnPath = nextSolutionUnderNode(root);
			} catch (TimeoutException e) {

			}
			if (drawnPath == null) {
				shutdown();
				AlgorithmEvent event = new AlgorithmFinishedEvent();
				postEvent(event);
				return event;
			}
			AlgorithmEvent event = new GraphSearchSolutionCandidateFoundEvent<>(drawnPath);
			postEvent(event);
			return event;
		}
		default: {
			throw new IllegalStateException("Cannot do anything in state " + getState());
		}
		}
	}

	public boolean knowsNode(N node) {
		return exploredGraph.getItems().contains(node);
	}

	public void appendPathToNode(List<N> nodes) throws InterruptedException {
		for (N node : nodes) {
			if (!closed.contains(node)) {
				expandNode(node);
			}
		}
	}

	public SearchGraphPath<N, A> nextSolutionUnderNode(N node) throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException {

		checkTermination();

		/* if the root is exhausted, cancel */
		if (exhausted.contains(node)) {
			return null;
		}

		/* conduct a random walk from the root to a goal */
		List<N> path = new ArrayList<>();
		path.add(node);
		N head = node;
		while (!goalTester.isGoal(head)) {

			checkTermination();

			/* expand node if this has not happened yet */
			if (!closed.contains(head)) {
				expandNode(head);
			}

			/* get unexhausted successors */
			List<N> successors = exploredGraph.getSuccessors(head).stream().filter(n -> !exhausted.contains(n)).collect(Collectors.toList());

			/* if we are in a dead end, mark the node as exhausted and remove the head again */
			if (successors.isEmpty()) {
				exhausted.add(head);
				prioritizedNodes.remove(head); // remove prioritized node from list if it is in
				path.remove(head);
				if (path.isEmpty())
					return null;
				head = path.get(path.size() - 1);
				continue;
			}

			/* if at least one of the successors is prioritized, choose one of those; otherwise choose one at random */
			assert SetUtil.intersection(exhausted, prioritizedNodes).isEmpty() : "There are nodes that are both exhausted and prioritized, which must not be the case:" + SetUtil.intersection(exhausted, prioritizedNodes).stream().map(n -> "\n\t" + n).collect(Collectors.joining());
			Collection<N> prioritizedSuccessors = SetUtil.intersection(successors, prioritizedNodes);
			if (!prioritizedSuccessors.isEmpty()) {
				head = prioritizedSuccessors.iterator().next();
			}
			else {
				int n = successors.size();
				assert n != 0 : "Ended up in a situation where only exhausted nodes can be chosen.";
				int k = random.nextInt(n);
				head = successors.get(k);
				final N tmpHead = head; // needed for stream in assertion
				assert !path.contains(head) : "Going in circles ... " + path.stream().map(pn -> "\n\t[" + (pn.equals(tmpHead) ? "*" : " ") + "]" + pn.toString()).collect(Collectors.joining())
						+ "\n\t[*]" + head;
			}
			path.add(head);
		}

		/* propagate exhausted state */
		exhausted.add(head);
		prioritizedNodes.remove(head);
		updateExhaustedAndPrioritizedState(head);
		return new SearchGraphPath<>(path, null);
	}
	
	private void updateExhaustedAndPrioritizedState(N node) {
		N current = node;
		Collection<N> predecessors;
		while (!(predecessors = exploredGraph.getPredecessors(current)).isEmpty()) {
			assert predecessors.size() == 1;
			current = predecessors.iterator().next();
			boolean currentIsPrioritized = prioritizedNodes.contains(current);
			boolean allChildrenExhausted = true;
			boolean allPrioritizedChildrenExhausted = true;
			for (N successor : exploredGraph.getSuccessors(current)) {
				if (!exhausted.contains(successor)) {
					allChildrenExhausted = false;
					if (currentIsPrioritized && prioritizedNodes.contains(successor)) {
						allPrioritizedChildrenExhausted = false;
						break;
					} else if (!currentIsPrioritized)
						break;
				}
			}
			if (allChildrenExhausted)
				exhausted.add(current);
			if (currentIsPrioritized && allPrioritizedChildrenExhausted) {
				int sizeBefore = prioritizedNodes.size();
				prioritizedNodes.remove(current);
				postEvent(new NodeTypeSwitchEvent<N>(current, "or_closed"));
				int sizeAfter = prioritizedNodes.size();
				assert sizeAfter == sizeBefore - 1;
			}
		}
	}

	@Override
	public void setNumCPUs(int numberOfCPUs) {

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
