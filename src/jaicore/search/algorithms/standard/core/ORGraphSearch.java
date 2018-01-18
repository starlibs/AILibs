package jaicore.search.algorithms.standard.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

import jaicore.search.algorithms.interfaces.IObservableORGraphSearch;
import jaicore.search.structure.core.GraphEventBus;
import jaicore.search.structure.core.GraphGenerator;
import jaicore.search.structure.core.Node;
import jaicore.search.structure.core.NodeExpansionDescription;
import jaicore.search.structure.events.GraphInitializedEvent;
import jaicore.search.structure.events.NodeParentSwitchEvent;
import jaicore.search.structure.events.NodeReachedEvent;
import jaicore.search.structure.events.NodeRemovedEvent;
import jaicore.search.structure.events.NodeTypeSwitchEvent;
import jaicore.search.structure.graphgenerator.MultipleRootGenerator;
import jaicore.search.structure.graphgenerator.NodeGoalTester;
import jaicore.search.structure.graphgenerator.PathGoalTester;
import jaicore.search.structure.graphgenerator.RootGenerator;
import jaicore.search.structure.graphgenerator.SingleRootGenerator;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;

public class ORGraphSearch<T, A, V extends Comparable<V>>
		implements IObservableORGraphSearch<T, A, V>, Iterable<List<NodeExpansionDescription<T, A>>>, Iterator<List<NodeExpansionDescription<T, A>>> {

	private static final Logger logger = LoggerFactory.getLogger(ORGraphSearch.class);

	/* meta vars for controlling the general behavior */
	private int createdCounter;
	private int expandedCounter;
	private boolean initialized = false;
	protected boolean interrupted = false;
	protected boolean canceled = false;
	private Thread shutdownHook = new Thread(() -> {
		this.cancel();
	});

	/* communication */
	protected final GraphEventBus<Node<T, V>> graphEventBus = new GraphEventBus<>();
	protected final Map<T, Node<T, V>> ext2int = new HashMap<>();

	/* search related objects */
	protected final Queue<Node<T, V>> open = new PriorityBlockingQueue<>();
	protected final RootGenerator<T> rootGenerator;
	protected final SuccessorGenerator<T, A> successorGenerator;
	protected final boolean checkGoalPropertyOnEntirePath;
	protected final PathGoalTester<T> pathGoalTester;
	protected final NodeGoalTester<T> nodeGoalTester;
	protected final INodeEvaluator<T, V> nodeEvaluator;
	protected final Queue<List<T>> solutions = new LinkedBlockingQueue<>();
	protected final Map<Node<T,V>,Map<String,Object>> nodeAnnotations = new HashMap<>(); // for nodes effectively examined
	protected final Map<List<T>,Map<String,Object>> solutionAnnotations = new HashMap<>(); // for solutions that may have been acquired from some subroutine without really knowing all of the nodes on the path
	private final Set<T> expanded = new HashSet<>();
	private final boolean solutionReportingNodeEvaluator;
	
	protected INodeSelector<T, V> nodeSelector = open -> {
		logger.info("Select for expansion: {}", open.peek());
		return open.peek();
	};

	/**
	 * Memorize the last expansion for when it is requested
	 */
	private List<NodeExpansionDescription<T, A>> lastExpansion = new ArrayList<>();
	private ParentDiscarding parentDiscarding;

	public ORGraphSearch(GraphGenerator<T, A> graphGenerator, INodeEvaluator<T, V> pNodeEvaluator) {
		this(graphGenerator, pNodeEvaluator, ParentDiscarding.NONE);
	}

	@SuppressWarnings("unchecked")
	public ORGraphSearch(GraphGenerator<T, A> graphGenerator, INodeEvaluator<T, V> pNodeEvaluator, ParentDiscarding pd) {
		super();
		this.rootGenerator = graphGenerator.getRootGenerator();
		this.successorGenerator = graphGenerator.getSuccessorGenerator();
		checkGoalPropertyOnEntirePath = !(graphGenerator.getGoalTester() instanceof NodeGoalTester);
		if (checkGoalPropertyOnEntirePath) {
			this.nodeGoalTester = null;
			this.pathGoalTester = (PathGoalTester<T>) graphGenerator.getGoalTester();
			;
		} else {
			this.nodeGoalTester = (NodeGoalTester<T>) graphGenerator.getGoalTester();
			this.pathGoalTester = null;
		}

		/* set parent discarding */
		parentDiscarding = pd;

		/* if the node evaluator is graph dependent, communicate the generator to it */
		this.nodeEvaluator = pNodeEvaluator;
		if (pNodeEvaluator instanceof DecoratingNodeEvaluator<?, ?>) {
			DecoratingNodeEvaluator<T, V> castedEvaluator = (DecoratingNodeEvaluator<T, V>) pNodeEvaluator;
			if (castedEvaluator.isGraphDependent()) {
				logger.info("{} is a graph dependent node evaluator. Setting its graph generator now ...", castedEvaluator);
				castedEvaluator.setGenerator(graphGenerator);
			}
			if (castedEvaluator.isSolutionReporter()) {
				logger.info("{} is a solution reporter. Register the search algo in its event bus", castedEvaluator);
				castedEvaluator.getSolutionEventBus().register(this);
				solutionReportingNodeEvaluator = true;
			} else
				solutionReportingNodeEvaluator = false;
		} else {
			if (pNodeEvaluator instanceof IGraphDependentNodeEvaluator) {
				logger.info("{} is a graph dependent node evaluator. Setting its graph generator now ...", pNodeEvaluator);
				((IGraphDependentNodeEvaluator<T, A, V>) pNodeEvaluator).setGenerator(graphGenerator);
			}

			/* if the node evaluator is a solution reporter, register in his event bus */
			if (pNodeEvaluator instanceof ISolutionReportingNodeEvaluator) {
				logger.info("{} is a solution reporter. Register the search algo in its event bus", pNodeEvaluator);
				((ISolutionReportingNodeEvaluator<T, V>) pNodeEvaluator).getSolutionEventBus().register(this);
				solutionReportingNodeEvaluator = true;
			} else
				solutionReportingNodeEvaluator = false;
		}

		// /* if this is a decorator, go to the next one */
		// if (currentlyConsideredEvaluator instanceof DecoratingNodeEvaluator) {
		// logger.info("{} is decorator. Continue setup with the wrapped evaluator ...", currentlyConsideredEvaluator);
		// currentlyConsideredEvaluator = ((DecoratingNodeEvaluator<T,V>)currentlyConsideredEvaluator).getEvaluator();
		// }
		// else
		// currentlyConsideredEvaluator = null;
		// }
		// while (currentlyConsideredEvaluator != null);
		Runtime.getRuntime().addShutdownHook(shutdownHook);
	}

	/**
	 * This method setups the graph by inserting the root nodes.
	 */
	protected void initGraph() {
		if (!initialized) {
			initialized = true;
			if (rootGenerator instanceof MultipleRootGenerator) {
				Collection<Node<T, V>> roots = ((MultipleRootGenerator<T>) rootGenerator).getRoots().stream().map(n -> newNode(null, n)).collect(Collectors.toList());
				for (Node<T, V> root : roots) {
					if (labelNode(root))
						open.add(root);
					logger.info("Labeled root with {}", root.getInternalLabel());
				}
			} else {
				Node<T, V> root = newNode(null, ((SingleRootGenerator<T>) rootGenerator).getRoot());
				if (labelNode(root))
					open.add(root);
			}

			// check if the equals method is explicitly implemented.
			// Method [] methods = open.peek().getPoint().getClass().getDeclaredMethods();
			// boolean containsEquals = false;
			// for(Method m : methods)
			// if(m.getName() == "equals") {
			// containsEquals = true;
			// break;
			// }
			//
			// if(!containsEquals)
			// this.parentDiscarding = ParentDiscarding.NONE;
		}
	}

	/**
	 * Find the shortest path to a goal starting from <code>start</code>.
	 *
	 * @param start
	 *            The initial node.
	 * @return A list of nodes from the initial point to a goal, <code>null</code> if a path doesn't exist.
	 */
	public List<T> nextSolution() {

		/* do preliminary stuff: init graph (only for first call) and return unreturned solutions first */
		initGraph();
		if (!solutions.isEmpty())
			return solutions.poll();
		do {
			step();
			if (!solutions.isEmpty()) {
				return solutions.poll();
			}
		} while (!(terminates() || interrupted));
		return solutions.isEmpty() ? null : solutions.poll();
	}

	protected boolean terminates() {
		return open.isEmpty();
	}

	/**
	 * Makes a single expansion and returns solution paths.
	 * 
	 * @return The last found solution path.
	 */
	public List<NodeExpansionDescription<T, A>> nextExpansion() {
		if (!this.initialized)
			initGraph();
		else {

			if (!terminates())
				step();
			else
				return null;
		}

		return lastExpansion;

	}

	protected void step() {
		if (beforeSelection()) {
			Node<T, V> nodeToExpand = nodeSelector.selectNode(open);
			afterSelection(nodeToExpand);
			step(nodeToExpand);
		}
		if (Thread.interrupted())
			interrupted = true;
	}

	public void step(Node<T, V> nodeToExpand) {
		lastExpansion.clear();
		assert nodeToExpand == null || !expanded.contains(nodeToExpand.getPoint()) : "Node selected for expansion already has been expanded: " + nodeToExpand;
		open.remove(nodeToExpand);
		assert ext2int.containsKey(nodeToExpand.getPoint()) : "Trying to expand a node whose point is not available in the ext2int map";
		beforeExpansion(nodeToExpand);
		expandNode(nodeToExpand);
		afterExpansion(nodeToExpand);
		if (Thread.interrupted())
			interrupted = true;
	}

	private void expandNode(Node<T, V> expandedNodeInternal) {
		graphEventBus.post(new NodeTypeSwitchEvent<Node<T, V>>(expandedNodeInternal, "or_expanding"));
		logger.info("Expanding node {}", expandedNodeInternal);
		assert !expanded.contains(expandedNodeInternal.getPoint()) : "Node " + expandedNodeInternal + " expanded twice!!";
		expanded.add(expandedNodeInternal.getPoint());

		/* compute successors */
		successorGenerator.generateSuccessors(expandedNodeInternal.getPoint()).stream().forEach(successorDescription -> {
			lastExpansion.add(successorDescription);
			Node<T, V> newNode = newNode(expandedNodeInternal, successorDescription.getTo());

			/* update creation counter */
			createdCounter++;

			/* compute node label */
			boolean labelDefined = labelNode(newNode);

			/* only insert the node if the label was computed; otherwise we assume that the label computer inserts the node */
			/* TODO: Perhaps it would be better to use a FutureTask for the insertion? */
			if (!labelDefined)
				return;

			// if (!newNode.isGoal()) {
			if (!beforeInsertionIntoOpen(newNode))
				return;

			logger.info("Inserting successor {} of {} to OPEN.", newNode, expandedNodeInternal);
			assert !open.contains(newNode) && !expanded.contains(newNode.getPoint()) : "Inserted node is already in OPEN or even expanded!";
			// if(!expanded.contains(newNode.getPoint())){
			if (newNode.getInternalLabel() == null) {
				logger.warn("Not inserting node {} since its label ist missing!", newNode);
				return;
			}

			/* if we discard (either only on OPEN or on both OPEN and CLOSED) */
			boolean nodeProcessed = false;
			if (parentDiscarding != ParentDiscarding.NONE) {

				/* determine whether we already have the node AND it is worse than the one we want to insert */
				Optional<Node<T, V>> existingIdenticalNodeOnOpen = open.stream().filter(n -> n.getPoint().equals(newNode.getPoint())).findFirst();
				if (existingIdenticalNodeOnOpen.isPresent()) {
					Node<T, V> existingNode = existingIdenticalNodeOnOpen.get();
					if (newNode.compareTo(existingNode) < 0) {
						graphEventBus.post(new NodeTypeSwitchEvent<>(newNode, "or_" + (newNode.isGoal() ? "solution" : "open")));
						graphEventBus.post(new NodeRemovedEvent<>(existingNode));
					} else {
						graphEventBus.post(new NodeRemovedEvent<>(newNode));
					}
					nodeProcessed = true;
				}

				/* if parent discarding is not only for OPEN but also for CLOSE (and the node was not on OPEN), check the list of expanded nodes */
				if (parentDiscarding == ParentDiscarding.ALL && !existingIdenticalNodeOnOpen.isPresent()) {

					/* reopening, if the node is already on CLOSED */
					Optional<T> existingIdenticalNodeOnClosed = expanded.stream().filter(n -> n.equals(newNode.getPoint())).findFirst();
					if (existingIdenticalNodeOnClosed.isPresent()) {
						Node<T, V> node = ext2int.get(existingIdenticalNodeOnClosed.get());
						if (newNode.compareTo(node) < 0) {
							node.setParent(newNode.getParent());
							node.setInternalLabel(newNode.getInternalLabel());
							expanded.remove(node.getPoint());
							open.add(node);
							graphEventBus.post(new NodeParentSwitchEvent<Node<T, V>>(node, node.getParent(), newNode.getParent()));
						}
						graphEventBus.post(new NodeRemovedEvent<Node<T, V>>(newNode));
						nodeProcessed = true;
					}
				}
			}

			/* if parent discarding is turned off OR if the node was node processed by a parent discarding rule, just insert it on OPEN */
			if (!nodeProcessed) {
				open.add(newNode);
				graphEventBus.post(new NodeTypeSwitchEvent<>(newNode, "or_" + (newNode.isGoal() ? "solution" : "open")));
				createdCounter++;
			}
		});
		/* update statistics, send closed notifications, and possibly return a solution */
		expandedCounter++;
		graphEventBus.post(new NodeTypeSwitchEvent<Node<T, V>>(expandedNodeInternal, "or_closed"));

	}

	public GraphEventBus<Node<T, V>> getEventBus() {
		return graphEventBus;
	}

	protected List<T> getTraversalPath(Node<T, V> n) {
		return n.path().stream().map(p -> p.getPoint()).collect(Collectors.toList());
	}

	/**
	 * Check how many times a node was expanded.
	 *
	 * @return A counter of how many times a node was expanded.
	 */
	public int getExpandedCounter() {
		return expandedCounter;
	}

	public int getCreatedCounter() {
		return createdCounter;
	}

	public V getFValue(T node) {
		return getFValue(ext2int.get(node));
	}

	public V getFValue(Node<T, V> node) {
		return node.getInternalLabel();
	}
	
	public Object getNodeAnnotation(T node, String annotation) {
		return nodeAnnotations.containsKey(node) ? nodeAnnotations.get(node).get(annotation) : null;
	}

	public Object getAnnotationOfReturnedSolution(List<T> solution, String annotation) {
		return solutionAnnotations.get(solution).get(annotation);
	}

	public V getFOfReturnedSolution(List<T> solution) {
		@SuppressWarnings("unchecked")
		V annotation = (V)getAnnotationOfReturnedSolution(solution, "f");
		if (annotation == null) {
			throw new IllegalArgumentException(
					"There is no solution annotation for the given solution. Please check whether the solution was really produced by the algorithm. If so, please check that its annotation was added into the list of annotations before the solution itself was added to the solution set");
		}
		return annotation;
	}

	public void cancel() {
		logger.info("Search has been canceled");
		this.canceled = true;
		this.interrupted = true;
		if (nodeEvaluator instanceof ICancelableNodeEvaluator) {
			logger.info("Canceling node evaluator.");
			((ICancelableNodeEvaluator) nodeEvaluator).cancel();
		}
	}

	public boolean isInterrupted() {
		return this.interrupted;
	}

	public List<T> getCurrentPathToNode(T node) {
		return ext2int.get(node).externalPath();
	}

	public Node<T, V> getInternalRepresentationOf(T node) {
		return ext2int.get(node);
	}

	public List<Node<T, V>> getOpenSnapshot() {
		return Collections.unmodifiableList(new ArrayList<>(open));
	}

	protected Node<T, V> newNode(Node<T, V> parent, T t2) {
		assert !ext2int.containsKey(t2) : "Generating a second node object for " + t2 + " as successor of " + parent.getPoint() + " was contained as " + ext2int.get(t2).getPoint()
				+ ", but ORGraphSearch currently only supports tree search!";
		return newNode(parent, t2, null);
	}

	public INodeEvaluator<T, V> getNodeEvaluator() {
		return this.nodeEvaluator;
	}

	protected synchronized Node<T, V> newNode(Node<T, V> parent, T t2, V evaluation) {

		assert parent == null || expanded.contains(parent.getPoint()) : "Generating successors of an unexpanded node " + parent;
		assert !open.contains(parent) : "Parent node " + parent + " is still on OPEN, which must not be the case!";

		// TODOcheck if t2 in ext2int
		// if(ext2int.containsKey(t2))
		// System.out.println("Yes");

		/* create new node and check whether it is a goal */
		Node<T, V> newNode = new Node<>(parent, t2);
		if (evaluation != null)
			newNode.setInternalLabel(evaluation);
		if (checkGoalPropertyOnEntirePath ? pathGoalTester.isGoal(newNode.externalPath()) : nodeGoalTester.isGoal(t2)) {
			newNode.setGoal(true);
			List<T> solution = getTraversalPath(newNode);
			if (!solutionReportingNodeEvaluator) {
				nodeAnnotations.get(t2).put("f", newNode.getInternalLabel());
				solutions.add(solution);
			}
			// else while (!annotationsOfSolutionsReturnedByNodeEvaluator.keySet().contains(solution)) {

			// throw new IllegalStateException("The solution reporting node evaluator has not yet reported the solution we just detected: " + solution);

			// }
		}
		// TODO check if t2 is already in ext2int

		ext2int.put(t2, newNode);
		/* send events for this new node */
		if (parent == null) {
			this.graphEventBus.post(new GraphInitializedEvent<Node<T, V>>(newNode));
		} else {
			this.graphEventBus.post(new NodeReachedEvent<Node<T, V>>(parent, newNode, "or_" + (newNode.isGoal() ? "solution" : "created")));
			logger.info("Sent message for creation of node {} as a successor of {}", newNode, parent);
		}
		return newNode;
	}

	/**
	 * This method can be used to create an initial graph different from just root nodes. This can be interesting if the search is distributed and we want to search only an excerpt of the original
	 * one.
	 *
	 * @param initialNodes
	 */
	public void bootstrap(Collection<Node<T, V>> initialNodes) {

		if (initialized)
			throw new UnsupportedOperationException("Bootstrapping is only supported if the search has already been initialized.");

		/* now initialize the graph */
		initGraph();

		/* remove previous roots from open */
		open.clear();

		/* now insert new nodes, and the leaf ones in open */
		for (Node<T, V> node : initialNodes) {
			insertNodeIntoLocalGraph(node);
			open.add(getLocalVersionOfNode(node));
		}
	}

	protected void insertNodeIntoLocalGraph(Node<T, V> node) {
		Node<T, V> localVersionOfParent = null;
		List<Node<T, V>> path = node.path();
		Node<T, V> leaf = path.get(path.size() - 1);
		for (Node<T, V> nodeOnPath : path) {
			if (!ext2int.containsKey(nodeOnPath.getPoint())) {
				assert nodeOnPath.getParent() != null : "Want to insert a new node that has no parent. That must not be the case! Affected node is: " + nodeOnPath.getPoint();
				assert ext2int.containsKey(nodeOnPath.getParent().getPoint()) : "Want to insert a node whose parent is unknown locally";
				Node<T, V> newNode = newNode(localVersionOfParent, nodeOnPath.getPoint(), nodeOnPath.getInternalLabel());
				if (!newNode.isGoal() && !newNode.getPoint().equals(leaf.getPoint()))
					this.getEventBus().post(new NodeTypeSwitchEvent<Node<T, V>>(newNode, "or_closed"));
				localVersionOfParent = newNode;
			} else
				localVersionOfParent = getLocalVersionOfNode(nodeOnPath);
		}
	}

	/**
	 * This is relevant if we work with several copies of a node (usually if we need to copy the search space somewhere).
	 *
	 * @param node
	 * @return
	 */
	protected Node<T, V> getLocalVersionOfNode(Node<T, V> node) {
		return ext2int.get(node.getPoint());
	}

	/* hooks */
	protected void afterInitialization() {
	}

	protected boolean beforeSelection() {
		return true;
	}

	protected void afterSelection(Node<T, V> node) {
	}

	protected void beforeExpansion(Node<T, V> node) {
	}

	protected void afterExpansion(Node<T, V> node) {
	}

	/**
	 * Default implementation to compute the node label.
	 *
	 * This method should return true iff the label has been computed successfully. Only in this case, the node is further process by this routine.
	 *
	 * @param node
	 * @return
	 */
	protected boolean labelNode(Node<T, V> node) {
		try {
			node.setInternalLabel(nodeEvaluator.f(node));
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	protected boolean beforeInsertionIntoOpen(Node<T, V> node) {
		labelNode(node);
		return true;
	}

	@Override
	public boolean hasNext() {
		if (!initialized) {
			initGraph();
			step();
		} else
			step();
		return !this.lastExpansion.isEmpty();
	}

	@Override
	public List<NodeExpansionDescription<T, A>> next() {
		if (hasNext())
			return this.lastExpansion;
		else
			return null;
	}

	@Override
	public Iterator<List<NodeExpansionDescription<T, A>>> iterator() {
		return this;
	}

	@Subscribe
	public void receiveSolutionEvent(SolutionFoundEvent<T> solution) {
		logger.info("Received solution: {}", solution);
		if (solutionAnnotations.containsKey(solution.getSolution()))
			throw new IllegalStateException("Solution is reported for the second time already!");
		solutionAnnotations.put(solution.getSolution(), new HashMap<>());
		solutions.add(solution.getSolution());
	}
	
	@Subscribe
	public void receiveSolutionAnnotationEvent(SolutionAnnotationEvent<T,V> solution) {
		logger.info("Received solution annotation: {}", solution);
		if (!solutionAnnotations.containsKey(solution.getSolution()))
			throw new IllegalStateException("Solution annotation is reported for a solution that has not been reported previously!");
		solutionAnnotations.get(solution.getSolution()).put(solution.getAnnotationName(), solution.getAnnotationValue());
	}
	
	@Subscribe
	public void receiveNodeAnnotationEvent(NodeAnnotationEvent<T> event) {
		T nodeExt = event.getNode();
		if (!ext2int.containsKey(nodeExt))
			throw new IllegalArgumentException("Received annotation for a node I don't know!");
		Node<T,V> nodeInt = ext2int.get(nodeExt);
		if (!nodeAnnotations.containsKey(nodeInt)) {
			nodeAnnotations.put(nodeInt, new HashMap<>());
		}
		nodeAnnotations.get(nodeInt).put(event.getAnnotationName(), event.getAnnotationValue());
	}

	public INodeSelector<T, V> getNodeSelector() {
		return nodeSelector;
	}

	public void setNodeSelector(INodeSelector<T, V> nodeSelector) {
		this.nodeSelector = nodeSelector;
	}
}
