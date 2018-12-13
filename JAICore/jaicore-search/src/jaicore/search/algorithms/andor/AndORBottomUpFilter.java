package jaicore.search.algorithms.andor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.IObjectEvaluator;
import jaicore.basic.algorithm.AAlgorithm;
import jaicore.basic.algorithm.AlgorithmEvent;
import jaicore.basic.algorithm.AlgorithmInitializedEvent;
import jaicore.basic.algorithm.AlgorithmState;
import jaicore.basic.sets.SetUtil;
import jaicore.graph.Graph;
import jaicore.graph.IGraphAlgorithm;
import jaicore.graphvisualizer.events.graphEvents.GraphInitializedEvent;
import jaicore.graphvisualizer.events.graphEvents.NodeReachedEvent;
import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.model.travesaltree.NodeExpansionDescription;
import jaicore.search.model.travesaltree.NodeType;
import jaicore.search.structure.graphgenerator.SingleRootGenerator;

public class AndORBottomUpFilter<N, A, V extends Comparable<V>> extends AAlgorithm<GraphGenerator<N, A>, Graph<N>> implements IGraphAlgorithm<GraphGenerator<N, A>, Graph<N>, N, A> {
	
	private Logger logger = LoggerFactory.getLogger(AndORBottomUpFilter.class);

	public class InnerNodeLabel {
		N node;
		NodeType type;
		// List<List<N>> survivedGoalPaths; // ranked list of paths from node to goals
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
	private final int nodeLimit;

	public AndORBottomUpFilter(GraphGenerator<N, A> gg, IObjectEvaluator<Graph<N>, V> pEvaluator) {
		this(gg, pEvaluator, 1);
	}

	public AndORBottomUpFilter(GraphGenerator<N, A> gg, IObjectEvaluator<Graph<N>, V> pEvaluator, int andNodeLimit) {
		super(gg);
		this.evaluator = pEvaluator;
		this.nodeLimit = andNodeLimit;
	}

