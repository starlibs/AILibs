//package jaicore.search.algorithms.standard.gbf;
//
//import java.util.Collection;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.LinkedList;
//import java.util.Map;
//import java.util.Queue;
//import java.util.Set;
//
//import jaicore.basic.PerformanceLogger;
//import jaicore.graph.Graph;
//import jaicore.graph.IGraphAlgorithm;
//import jaicore.graph.IGraphAlgorithmListener;
//import jaicore.graph.LabeledGraph;
//import jaicore.graphvisualizer.events.graphEvents.GraphInitializedEvent;
//import jaicore.graphvisualizer.events.graphEvents.NodeReachedEvent;
//import jaicore.graphvisualizer.events.graphEvents.NodeTypeSwitchEvent;
//import jaicore.search.algorithms.BestFirstSolution;
//import jaicore.search.algorithms.standard.bestfirst.model.AndNode;
//import jaicore.search.algorithms.standard.bestfirst.model.GraphEventBus;
//import jaicore.search.algorithms.standard.bestfirst.model.Node;
//import jaicore.search.algorithms.standard.bestfirst.model.OrNode;
//import jaicore.search.structure.graphgenerator.GoalTester;
//import jaicore.search.structure.graphgenerator.NodeGoalTester;
//import jaicore.search.structure.graphgenerator.SingleRootGenerator;
//import jaicore.search.structure.graphgenerator.SuccessorGenerator;
//import meka.core.A;
//
//public abstract class ANDORGraphSearch<T, A, V extends Comparable<V>> implements IGraphAlgorithm<T,A,IGraphAlgorithmListener<T, A>, GBFSolution<T, A, V>>> {
//
//	/* meta vars for controlling the general behavior */
//	private int expandedCounter;
//	private boolean initialized = false;
//	private boolean interrupted = false;
//
//	/* communication */
//	private final GraphEventBus<Node<T, V>> eventBus = new GraphEventBus<>();
//
//	/* search related objects */
//	protected final SingleRootGenerator<T> rootGenerator;
//	protected final SuccessorGenerator<T, A> successorGenerator;
//	protected final NodeGoalTester<T> goalTester;
//	protected final Map<T, Node<T, V>> ext2int = new HashMap<>();
//	private Node<T, V> root;
//	protected final LabeledGraph<Node<T, V>, A> traversalGraph = new LabeledGraph<>();
//	protected final Set<Node<T, V>> solvedNodes = new HashSet<>();
//	protected final Set<Node<T, V>> recursivelyExhaustedNodes = new HashSet<>();
//
//	/* methods that need refinement */
//	abstract protected Node<T, V> initialize();
//
//	abstract protected Graph<Node<T, V>> nextSolutionBase();
//
//	abstract protected Node<T, V> nextNode(Graph<Node<T, V>> solutionBase);
//
//	abstract protected Collection<Node<T, V>> expand(Node<T, V> expanded);
//
//	abstract protected int getNumberOfOpenNodesInSolutionBase(Graph<Node<T, V>> solutionBase);
//
//	public ANDORGraphSearch(SingleRootGenerator<T> rootGenerator, SuccessorGenerator<T, A> successorGenerator, GoalTester<T> goalTester) {
//		super();
//		this.rootGenerator = rootGenerator;
//		this.successorGenerator = successorGenerator;
//		this.goalTester = (NodeGoalTester<T>)goalTester;
//	}
//
//	/**
//	 * Find the shortest path to a goal starting from <code>start</code>.
//	 *
//	 * @param start
//	 *            The initial node.
//	 * @return A list of nodes from the initial point to a goal, <code>null</code> if a path doesn't exist.
//	 */
//	public LabeledGraph<T, A> getSolution() {
//
//		/* create initial node */
//		if (!initialized) {
//			initialized = true;
//			root = initialize();
//			this.eventBus.post(new GraphInitializedEvent<T>(root.getPoint()));
//		}
//
//		/* run actual algorithm */
//		Graph<Node<T, V>> solutionBase = nextSolutionBase();
//		Node<T, V> nodeToExpandNext = nextNode(solutionBase);
//		while (nodeToExpandNext != null && !interrupted && !Thread.interrupted()) {
//
//			simpleSolvedLabeling(root);
//			for (Node<T, V> solvedNode : solvedNodes) {
//				eventBus.post(new NodeTypeSwitchEvent<T>(solvedNode.getPoint(), (solvedNode instanceof AndNode ? "and" : "or") + "_solution"));
//			}
//
//			/* now return a solution if we found one; this can actually be done before */
//			if (solvedNodes.contains(root)) {
//				return getBestSolutionGraph();
//			}
//			
//			/* acquire next node to expand */
//			expandNode(nodeToExpandNext);
//
//			/* jump to next one */
//			solutionBase = nextSolutionBase();
//			nodeToExpandNext = nextNode(solutionBase);
//		}
//		return null;
//	}
//
//	protected void expandNode(Node<T, V> nodeToExpand) {
//
//		/* perform expand step */
//		T externalRepresentation = nodeToExpand.getPoint();
//		eventBus.post(new NodeTypeSwitchEvent<T>(externalRepresentation, "expanding"));
//		Set<Node<T, V>> knownNodes = new HashSet<>(traversalGraph.getItems());
//		PerformanceLogger.logStart("successor node computation");
//		Collection<Node<T, V>> insertedChildren = expand(nodeToExpand);
//		PerformanceLogger.logEnd("successor node computation");
//		PerformanceLogger.logStart("successor node labeling");
//		for (Node<T, V> successor : insertedChildren) {
//			String state = knownNodes.contains(successor) ? "closed" : "open";
//			eventBus.post(new NodeReachedEvent<T>(nodeToExpand.getPoint(), successor.getPoint(), (successor instanceof AndNode ? "and" : "or") + "_" + state));
//			exhaustiveSolvedLabeling(successor);
//		}
//		PerformanceLogger.logEnd("successor node labeling");
//
//		/* if this was a dead end, then add the node to the exhausted ones (relevant for efficient solved labeling) */
//		if (insertedChildren.isEmpty()) {
//			recursivelyExhaustedNodes.add(nodeToExpand);
//		}
//
//		expandedCounter++;
//		if (!solvedNodes.contains(nodeToExpand))
//			eventBus.post(new NodeTypeSwitchEvent<T>(externalRepresentation, (nodeToExpand instanceof AndNode ? "and" : "or") + "_closed"));
//	}
//
//	/**
//	 * Check how many times a node was expanded.
//	 *
//	 * @return A counter of how many times a node was expanded.
//	 */
//	public int getExpandedCounter() {
//		return expandedCounter;
//	}
//
//	public int getCreatedCounter() {
//		return traversalGraph.getItems().size();
//	}
//
//	public int getEdgesCounter() {
//		int numEdges = 0;
//		for (Node<T, V> node : traversalGraph.getItems())
//			numEdges += traversalGraph.getSuccessors(node).size();
//		return numEdges;
//	}
//
//	public void interrupt() {
//		this.interrupted = true;
//	}
//
//	protected OrNode<T, V> getOrNode(Node<T, V> parent, T ext, A edgeLabel) {
//		if (!ext2int.containsKey(ext)) {
//			OrNode<T, V> n = new OrNode<>(null, ext);
//			ext2int.put(ext, n);
//			traversalGraph.addItem(n);
//		}
//		OrNode<T, V> n = (OrNode<T, V>) ext2int.get(ext);
//		if (parent != null) {
//			if (edgeLabel == null)
//				throw new IllegalArgumentException("Edge label must not be null!");
//			traversalGraph.addEdge(parent, n, edgeLabel);
//		}
//		return n;
//	}
//
//	protected AndNode<T, V> getAndNode(Node<T, V> parent, T ext, A edgeLabel) {
//		if (!ext2int.containsKey(ext)) {
//			AndNode<T, V> n = new AndNode<>(null, ext);
//			ext2int.put(ext, n);
//			traversalGraph.addItem(n);
//		}
//		AndNode<T, V> n = (AndNode<T, V>) ext2int.get(ext);
//		if (parent != null) {
//			if (edgeLabel == null)
//				throw new IllegalArgumentException("Edge label must not be null!");
//			traversalGraph.addEdge(parent, n, edgeLabel);
//		}
//		return n;
//	}
//
//	public GraphEventBus<Node<T, V>> getEventBus() {
//		return eventBus;
//	}
//
//	protected void bottom2topLabeling(Node<T, V> n) {
//		boolean solution = simpleSolvedLabeling(n);
//		if (solution) {
//			Collection<Node<T, V>> parents = traversalGraph.getPredecessors(n);
//			for (Node<T, V> parent : parents) {
//				bottom2topLabeling(parent);
//			}
//		}
//	}
//
//	protected boolean simpleSolvedLabeling(Node<T, V> n) {
//
//		/* if node is solved, announce this */
//		if (solvedNodes.contains(n)) {
//			return true;
//		}
//
//		Collection<Node<T, V>> successors = traversalGraph.getSuccessors(n);
//		if (successors.isEmpty()) {
//			if (goalTester.isGoal(n.getPoint())) {
//				solvedNodes.add(n);
//				return true;
//			}
//			return false;
//		}
//
//		for (Node<T, V> successor : successors) {
//			if (simpleSolvedLabeling(successor)) {
//				if (n instanceof OrNode) {
//					solvedNodes.add(n);
//					return true;
//				}
//			} else if (n instanceof AndNode) {
//				return false;
//			}
//		}
//
//		/* if this is an or-node, it has not been solved; if it is an and-node, it has been solved */
//		if (n instanceof OrNode) {
//			return false;
//		} else {
//			solvedNodes.add(n);
//			return true;
//		}
//	}
//
//	protected boolean exhaustiveSolvedLabeling(Node<T, V> n) {
//
//		/* if node is solved, announce this */
//		if (solvedNodes.contains(n) && recursivelyExhaustedNodes.contains(n)) {
//			return true;
//		}
//
//		/* if there is no successor, return true if the node is a goal and false otherwise (node may not have been expanded yet) */
//		Collection<Node<T, V>> successors = traversalGraph.getSuccessors(n);
//		if (successors.isEmpty()) {
//			if (goalTester.isGoal(n.getPoint())) {
//				solvedNodes.add(n);
//				recursivelyExhaustedNodes.add(n);
//				return true;
//			}
//			return false;
//		}
//
//		/* compute solved state based on successors */
//		if (recursivelyExhaustedNodes.containsAll(successors)) {
//			recursivelyExhaustedNodes.add(n);
//		}
//		for (Node<T, V> successor : successors) {
//			boolean solution = exhaustiveSolvedLabeling(successor);
//			if (solution) {
//				if (n instanceof OrNode) {
//					solvedNodes.add(n);
//				}
//			} else if (n instanceof AndNode) {
//				return false;
//			}
//		}
//		if (n instanceof OrNode) {
//			return solvedNodes.contains(n);
//		}
//
//		/* if this is an or-node, it has not been solved; if it is an and-node, it has been solved */
//		if (n instanceof OrNode) {
//			return false;
//		} else {
//			solvedNodes.add(n);
//			return true;
//		}
//	}
//
//	protected LabeledGraph<T, A> getBestSolutionGraph() {
//		LabeledGraph<T,A> g = new LabeledGraph<>();
//		g.addItem(root.getPoint());
//		Queue<Node<T, V>> open = new LinkedList<>();
//		open.add(root);
//		while (!open.isEmpty()) {
//			Node<T, V> next = open.poll();
//			for (Node<T, V> succ : traversalGraph.getSuccessors(next)) {
//				if (next instanceof AndNode) {
//					g.addItem(succ.getPoint());
//					g.addEdge(next.getPoint(), succ.getPoint(), traversalGraph.getEdgeLabel(next, succ));
//					open.add(succ);
//				} else if (next instanceof OrNode) {
//					if (solvedNodes.contains(succ)) {
//						g.addItem(succ.getPoint());
//						g.addEdge(next.getPoint(), succ.getPoint(), traversalGraph.getEdgeLabel(next, succ));
//						open.add(succ);
//						break;
//					}
//				}
//			}
//		}
//		return g;
//	}
//
//	public LabeledGraph<T, A> getExternalTraversalGraph() {
//		return getExternalGraph(traversalGraph);
//	}
//
//	public LabeledGraph<T, A> getExternalGraph(LabeledGraph<Node<T, V>, A> internalGraph) {
//		LabeledGraph<T, A> graph = new LabeledGraph<>();
//		for (Node<T, V> n : internalGraph.getItems()) {
//			graph.addItem(n.getPoint());
//		}
//		for (Node<T, V> n1 : internalGraph.getItems()) {
//			for (Node<T, V> n2 : internalGraph.getSuccessors(n1)) {
//				graph.addEdge(n1.getPoint(), n2.getPoint(), internalGraph.getEdgeLabel(n1, n2));
//			}
//		}
//		return graph;
//	}
//
//	public Graph<T> getExternalGraph(Graph<Node<T, V>> internalGraph) {
//		Graph<T> graph = new Graph<>();
//		for (Node<T, V> n : internalGraph.getItems()) {
//			graph.addItem(n.getPoint());
//		}
//		for (Node<T, V> n1 : internalGraph.getItems()) {
//			for (Node<T, V> n2 : internalGraph.getSuccessors(n1)) {
//				graph.addEdge(n1.getPoint(), n2.getPoint());
//			}
//		}
//		return graph;
//	}
//
//	// protected Collection<LabeledGraph<Node<T, V>, A>> getAllSolutionGraphs(Node<T, V> node) {
//	// if (!solvedNodes.contains(node))
//	// return new ArrayList<>();
//	//
//	// if (completeSolutionGraphSets.containsKey(node)) {
//	// Collection<LabeledGraph<Node<T,V>,A>> solutions = completeSolutionGraphSets.get(node).stream().map(graph -> new LabeledGraph<Node<T,V>,A>(graph)).collect(Collectors.toList());
//	// assert !solutions.stream().filter(subgraph -> subgraph.getSources().size() != 1).findAny().isPresent() : "at least one subgraph has multiple roots";
//	// assert !solutions.stream().filter(subgraph -> subgraph.getSources().iterator().next() != node).findAny().isPresent() : "at least one of the " + solutions.size() + " returned subgraphs has a
//	// root that is different from the given node";
//	// return solutions;
//	// }
//	// Collection<LabeledGraph<Node<T, V>, A>> solutions = new ArrayList<>();
//	//
//	// /* for leaf nodes, insert a graph with only one node */
//	// if (traversalGraph.getSuccessors(node).isEmpty()) {
//	// LabeledGraph<Node<T, V>, A> graph = new LabeledGraph<>();
//	// graph.addItem(node);
//	// assert graph.getSources().size() == 1 : "Returning a subgraph with " + graph.getSources().size() + " roots";
//	// assert graph.getSources().iterator().next() == node : "Returning subgraph whose root is not the given node.";
//	// solutions.add(graph);
//	// }
//	//
//	// else if (node instanceof OrNode) {
//	// for (Node<T, V> successor : traversalGraph.getSuccessors(node)) {
//	// A edgeLabel = traversalGraph.getEdgeLabel(node, successor);
//	// if (edgeLabel == null) {
//	// System.err.println("The edge between " + node + " and " + successor + " has no label!");
//	// }
//	// for (LabeledGraph<Node<T, V>, A> subgraph : getAllSolutionGraphs(successor)) {
//	// assert subgraph.getSources().iterator().next() == successor : "The root of the subgraph is " + subgraph.getSources().iterator().next() + ", but the successor should be " + successor + "!";
//	// subgraph.addItem(node);
//	// subgraph.addEdge(node, successor, edgeLabel);
//	// assert subgraph.getSources().size() == 1 : "Returning subgraph with " + subgraph.getSources().size() + " roots.";
//	// assert subgraph.getSources().iterator().next() == node : "Returning subgraph whose root is not the given node.";
//	// solutions.add(subgraph);
//	// }
//	// }
//	// } else if (node instanceof AndNode) {
//	// List<Collection<LabeledGraph<Node<T, V>, A>>> subgraphsPerSuccessor = new ArrayList<>();
//	// for (Node<T, V> successor : traversalGraph.getSuccessors(node)) {
//	// Collection<LabeledGraph<Node<T, V>, A>> subgraphs = getAllSolutionGraphs(successor);
//	// assert !subgraphs.stream().filter(subgraph -> subgraph.getSources().size() != 1).findAny().isPresent() : "at least one subgraph has multiple roots";
//	// subgraphsPerSuccessor.add(subgraphs);
//	// }
//	// for (List<LabeledGraph<Node<T, V>, A>> tuple : SetUtil.cartesianProduct(subgraphsPerSuccessor)) {
//	// LabeledGraph<Node<T, V>, A> mergedGraph = new LabeledGraph<>();
//	// mergedGraph.addItem(node);
//	// for (LabeledGraph<Node<T, V>, A> subgraph : tuple) {
//	// mergedGraph.addGraph(subgraph);
//	// Node<T, V> root = subgraph.getSources().iterator().next();
//	// mergedGraph.addEdge(node, root, traversalGraph.getEdgeLabel(node, root));
//	// }
//	// assert mergedGraph.getSources().size() == 1 : "Returning subgraph with " + mergedGraph.getSources().size() + " roots.";
//	// assert mergedGraph.getSources().iterator().next() == node : "Returning subgraph whose root is not the given node.";
//	// solutions.add(mergedGraph);
//	// }
//	// }
//	//
//	// /* return the solutions */
//	// assert !solutions.stream().filter(subgraph -> subgraph.getSources().size() != 1).findAny().isPresent() : "at least one of the " + solutions.size() + " returned subgraphs has multiple roots: " +
//	// solutions.stream().filter(subgraph -> subgraph.getSources().size() != 1).findAny().get().getSources();
//	// assert !solutions.stream().filter(subgraph -> subgraph.getSources().iterator().next() != node).findAny().isPresent() : "at least one of the " + solutions.size() + " returned subgraphs has a
//	// root that is different from the given node";
//	// if (recursivelyExhaustedNodes.contains(node))
//	// completeSolutionGraphSets.put(node, solutions);
//	// return solutions;
//	// }
//
//	public Collection<Node<T, V>> getPredecessors(Node<T, V> node) {
//		return traversalGraph.getPredecessors(node);
//	}
//
//	public Collection<Node<T, V>> getSuccessors(Node<T, V> node) {
//		return traversalGraph.getSuccessors(node);
//	}
//
//	@Override
//	public void registerListener(Object listener) {
//		this.eventBus.register(listener);
//	}
//}
