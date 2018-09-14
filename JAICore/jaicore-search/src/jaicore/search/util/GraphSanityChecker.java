package jaicore.search.util;

import java.util.List;
import java.util.Stack;

import jaicore.basic.algorithm.AlgorithmEvent;
import jaicore.basic.algorithm.AlgorithmFinishedEvent;
import jaicore.graphvisualizer.events.graphEvents.GraphInitializedEvent;
import jaicore.graphvisualizer.events.graphEvents.NodeReachedEvent;
import jaicore.graphvisualizer.events.graphEvents.NodeTypeSwitchEvent;
import jaicore.search.algorithms.standard.AbstractORGraphSearch;
import jaicore.search.model.probleminputs.GraphSearchInput;
import jaicore.search.model.travesaltree.Node;
import jaicore.search.model.travesaltree.NodeExpansionDescription;
import jaicore.search.structure.graphgenerator.NodeGoalTester;
import jaicore.search.structure.graphgenerator.SingleRootGenerator;

public class GraphSanityChecker<N, A> extends AbstractORGraphSearch<GraphSearchInput<N, A>, SanityCheckResult, N, A, Double, N, A> {

	private SanityCheckResult sanityCheckResult;
	private final int maxNodesToExpand;
	private boolean detectCycles = true;
	private boolean detectDeadEnds = true;

	public GraphSanityChecker(GraphSearchInput<N, A> problem, final int maxNodesToExpand) {
		super(problem);
		this.maxNodesToExpand = maxNodesToExpand;
	}

	@Override
	public void setNumCPUs(int numberOfCPUs) {

	}

	@Override
	public int getNumCPUs() {
		return 1;
	}

	@Override
	public AlgorithmEvent nextWithException() throws Exception {
		switch (getState()) {
		case created:
			return activate();
		case active: {
			int expanded = 0;
			Stack<Node<N, ?>> open = new Stack<>();
			N root = ((SingleRootGenerator<N>) getGraphGenerator().getRootGenerator()).getRoot();
			NodeGoalTester<N> goalTester = (NodeGoalTester<N>)getGraphGenerator().getGoalTester();
			open.push(new Node<>(null, root));
			postEvent(new GraphInitializedEvent<N>(root));
			while (!open.isEmpty() && expanded < maxNodesToExpand) {
				Node<N,?> node = open.pop();
				if (!node.isGoal())
					postEvent(new NodeTypeSwitchEvent<>(node, "or_closed"));
				expanded ++;
				List<NodeExpansionDescription<N, A>> successors = getGraphGenerator().getSuccessorGenerator().generateSuccessors(node.getPoint());
				if (detectDeadEnds && successors.isEmpty() && !node.isGoal()) {
					sanityCheckResult = new DeadEndDetectedResult<N>(node.getPoint());
					break;
				}
				for (NodeExpansionDescription<N, A> successor : successors) {
					if (detectCycles && node.externalPath().contains(successor.getTo())) {
						List<N> path = node.externalPath();
						path.add(successor.getTo());
						sanityCheckResult = new CycleDetectedResult<N>(path, node.getPoint());
						break;
					}
					Node<N,?> newNode = new Node<>(node, successor.getTo());
					newNode.setGoal(goalTester.isGoal(newNode.getPoint()));
					open.add(newNode);
					postEvent(new NodeReachedEvent<N>(node.getPoint(), successor.getTo(), newNode.isGoal() ? "or_solution" : "or_open"));
				}
				if (sanityCheckResult != null)
					break;
			}
			shutdown();
			AlgorithmFinishedEvent event = new AlgorithmFinishedEvent();
			postEvent(event);
			return event;
		}
		default:
			throw new IllegalStateException("Cannot do anything in state " + getState());
		}
	}

	@Override
	public SanityCheckResult getSolutionProvidedToCall() {
		return sanityCheckResult != null ? sanityCheckResult : new GraphSeemsSaneResult();
	}
}