	@Override
	public AlgorithmEvent nextWithException() throws Exception {
		switch (getState()) {
		case created: {

			/* step 1: construct the whole graph */
			Queue<InnerNodeLabel> open = new LinkedList<>();
			InnerNodeLabel root = new InnerNodeLabel(((SingleRootGenerator<N>) getInput().getRootGenerator()).getRoot(), NodeType.AND);
			open.add(root);
			post(new GraphInitializedEvent<N>(root.node));
			graph.addItem(root);
			while (!open.isEmpty()) {
				Queue<InnerNodeLabel> newOpen = new LinkedList<>();
				open.stream().forEach(n -> {
					try {
						for (NodeExpansionDescription<N, A> descr : getInput().getSuccessorGenerator().generateSuccessors(n.node)) {
							InnerNodeLabel newNode = new InnerNodeLabel(descr.getTo(), descr.getTypeOfToNode());
							synchronized (graph) {
								graph.addItem(newNode);
								graph.addEdge(n, newNode);
							}
							newOpen.add(newNode);
							post(new NodeReachedEvent<N>(n.node, newNode.node, descr.getTypeOfToNode() == NodeType.OR ? "or" : "and"));
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
				open = newOpen;
			}

			logger.info("Size: {}", graph.getItems().size());
			setState(AlgorithmState.active);
			return new AlgorithmInitializedEvent();
		}
		case active: {

			logger.debug("timeout: {}", getTimeout());

			/* now compute best local values bottom up */
			Queue<EvaluatedGraph> bestSolutions = filterNodeSolution(graph.getRoot());
			logger.info("Number of solutions: {}", bestSolutions.size());
			bestSolutionBase = bestSolutions.poll().graph;
			return terminate();
		}
		default:
			throw new IllegalStateException("No handler defined for state " + getState());
		}

	}

	/**
	 * assumes that solution paths for children have been computed already
	 * 
	 * @throws Exception
	 */
	private Queue<EvaluatedGraph> filterNodeSolution(InnerNodeLabel node) throws Exception {

		Queue<EvaluatedGraph> filteredSolutions = new PriorityQueue<>((p1, p2) -> p2.value.compareTo(p1.value)); // a list of paths ordered by their (believed) importance in ASCENDING ORDER
																													// (unimportant first, because these are drained)
		assert !node.evaluated : "Node " + node + " is filtered for the 2nd time already!";
		node.evaluated = true;
		logger.debug("Filtering node " + node + " with " + graph.getSuccessors(node).size() + " children.");

		/* if this is a leaf node, just return itself */
		if (graph.getSuccessors(node).isEmpty()) {
			EvaluatedGraph evaluatedGraph = new EvaluatedGraph();
			Graph<N> graph = new Graph<>();
			graph.addItem(node.node);
			evaluatedGraph.graph = graph;
			evaluatedGraph.value = evaluator.evaluate(graph);
			filteredSolutions.add(evaluatedGraph);
			logger.debug("Returning solutions for leaf node " + node);
			return filteredSolutions;
		}

		/* otherwise first compute the values for all children */
		Map<InnerNodeLabel, Queue<EvaluatedGraph>> subSolutions = new HashMap<>();
		for (InnerNodeLabel child : graph.getSuccessors(node)) {
			Queue<EvaluatedGraph> filteredSolutionsUnderChild = filterNodeSolution(child);
			subSolutions.put(child, filteredSolutionsUnderChild);
		}
		logger.debug("Survived paths for children " + (node.type == NodeType.OR ? "OR" : "AND") + "-node " + node.node + ":");
		for (InnerNodeLabel child : subSolutions.keySet()) {
			logger.debug("\t#" + child);
			subSolutions.get(child).forEach(l -> logger.debug("\n\t\t" + l));
		}

		/*
		 * if this is an AND node, combine all solution paths of the children and choose
		 * the best COMBINATIONs
		 */
		if (node.type == NodeType.AND) {

			/* compute cartesian product of best subsolutions of children */
			int k = (int) Math.ceil(Math.pow(nodeLimit, 1f / subSolutions.size())); // the number of subsolution to consider for each child
			List<List<EvaluatedGraph>> subSolutionsPerChild = new ArrayList<>();
			for (InnerNodeLabel child : subSolutions.keySet()) {
				List<EvaluatedGraph> bestSubSolutionsOfThisChild = new ArrayList<>();
				logger.debug("Adding " + k + "/" + subSolutions.get(child).size() + " subsolutions of child into the cartesian product input.");
				for (int i = 0; i < k; i++)
					bestSubSolutionsOfThisChild.add(subSolutions.get(child).poll());
				subSolutionsPerChild.add(bestSubSolutionsOfThisChild);
			}
			List<List<EvaluatedGraph>> cartesianProduct = SetUtil.cartesianProduct(subSolutionsPerChild);

			/* for each such combination, build the grpah and store it */
			int n = Math.min(cartesianProduct.size(), nodeLimit);
			for (int i = 0; i < n; i++) {
				List<EvaluatedGraph> subSolutionCombination = cartesianProduct.get(i);
				EvaluatedGraph extendedSolutionBase = new EvaluatedGraph();
				extendedSolutionBase.graph = new Graph<>();
				extendedSolutionBase.graph.addItem(node.node);
				for (EvaluatedGraph subSolution : subSolutionCombination) {
					assert subSolution != null;
					extendedSolutionBase.graph.addGraph(subSolution.graph);
					extendedSolutionBase.graph.addEdge(node.node, subSolution.graph.getRoot());
				}
				extendedSolutionBase.value = evaluator.evaluate(extendedSolutionBase.graph);
				filteredSolutions.add(extendedSolutionBase);
			}
			logger.debug("Determined " + filteredSolutions.size() + " sub-solutions for AND-node with " + subSolutions.size() + " children.");
		}

		/* if this is an OR node, choose the best solution paths of the children */
		else {
			for (InnerNodeLabel child : subSolutions.keySet()) {
				for (EvaluatedGraph solutionBase : subSolutions.get(child)) {
					EvaluatedGraph extendedSolutionBase = new EvaluatedGraph();
					extendedSolutionBase.graph = new Graph<>(solutionBase.graph);
					extendedSolutionBase.graph.addItem(node.node);
					extendedSolutionBase.graph.addEdge(node.node, child.node);
					extendedSolutionBase.value = solutionBase.value;
					filteredSolutions.add(extendedSolutionBase);
					while (filteredSolutions.size() > nodeLimit)
						filteredSolutions.remove();
				}
			}
		}

		/* reverse queue */
		LinkedList<EvaluatedGraph> filteredSolutionsReordered = new LinkedList<>();
		while (!filteredSolutions.isEmpty()) {
			filteredSolutionsReordered.add(0, filteredSolutions.poll());
		}
		// logger.debug("\treturning " + filteredSolutionsReordered.size() + "
		// graph(s):");
		filteredSolutionsReordered.forEach(g -> logger.debug("\tScore " + g.value + " for " + g.graph));
		return filteredSolutionsReordered;
	}

	@Override
	public Graph<N> call() throws Exception {
		while (hasNext())
			nextWithException();
		return bestSolutionBase;
	}

}
