package jaicore.search.algorithms.standard.core;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

import jaicore.search.algorithms.interfaces.IObservableORGraphSearch;
import jaicore.search.algorithms.interfaces.solutionannotations.SolutionAnnotation;
import jaicore.search.structure.core.GraphEventBus;
import jaicore.search.structure.core.GraphGenerator;
import jaicore.search.structure.core.Node;
import jaicore.search.structure.core.NodeExpansionDescription;
import jaicore.search.structure.core.OpenCollection;
import jaicore.search.structure.core.PriorityQueueOpen;
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

public class ORGraphSearch<T, A, V extends Comparable<V>> implements IObservableORGraphSearch<T, A, V>, Iterable<List<NodeExpansionDescription<T,A>>>, Iterator<List<NodeExpansionDescription<T,A>>> {
//public class ORGraphSearch<T, A, V extends Comparable<V>> implements IObservableORGraphSearch<T, A, V>, Iterable<List<T>>, Iterator<List<T>> {

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

	/* next solution var used for iterator */
	private List<T> nextSolution = null;

	/* communication */
	protected final GraphEventBus<Node<T, V>> graphEventBus = new GraphEventBus<>();
	protected final Map<T, Node<T, V>> ext2int = new HashMap<>();

	/* search related objects */
//	protected final Queue<Node<T, V>> open = new PriorityBlockingQueue<>();
	protected OpenCollection<Node<T,V>> open;
	protected final RootGenerator<T> rootGenerator;
	protected final SuccessorGenerator<T, A> successorGenerator;
	protected final boolean checkGoalPropertyOnEntirePath;
	protected final PathGoalTester<T> pathGoalTester;
	protected final NodeGoalTester<T> nodeGoalTester;
	protected final INodeEvaluator<T, V> nodeEvaluator;
	protected final Queue<List<T>> solutions = new LinkedBlockingQueue<>();
	protected final Map<List<T>, SolutionAnnotation<T, V>> annotationsOfSolutionsReturnedByNodeEvaluator = new HashMap<>();
	private final Set<T> expanded = new HashSet<>();
	private final HashMap<T,Node<T,V>> closed = new HashMap<>();
	private final HashMap<T,Node<T,V>> openMap = new HashMap<>();
	private final boolean solutionReportingNodeEvaluator;

	private List<NodeExpansionDescription<T,A>> lastExpansion;
	private ParentDiscarding parentDiscarding;

	//TODO more Constructors
	@SuppressWarnings("unchecked")
	public ORGraphSearch(GraphGenerator<T, A> graphGenerator, INodeEvaluator<T, V> pNodeEvaluator) {
		this(graphGenerator,pNodeEvaluator, new PriorityQueueOpen<>());
	}
	
	@SuppressWarnings("unchecked")
	public ORGraphSearch(GraphGenerator<T,A> graphGenerator, INodeEvaluator<T,V> pNodeEvaluator, ParentDiscarding pd) {
		this(graphGenerator, pNodeEvaluator, new PriorityQueueOpen<>(), pd);
	}
	
	@SuppressWarnings("unchecked")
	public ORGraphSearch(GraphGenerator<T, A> graphGenerator, INodeEvaluator<T, V> pNodeEvaluator, OpenCollection<Node<T,V>> open) {
		this(graphGenerator,pNodeEvaluator,open , ParentDiscarding.NONE);
	}

	@SuppressWarnings("unchecked")
	public ORGraphSearch(GraphGenerator<T, A> graphGenerator, INodeEvaluator<T, V> pNodeEvaluator, OpenCollection<Node<T,V>> open,ParentDiscarding pd) {
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

		//init lastExpansion with an empty ArrayList
		lastExpansion = new ArrayList<>();
		
		// inti open
		this.open = open;
		//init parentDiscarding with none
		parentDiscarding = pd;

		this.nodeEvaluator = pNodeEvaluator;

		/* if the node evaluator is graph dependent, communicate the generator to it */
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
			}
			else {
				Node<T,V> root = newNode(null, ((SingleRootGenerator<T>)rootGenerator).getRoot());
				if (labelNode(root))
					open.add(root);
			}

