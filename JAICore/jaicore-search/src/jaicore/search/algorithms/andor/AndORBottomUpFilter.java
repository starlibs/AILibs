package jaicore.search.algorithms.andor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

import jaicore.basic.IObjectEvaluator;
import jaicore.basic.algorithm.AAlgorithm;
import jaicore.basic.algorithm.AlgorithmEvent;
import jaicore.basic.algorithm.AlgorithmInitializedEvent;
import jaicore.basic.algorithm.AlgorithmState;
import jaicore.graph.Graph;
import jaicore.graph.IGraphAlgorithm;
import jaicore.graphvisualizer.events.graphEvents.GraphInitializedEvent;
import jaicore.graphvisualizer.events.graphEvents.NodeReachedEvent;
import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.model.travesaltree.NodeExpansionDescription;
import jaicore.search.model.travesaltree.NodeType;
import jaicore.search.structure.graphgenerator.SingleRootGenerator;

public class AndORBottomUpFilter<N, A, V extends Comparable<V>> extends AAlgorithm<GraphGenerator<N, A>, Graph<N>>
		implements IGraphAlgorithm<GraphGenerator<N, A>, Graph<N>, N, A> {

	class InnerNodeLabel {
		N node;
		NodeType type;
//		List<List<N>> survivedGoalPaths; // ranked list of paths from node to goals
		boolean evaluated;

		public InnerNodeLabel(N node, NodeType type) {
			super();
			this.node = node;
			this.type = type;
		}
	}
	
	class EvaluatedGraph {
		Graph<N> graph;
		V value;
	}

	private final Graph<InnerNodeLabel> graph = new Graph<>();
	private final IObjectEvaluator<Graph<N>, V> evaluator; // to evaluate (sub-)solutions
	private Graph<N> bestSolutionBase;

	public AndORBottomUpFilter(GraphGenerator<N, A> gg, IObjectEvaluator<Graph<N>, V> pEvaluator) {
		super(gg);
		this.evaluator = pEvaluator;
	}

	@Override
	public AlgorithmEvent nextWithException() throws Exception {
		switch (getState()) {
		case created: {
			
			/* step 1: construct the whole graph */
			Queue<InnerNodeLabel> open = new LinkedList<>();
			InnerNodeLabel root = new InnerNodeLabel(((SingleRootGenerator<N>) getInput().getRootGenerator()).getRoot(),
					NodeType.AND);

			open.add(root);
			post(new GraphInitializedEvent<N>(root.node));
			graph.addItem(root);
			while (!open.isEmpty()) {
				InnerNodeLabel n = open.poll();
				for (NodeExpansionDescription<N, A> descr : getInput().getSuccessorGenerator()
						.generateSuccessors(n.node)) {
					InnerNodeLabel newNode = new InnerNodeLabel(descr.getTo(), descr.getTypeOfToNode());
					graph.addItem(newNode);
					graph.addEdge(n, newNode);
					open.add(newNode);
					post(new NodeReachedEvent<N>(n.node, newNode.node,
							descr.getTypeOfToNode() == NodeType.OR ? "or" : "and"));
				}
			}

			System.out.println(graph.getItems().size());
			setState(AlgorithmState.active);
			return new AlgorithmInitializedEvent();
		}
		case active: {

			/* now compute best local values bottom up */
			Queue<EvaluatedGraph> bestSolutions = filterNodeSolution(graph.getRoot());
			System.out.println(bestSolutions.size());
			bestSolutionBase = bestSolutions.poll().graph;
			return terminate();
		}
		default:
			throw new IllegalStateException("No handler defined for state " + getState());
		}

	}

	/**
	 * assumes that solution paths for children have been computed already
	 * @throws Exception 
	 */
	private Queue<EvaluatedGraph> filterNodeSolution(InnerNodeLabel node) throws Exception {
		
		Queue<EvaluatedGraph> filteredSolutions = new PriorityQueue<>((p1,p2) -> p2.value.compareTo(p1.value)); // a list of paths ordered by their (believed) importance in ASCENDING ORDER (unimportant first, because these are drained)
		assert !node.evaluated : "Node " + node + " is filtered for the 2nd time already!";
		node.evaluated = true;
		System.out.println("Filtering node " + node + " with " + graph.getSuccessors(node).size() + " children.");
		
		/* if this is a leaf node, just return itself */
		if (graph.getSuccessors(node).isEmpty()) {
			EvaluatedGraph evaluatedGraph = new EvaluatedGraph();
			Graph<N> graph = new Graph<>();
			graph.addItem(node.node);
			evaluatedGraph.graph = graph;
			evaluatedGraph.value = evaluator.evaluate(graph);
			filteredSolutions.add(evaluatedGraph);
			System.out.println("Returning solutions for leaf node " + node);
			return filteredSolutions;
		}
		
		/* otherwise first compute the values for all children */
		Map<InnerNodeLabel, Queue<EvaluatedGraph>> labels = new HashMap<>();
		for (InnerNodeLabel child : graph.getSuccessors(node)) {
			Queue<EvaluatedGraph> filteredSolutionsUnderChild = filterNodeSolution(child);
			labels.put(child, filteredSolutionsUnderChild);
		}
		System.out.println("Survived paths for children " + (node.type == NodeType.OR ? "OR" : "AND") + "-node " + node.node +":");
		for (InnerNodeLabel child : labels.keySet()) {
			System.out.print("\t#" + child);
			labels.get(child).forEach(l -> System.out.println("\n\t\t" + l));
		}
		
		/* if this is an AND node, combine all solution paths of the children and choose the best COMBINATIONs */
		if (node.type == NodeType.AND) {
			
			/* as a dummy, take the best of each child and combine it */
			EvaluatedGraph extendedSolutionBase = new EvaluatedGraph();
			extendedSolutionBase.graph = new Graph<>();
			extendedSolutionBase.graph.addItem(node.node);
			for (InnerNodeLabel child : labels.keySet()) {
				EvaluatedGraph bestSolutionBaseOfChild = labels.get(child).poll();
				extendedSolutionBase.graph.addGraph(bestSolutionBaseOfChild.graph);
				extendedSolutionBase.graph.addEdge(node.node, bestSolutionBaseOfChild.graph.getRoot());
			}
			extendedSolutionBase.value = evaluator.evaluate(extendedSolutionBase.graph);
			filteredSolutions.add(extendedSolutionBase);
		}
		
		/* if this is an OR node, choose the best solution paths of the children */
		else {
			int maxK = 2;
			for (InnerNodeLabel child : labels.keySet()) {
				for (EvaluatedGraph solutionBase : labels.get(child)) {
					EvaluatedGraph extendedSolutionBase = new EvaluatedGraph();
					extendedSolutionBase.graph = new Graph<>(solutionBase.graph);
					extendedSolutionBase.graph.addItem(node.node);
					extendedSolutionBase.graph.addEdge(node.node, child.node);
					extendedSolutionBase.value = solutionBase.value;
					filteredSolutions.add(extendedSolutionBase);
					while (filteredSolutions.size() > maxK)
						filteredSolutions.remove();
				}
			}
		}
		
		/* reverse queue */
		LinkedList<EvaluatedGraph> filteredSolutionsReordered = new LinkedList<>();
		while (!filteredSolutions.isEmpty()) {
			filteredSolutionsReordered.add(0, filteredSolutions.poll());
		}
		System.out.println("\treturning " + filteredSolutionsReordered.size() + " graph(s):");
		filteredSolutionsReordered.forEach(g -> System.out.println("\tScore " + g.value + " for " + g.graph));
		return filteredSolutionsReordered;
	}

	@Override
	public Graph<N> call() throws Exception {
		while (hasNext())
			nextWithException();
		return bestSolutionBase;
	}

}
