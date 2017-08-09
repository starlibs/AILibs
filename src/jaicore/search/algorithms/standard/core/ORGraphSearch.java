package jaicore.search.algorithms.standard.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.search.algorithms.interfaces.IObservableORGraphSearch;
import jaicore.search.structure.core.GraphEventBus;
import jaicore.search.structure.core.GraphGenerator;
import jaicore.search.structure.core.Node;
import jaicore.search.structure.core.NodeExpansionDescription;
import jaicore.search.structure.events.GraphInitializedEvent;
import jaicore.search.structure.events.NodeReachedEvent;
import jaicore.search.structure.events.NodeTypeSwitchEvent;
import jaicore.search.structure.graphgenerator.GoalTester;
import jaicore.search.structure.graphgenerator.RootGenerator;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;

public class ORGraphSearch<T, A, V extends Comparable<V>> implements IObservableORGraphSearch<T, A, V> {

	private static final Logger logger = LoggerFactory.getLogger(ORGraphSearch.class);

	/* meta vars for controlling the general behavior */
	private int createdCounter;
	private int expandedCounter;
	private boolean initialized = false;
	private boolean started = false;
	protected boolean interrupted = false;

	/* communication */
	protected final GraphEventBus<Node<T, V>> eventBus = new GraphEventBus<>();
	protected final Map<T, Node<T, V>> ext2int = new HashMap<>();

	/* search related objects */
	protected final Queue<Node<T, V>> open = new PriorityBlockingQueue<>();
	protected final RootGenerator<T> rootGenerator;
	protected final SuccessorGenerator<T, A> successorGenerator;
	protected final GoalTester<T> goalTester;
	protected final NodeEvaluator<T, V> nodeEvaluator;
	protected final Queue<List<T>> solutions = new LinkedBlockingQueue<>();
	private final Set<T> expanded = new HashSet<>();

	@SuppressWarnings("unchecked")
	public ORGraphSearch(GraphGenerator<T, A> graphGenerator, NodeEvaluator<T, V> pNodeEvaluator) {
		super();
		this.rootGenerator = graphGenerator.getRootGenerator();
		this.successorGenerator = graphGenerator.getSuccessorGenerator();
		this.goalTester = graphGenerator.getGoalTester();
		this.nodeEvaluator = pNodeEvaluator;

		/* if the node evaluator is graph dependent, communicate the generator to it */
		if (pNodeEvaluator instanceof GraphDependentNodeEvaluator)
			((GraphDependentNodeEvaluator<T, A, V>) pNodeEvaluator).setGenerator(graphGenerator);
	}