			//check if the equals method is explicitly implemented.
			//TODO peeking?
			Method [] methods = open.peek().getPoint().getClass().getDeclaredMethods();
			boolean containsEquals = false;
			for(Method m : methods)
				if(m.getName() == "equals") {
					containsEquals = true;
					break;
				}

			if(!containsEquals)
				this.parentDiscarding = ParentDiscarding.NONE;

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
//			if(!solutions.isEmpty() && open.peek().isGoal()){
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
	 * @return
	 * 		The last found solution path.
	 */
	public List<NodeExpansionDescription<T,A>> nextExpansion() {
		if(!this.initialized)
			initGraph();
		else {

			if(!terminates())
				step();
		}

		return lastExpansion;

	}

	protected void step() {
		lastExpansion.clear();
		if (beforeSelection()) {
			Node<T, V> nodeToExpand = nextNode();
//			TODO assert warning
//			assert nodeToExpand == null || !expanded.contains(nodeToExpand.getPoint()) : "Node selected for expansion already has been expanded: " + nodeToExpand;
			if (nodeToExpand != null) {
				afterSelection(nodeToExpand);
				assert ext2int.containsKey(nodeToExpand.getPoint()) : "Trying to expand a node whose point is not available in the ext2int map";
				beforeExpansion(nodeToExpand);

				if(parentDiscarding == ParentDiscarding.ALL) {
					expandNodeWithParentDiscarding(nodeToExpand);
					closed.put(nodeToExpand.getPoint(), nodeToExpand);
					
				}
				else
					expandNode(nodeToExpand);
				afterExpansion(nodeToExpand);
			}
		}
		if (Thread.interrupted())
			interrupted = true;
	}

	private void expandNode(Node<T, V> expandedNodeInternal) {
		graphEventBus.post(new NodeTypeSwitchEvent<Node<T, V>>(expandedNodeInternal, "or_expanding"));
		logger.info("Expanding node {}", expandedNodeInternal);
//		TODO assert warning
//		assert !expanded.contains(expandedNodeInternal.getPoint()) : "Node " + expandedNodeInternal + " expanded twice!!";
		expanded.add(expandedNodeInternal.getPoint());

		/* compute successors */
		successorGenerator.generateSuccessors(expandedNodeInternal.getPoint()).stream().forEach(successorDescription -> {
			lastExpansion.add(successorDescription);
			Node<T, V> newNode = newNode(expandedNodeInternal, successorDescription.getTo());
			//TODO newNode == null;

			/* update creation counter */
			createdCounter++;

			/* compute node label */
			boolean labelDefined = labelNode(newNode);

			/* if node is not a goal node, put it on open */
			if (labelDefined) {
				if (!newNode.isGoal()) {
					if (beforeInsertionIntoOpen(newNode)) {
						logger.info("Inserting successor {} of {} to OPEN.", newNode, expandedNodeInternal);
//						assert !open.contains(newNode) && !expanded.contains(newNode.getPoint()) : "Inserted node is already in OPEN or even expanded!";
						if(!expanded.contains(newNode.getPoint())){
							if (newNode.getInternalLabel() != null) {
	
								if(parentDiscarding == ParentDiscarding.OPEN) {
	
									PriorityBlockingQueue<Node<T,V>> q = new PriorityBlockingQueue<>();
									boolean added =false;
									while(!open.isEmpty()) {
										//TODO polling
										Node<T,V> node = open.next();
										if(node.getPoint().equals(newNode.getPoint())) {
											if(node.compareTo(newNode)<1)
												q.add(node);
											else
												q.add(newNode);
											added = true;
											createdCounter --;
											break;
										}
										else
											q.add(node);
									}
									q.drainTo(open);
									if(!added)
										open.add(newNode);
								}
								else {
									open.add(newNode);
								}
								graphEventBus.post(new NodeTypeSwitchEvent<>(newNode, "or_open"));
							} else
								logger.warn("Not inserting node {} since its label ist missing!", newNode);
						}
					}
				}
			}
		});
		/* update statistics, send closed notifications, and possibly return a solution */
		expandedCounter++;
		graphEventBus.post(new NodeTypeSwitchEvent<Node<T, V>>(expandedNodeInternal, "or_closed"));
	}

