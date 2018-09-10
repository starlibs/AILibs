package jaicore.search.algorithms.standard.random;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import jaicore.basic.algorithm.AlgorithmEvent;
import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.AlgorithmFinishedEvent;
import jaicore.basic.algorithm.AlgorithmInitializedEvent;
import jaicore.basic.algorithm.AlgorithmState;
import jaicore.graph.LabeledGraph;
import jaicore.search.algorithms.standard.AbstractORGraphSearch;
import jaicore.search.algorithms.standard.bestfirst.events.GraphSearchSolutionCandidateFoundEvent;
import jaicore.search.model.other.SearchGraphPath;
import jaicore.search.model.probleminputs.GraphSearchInput;
import jaicore.search.model.travesaltree.NodeExpansionDescription;
import jaicore.search.structure.graphgenerator.NodeGoalTester;
import jaicore.search.structure.graphgenerator.SingleRootGenerator;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;

public class RandomSearch<N, A> extends AbstractORGraphSearch<GraphSearchInput<N, A>, Object, N, A, Double, N, A> {

	private final N root;
	private final SuccessorGenerator<N, A> gen;
	private final NodeGoalTester<N> goalTester;
	private final LabeledGraph<N, A> exploredGraph = new LabeledGraph<>();
	private final Set<N> closed = new HashSet<>();
	private final Set<N> exhausted = new HashSet<>();
	private final Random random;

	public RandomSearch(GraphSearchInput<N, A> problem, int seed) {
		this(problem, new Random(seed));
	}

	public RandomSearch(GraphSearchInput<N, A> problem, Random random) {
		super(problem);
		this.root = ((SingleRootGenerator<N>) problem.getGraphGenerator().getRootGenerator()).getRoot();
		this.gen = problem.getGraphGenerator().getSuccessorGenerator();
		this.goalTester = (NodeGoalTester) problem.getGraphGenerator().getGoalTester();
		exploredGraph.addItem(root);
		this.random = random;
	}

	private void expandNode(N node) {
		assert !closed.contains(node);
		try {
			for (NodeExpansionDescription<N, A> successor : gen.generateSuccessors(node)) {
				exploredGraph.addItem(successor.getTo());
				exploredGraph.addEdge(node, successor.getTo(), successor.getAction());
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		closed.add(node);
	}

	@Override
	public AlgorithmEvent nextWithException() throws Exception {

		switch (getState()) {
		case created: {
			activateTimeoutTimer("RandomSearch-Timeouter");
			switchState(AlgorithmState.active);
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

	public void appendPathToNode(List<N> nodes) {
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
				path.remove(head);
				if (path.isEmpty())
					return null;
				head = path.get(path.size() - 1);
				continue;
			}

			/* choose one of the successors */
			int n = successors.size();
			assert n != 0 : "Ended up in a situation where only exhausted nodes can be chosen.";
			int k = random.nextInt(n);
			head = successors.get(k);
			assert !path.contains(head) : "Going in circles ...";
			path.add(head);
		}

		/* propagate exhausted state */
		exhausted.add(head);
		N current = head;
		Collection<N> predecessors;
		while (!(predecessors = exploredGraph.getPredecessors(current)).isEmpty()) {
			assert predecessors.size() == 1;
			N predecessor = predecessors.iterator().next();
			boolean allChildrenExhausted = true;
			for (N successor : exploredGraph.getSuccessors(predecessor)) {
				if (!exhausted.contains(successor)) {
					allChildrenExhausted = false;
					break;
				}
			}
			if (allChildrenExhausted)
				exhausted.add(predecessor);
			current = predecessor;
		}
		return new SearchGraphPath<>(path, null);
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