	/**
	 * This method setups the graph by inserting the root nodes.
	 */
	protected void initGraph() {
		if (!initialized) {
			initialized = true;
			Collection<Node<T,V>> roots = rootGenerator.getRoots().stream().map(n -> newNode(null, n)).collect(Collectors.toList());
			for (Node<T,V> root : roots) {
				labelNode(root);
				logger.info("Labeled root with {}", root.getInternalLabel());
			}
			open.addAll(roots);
			createdCounter = open.size();
			afterInitialization();
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
		runMainLoop(true);
		return solutions.isEmpty() ? null : solutions.poll();
	}

	protected boolean terminates() {
		return open.isEmpty();
	}

	protected void runMainLoop(boolean exitOnSolution) {
		Node<T, V> nodeToExpand;
		int iteration = 0;
		long start = System.currentTimeMillis(), end;
		do {
			iteration ++;
			if (iteration % 100 == 1) {
				start = System.currentTimeMillis();
			}
			beforeSelection();
			nodeToExpand = nextNode();
			assert nodeToExpand == null || !expanded.contains(nodeToExpand.getPoint()) : "Node selected for expansion already has been expanded: " + nodeToExpand;
			if (nodeToExpand != null) {
				afterSelection(nodeToExpand);
				assert ext2int.containsKey(nodeToExpand.getPoint()) : "Trying to expand a node whose point is not available in the ext2int map";
				beforeExpansion(nodeToExpand);
				expandNode(nodeToExpand);
				afterExpansion(nodeToExpand);
				if (exitOnSolution && !solutions.isEmpty()) {
					return;
				}
			}
			if (Thread.interrupted())
				interrupted = true;
			if (iteration % 100 == 0) {
				end = System.currentTimeMillis();
				System.out.println(end - start);
			}
		} while (!(terminates() || interrupted));
		System.out.println(interrupted);
	}

	private void expandNode(Node<T, V> expandedNodeInternal) {
		eventBus.post(new NodeTypeSwitchEvent<Node<T, V>>(expandedNodeInternal, "or_expanding"));
		logger.info("Expanding node {}", expandedNodeInternal);
		assert !expanded.contains(expandedNodeInternal.getPoint()) : "Node " + expandedNodeInternal + " expanded twice!!";
		expanded.add(expandedNodeInternal.getPoint());

		/* compute successors */
		successorGenerator.generateSuccessors(expandedNodeInternal).stream().parallel().forEach(successorDescription -> {
			Node<T, V> newNode = newNode(expandedNodeInternal, successorDescription.getTo());

			createdCounter++;
			if (!goalTester.isGoal(newNode)) {
				if (beforeInsertionIntoOpen(newNode)) {
					logger.info("Inserting successor {} of {} to OPEN.", newNode, expandedNodeInternal);
					assert !open.contains(newNode) && !expanded.contains(newNode.getPoint()) : "Inserted node is already in OPEN or even expanded!";
					if (newNode.getInternalLabel() != null)
						open.add(newNode);
					else
						logger.warn("Not inserting node {} since its label ist missing!", newNode);
				}
			}
			else
				labelNode(newNode);
		});

		/* update statistics, send closed notifications, and possibly return a solution */
		expandedCounter++;
		eventBus.post(new NodeTypeSwitchEvent<Node<T, V>>(expandedNodeInternal, "or_closed"));
	}

	public GraphEventBus<Node<T, V>> getEventBus() {
		return eventBus;
	}

	protected Node<T, V> nextNode() {
		logger.info("Select for expansion: {}", open.peek());
		return open.poll();
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

	public V getFOfReturnedSolution(List<T> solution) {
		if (!ext2int.containsKey(solution.get(solution.size() -1)))
			return null;
		return ext2int.get(solution.get(solution.size() - 1)).getInternalLabel();
	}

	public void cancel() {
		this.interrupted = true;
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

	protected synchronized Node<T, V> newNode(Node<T, V> parent, T t2, V evaluation) {

		assert parent == null || expanded.contains(parent.getPoint()) : "Generating successors of an unexpanded node " + parent;
		assert !open.contains(parent) : "Parent node " + parent + " is still on OPEN, which must not be the case!";

		/* create new node and check whether it is a goal */
		Node<T, V> newNode = new Node<>(parent, t2);
		if (evaluation != null)
			newNode.setInternalLabel(evaluation);
		if (goalTester.isGoal(newNode)) {
			newNode.setGoal(true);
			solutions.add(getTraversalPath(newNode));
		}
		ext2int.put(t2, newNode);

		/* send events for this new node */
		if (parent == null) {
			this.eventBus.post(new GraphInitializedEvent<Node<T, V>>(newNode));
		} else {
			this.eventBus.post(new NodeReachedEvent<Node<T, V>>(parent, newNode, "or_" + (newNode.isGoal() ? "solution" : "open")));
			logger.info("Sent message for creation of node {} as a successor of {}", newNode, parent);
		}
		return newNode;
	}

	/**
	 * This method can be used to create an initial graph different from just root nodes.
	 * This can be interesting if the search is distributed and we want to search only an excerpt of the original one.
	 * 
	 * @param initialNodes
	 */
	public void bootstrap(Collection<Node<T,V>> initialNodes) {
		
		if (initialized)
			throw new UnsupportedOperationException("Bootstrapping is only supported if the search has already been initialized.");
		
		/* now initialize the graph */
		initGraph();
		
		/* remove previous roots from open */
		open.clear();
		
		/* now insert new nodes, and the leaf ones in open */
		for (Node<T,V> node : initialNodes) {
			insertNodeIntoLocalGraph(node);
			open.add(getLocalVersionOfNode(node));
		}
	}
	
	protected void insertNodeIntoLocalGraph(Node<T, V> node) {
		Node<T, V> localVersionOfParent = null;
		List<Node<T,V>> path = node.path();
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

	protected void labelNode(Node<T, V> node) {
		try {
			node.setInternalLabel(nodeEvaluator.f(node));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected boolean beforeInsertionIntoOpen(Node<T, V> node) {
		labelNode(node);
		return true;
	}
}