	/*
	* a version of the expand Node function with additions to implement complete parentDiscarding
	*/
	private void expandNodeWithParentDiscarding(Node<T, V> expandedNodeInternal) {
		/*put the node which should get expanded on expanded*/
		logger.info("Expanding node{}", expandedNodeInternal);
		expanded.add(expandedNodeInternal.getPoint());
		
		/*compute successors*/
		successorGenerator.generateSuccessors(expandedNodeInternal.getPoint()).stream().forEach(successorDescription ->{
			lastExpansion.add(successorDescription);
			
			/*creating new node*/
			Node<T,V> newNode = newNode(expandedNodeInternal, successorDescription.getTo());
			
			/*defines the label for the newly created node*/
			boolean labelDefined = labelNode(newNode);
			boolean proccessed = false;
			
			if(labelDefined) {
				if(!newNode.isGoal()){
					/*modifing the node, if it is already on OPEN*/
					if(openMap.containsKey(newNode.getPoint())) {
						PriorityBlockingQueue<Node<T,V>> q = new PriorityBlockingQueue<>();
									
						/*extract nodes from OPEN to q until newNode was found.
						 * If newNode was found compute if it is better
						 * than the old one and put the better back on OPEN*/
						while(!open.isEmpty()) {
							//TODO polling
							Node<T,V> node = open.next();
							if(node.getPoint().equals(newNode.getPoint())) {
								if(newNode.compareTo(node)< 0) {
									q.add(newNode);
	//								graphEventBus.post(new NodeParentSwitchEvent<Node<T,V>>(node, node.getParent(), newNode.getParent()));
									graphEventBus.post(new NodeTypeSwitchEvent<>(newNode, "or_open"));
									graphEventBus.post(new NodeRemovedEvent<Node<T,V>>(node));
									openMap.put(newNode.getPoint(), newNode);
								}
								else {
									q.add(node);
									graphEventBus.post(new NodeRemovedEvent<Node<T,V>>(newNode));
								}
								break;
							}
							else
								q.add(node);
						}
						 /*reinsert q into OPEN*/
						q.drainTo(open);
						proccessed = true;
					}
					
					/*reopening, if the node is already on CLOSED */
					if(closed.containsKey(newNode.getPoint())) {
						Node<T,V> node = closed.get(newNode.getPoint());
						/*update the node, if the new one is better*/
						if(newNode.compareTo(node) < 0) {
							closed.remove(node.getPoint());
							node.setInternalLabel(newNode.getInternalLabel());
							
							graphEventBus.post(new NodeRemovedEvent<Node<T,V>>(newNode));
							graphEventBus.post(new NodeParentSwitchEvent<Node<T,V>>(node, node.getParent(), newNode.getParent()));
							node.setParent(newNode.getParent());
							open.add(node);
							openMap.put(node.getPoint(), node);
						}
						else {
							graphEventBus.post(new NodeRemovedEvent<Node<T,V>>(newNode));
						}
						proccessed = true;
					}
					
					if(!proccessed) {
						open.add(newNode);
						openMap.put(newNode.getPoint(), newNode);
						graphEventBus.post(new NodeTypeSwitchEvent<>(newNode, "or_open"));
						createdCounter ++;
					}
					
				}
				else {
					logger.warn("Not inserting node {} since its label is missing!" ,newNode);
				}
			}
		});
		
		expandedCounter ++;
		graphEventBus.post(new NodeTypeSwitchEvent<Node<T,V>>(expandedNodeInternal, "or_closed"));
	}

