//package jaicore.search.algorithms.standard.gbf;
//
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.HashMap;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Map;
//import java.util.Queue;
//import java.util.concurrent.atomic.AtomicInteger;
//import java.util.stream.Collectors;
//
//import jaicore.basic.sets.SetUtil;
//import jaicore.graph.Graph;
//import jaicore.search.algorithms.standard.bestfirst.model.AndNode;
//import jaicore.search.algorithms.standard.bestfirst.model.GraphGenerator;
//import jaicore.search.algorithms.standard.bestfirst.model.Node;
//import jaicore.search.algorithms.standard.bestfirst.model.NodeExpansionDescription;
//import jaicore.search.algorithms.standard.bestfirst.model.NodeType;
//import jaicore.search.algorithms.standard.bestfirst.model.OrNode;
//import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
//import jaicore.search.structure.graphgenerator.SingleRootGenerator;
//
///**
// * A* algorithm implementation using the method design pattern.
// *
// * @author Felix Mohr
// */
//public class GeneralBestFirst<T, A> extends ANDORGraphSearch<T, A, Integer> {
//
//	private Node<T, Integer> root;
//	private final Queue<Node<T, Integer>> open = new LinkedList<>();
//
//	private final GeneralBestFirstEvaluationOrSelector<T> orAggregator;
//	private final GeneralBestFirstEvaluationAggregation<T> andAggregator;
//	private final INodeEvaluator<T, Integer> nodeEvaluator;
//	private final Map<Node<T, Integer>, Integer> bestValues = new HashMap<>();
//	private final Map<Node<T, Integer>, List<Node<T, Integer>>> bestOrSuccessors = new HashMap<>();
//
//	/* solution stats */
//
//	public GeneralBestFirst(GraphGenerator<T, A> graphGenerator, GeneralBestFirstEvaluationOrSelector<T> orAggregator, GeneralBestFirstEvaluationAggregation<T> andAggregator,
//			INodeEvaluator<T, Integer> nodeEvaluator) {
//		super((SingleRootGenerator<T>)graphGenerator.getRootGenerator(), graphGenerator.getSuccessorGenerator(), graphGenerator.getGoalTester());
//		this.orAggregator = orAggregator;
//		this.andAggregator = andAggregator;
//		this.nodeEvaluator = nodeEvaluator;
//	}
//
//	@Override
//	protected Node<T, Integer> initialize() {
//		root = getOrNode(null, rootGenerator.getRoot(), null);
//		open.add(root);
//		try {
//			bestValues.put(root, nodeEvaluator.f(root));
//		} catch (Throwable e) {
//			e.printStackTrace();
//		}
//		return root;
//	}
//
//	@Override
//	protected Graph<Node<T, Integer>> nextSolutionBase() {
//		return getSolutionBase(0);
//	}
//
//	protected Graph<Node<T, Integer>> getSolutionBase(int index) {
//		return getSolutionBaseUnderNode(root, new AtomicInteger(0));
//	}
//
//	protected Graph<Node<T, Integer>> getSolutionBaseUnderNode(Node<T, Integer> node, AtomicInteger index) {
//
//		/* if we reached the bottom, return a graph of size 1 */
//		Graph<Node<T, Integer>> base = new Graph<>();
//		base.addItem(node);
//		if (traversalGraph.getSuccessors(node).isEmpty()) {
//			return base;
//		}
//
//		if (node instanceof OrNode) {
//			if (bestOrSuccessors.containsKey(node)) {
//				List<Node<T, Integer>> successorRanking = bestOrSuccessors.get(node);
//				for (int i = 0; i < successorRanking.size(); i++) {
//					Graph<Node<T, Integer>> subgraph = getSolutionBaseUnderNode(successorRanking.get(i), index);
//					if (subgraph.isEmpty()) {
//						throw new IllegalStateException("Subgraph " + subgraph + " is empty");
//					}
//					boolean isSolved = solvedNodes.contains(subgraph.getRoot());
//					boolean hasOpenNodes = !SetUtil.intersection(subgraph.getSinks(), open).isEmpty();
//					if (isSolved && index.get() > 0) {
//						index.decrementAndGet();
//					} else if (isSolved || hasOpenNodes) {
//						base.addGraph(subgraph);
//						Node<T, Integer> rootOfSubgraph = subgraph.getRoot();
//						base.addEdge(node, rootOfSubgraph);
//						break;
//					}
//				}
//			} else {
//				throw new IllegalStateException("I don't know anything about the quality of successors of an internal OR-Node");
//			}
//		} else if (node instanceof AndNode) {
//			for (Node<T, Integer> successor : traversalGraph.getSuccessors(node)) {
//				Graph<Node<T, Integer>> subgraph = getSolutionBaseUnderNode(successor, index);
//				Node<T, Integer> rootOfSubgraph = subgraph.getRoot();
//				base.addGraph(subgraph);
//				base.addEdge(node, rootOfSubgraph);
//			}
//		}
//		return base;
//	}
//
//	@Override
//	protected Node<T, Integer> nextNode(Graph<Node<T, Integer>> solutionBase) {
//		Collection<Node<T, Integer>> candidates = SetUtil.intersection(solutionBase.getSinks(), open);
//		if (candidates.isEmpty()) {
//			assert !open.isEmpty() : "no open node in solution base " + solutionBase + " but there are still nodes on open!";
//			return null;
//		}
//		Node<T, Integer> n = candidates.iterator().next();
//		open.remove(n);
//		return n;
//	}
//
//	@Override
//	protected Collection<Node<T, Integer>> expand(Node<T, Integer> expanded) {
//		Collection<NodeExpansionDescription<T, A>> successorNodes = successorGenerator.generateSuccessors(expanded.getPoint());
//		Collection<Node<T, Integer>> successors = new ArrayList<>();
//		Map<Node<T, Integer>, Integer> newSuccessorsAndTheirScores = new HashMap<>();
//		for (NodeExpansionDescription<T, A> successorDescription : successorNodes) {
//
//			/* no reopening of nodes we already know */
//			T successor = successorDescription.getTo();
//			boolean isKnown = ext2int.containsKey(successor);
//			Node<T, Integer> node = null;
//			NodeType type = successorDescription.getTypeOfToNode();
//			if (type == NodeType.AND)
//				node = getAndNode(expanded, successor, successorDescription.getAction());
//			if (type == NodeType.OR)
//				node = getOrNode(expanded, successor, successorDescription.getAction());
//			if (!isKnown) {
//				int val;
//				try {
//					val = nodeEvaluator.f(node);
//					newSuccessorsAndTheirScores.put(node, val);
//				} catch (Throwable e) {
//					e.printStackTrace();
//				}
//			} else
//				successors.add(node);
//		}
//
//		/* add nodes to open */
//		for (Node<T, Integer> node : newSuccessorsAndTheirScores.keySet().stream().sorted((n1, n2) -> newSuccessorsAndTheirScores.get(n1) - newSuccessorsAndTheirScores.get(n2))
//				.collect(Collectors.toList())) {
//			bestValues.put(node, newSuccessorsAndTheirScores.get(node));
//			if (!goalTester.isGoal(node.getPoint())) {
//				open.add(node);
//			}
//			successors.add(node);
//		}
//
//		/* selective cost update of node and its parents */
//		bottom2topCostUpdate(expanded);
//		return successors;
//	}
//
//	@Override
//	protected int getNumberOfOpenNodesInSolutionBase(Graph<Node<T, Integer>> solutionBase) {
//		return SetUtil.intersection(solutionBase.getSinks(), open).size();
//	}
//
//	protected void bottom2topCostUpdate(Node<T, Integer> n) {
//		Map<Node<T, Integer>, Integer> successorValues = new HashMap<>();
//		for (Node<T, Integer> s : traversalGraph.getSuccessors(n)) {
//			if (!bestValues.containsKey(s))
//				throw new IllegalStateException("No best value known for " + s);
//			successorValues.put(s, bestValues.get(s));
//		}
//
//		/* compute aggregation and update best successor of or-nodes */
//		int newValue = Integer.MAX_VALUE;
//		if (!successorValues.isEmpty()) {
//			if (n instanceof OrNode) {
//				List<Node<T, Integer>> successorRanking = orAggregator.getSuccessorRanking(successorValues);
//				newValue = bestValues.get(successorRanking.get(0));
//				bestOrSuccessors.put(n, successorRanking);
//			} else {
//				newValue = andAggregator.aggregate(successorValues);
//			}
//		}
//
//		/* propagate this update to parents */
//		bestValues.put(n, newValue);
//		Collection<Node<T, Integer>> parents = traversalGraph.getPredecessors(n);
//		for (Node<T, Integer> parent : parents) {
//			bottom2topCostUpdate(parent);
//		}
//	}
//
//	public Integer getBestValue(Node<T, Integer> node) {
//		return bestValues.get(node);
//	}
//
//	public Integer getBestValue(T node) {
//		return getBestValue(ext2int.get(node));
//	}
//}