	public GraphEventBus<Node<T, V>> getEventBus() {
		return graphEventBus;
	}

	protected Node<T, V> nextNode() {
		//TODO peek and poll
		logger.info("Select for expansion: {}", open.peek());
		if(!openMap.isEmpty())
			openMap.remove(open.peek().getPoint());
		return open.next();
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

	public SolutionAnnotation<T, V> getAnnotationOfReturnedSolution(List<T> solution) {
		if (!annotationsOfSolutionsReturnedByNodeEvaluator.containsKey(solution))
			return null;
		else {
			return annotationsOfSolutionsReturnedByNodeEvaluator.get(solution);
		}
	}

	public V getFOfReturnedSolution(List<T> solution) {
		SolutionAnnotation<T, V> annotation = getAnnotationOfReturnedSolution(solution);
		if (annotation == null) {
			throw new IllegalArgumentException(
					"There is no solution annotation for the given solution. Please check whether the solution was really produced by the algorithm. If so, please check that its annotation was added into the list of annotations before the solution itself was added to the solution set");
		}
		return annotation.f();
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
		//TODO assert warning
//		assert !ext2int.containsKey(t2) : "Generating a second node object for " + t2 + " as successor of " + parent.getPoint() + " was contained as " + ext2int.get(t2).getPoint()
//				+ ", but ORGraphSearch currently only supports tree search!";
		return newNode(parent, t2, null);
	}

	public INodeEvaluator<T, V> getNodeEvaluator() {
		return this.nodeEvaluator;
	}

	protected synchronized Node<T, V> newNode(Node<T, V> parent, T t2, V evaluation) {

		assert parent == null || expanded.contains(parent.getPoint()) : "Generating successors of an unexpanded node " + parent;
		assert !open.contains(parent) : "Parent node " + parent + " is still on OPEN, which must not be the case!";

		//TODOcheck if t2 in ext2int
//		if(ext2int.containsKey(t2))
//			System.out.println("Yes");

		/* create new node and check whether it is a goal */
		Node<T, V> newNode = new Node<>(parent, t2);
		if (evaluation != null)
			newNode.setInternalLabel(evaluation);
		if (checkGoalPropertyOnEntirePath ? pathGoalTester.isGoal(newNode.externalPath()) : nodeGoalTester.isGoal(t2)) {
			newNode.setGoal(true);
			List<T> solution = getTraversalPath(newNode);
			if (!solutionReportingNodeEvaluator) {
				annotationsOfSolutionsReturnedByNodeEvaluator.put(solution, () -> newNode.getInternalLabel());
				solutions.add(solution);
			}
			// else while (!annotationsOfSolutionsReturnedByNodeEvaluator.keySet().contains(solution)) {

			// throw new IllegalStateException("The solution reporting node evaluator has not yet reported the solution we just detected: " + solution);

			// }
		}
		//TODO check if t2 is already in ext2int


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

//	@Override
//	public boolean hasNext() {
//		nextSolution = nextSolution();
//		return nextSolution != null;
//	}
//
//	@Override
//	public List<T> next() {
//		return nextSolution;
//	}

	@Override
	public boolean hasNext() {
		if(lastExpansion.isEmpty())
			return false;
		else
			return true;
	}

	@Override
	public List<NodeExpansionDescription<T,A>> next(){
		step();
		return lastExpansion;
	}


	@Override
	public Iterator<List<NodeExpansionDescription<T,A>>> iterator() {
		return this;
	}

	@Subscribe
	public void receiveNextSolutionFromFComputer(SolutionFoundEvent<T, V> solution) {
		logger.info("Received solution from FComputer: {}", solution);
		if (annotationsOfSolutionsReturnedByNodeEvaluator.containsKey(solution.getSolution()))
			throw new IllegalStateException("Solution is reported for the second time already!");
		annotationsOfSolutionsReturnedByNodeEvaluator.put(solution.getSolution(), solution.getAnnotation());
		solutions.add(solution.getSolution());
	}
}
