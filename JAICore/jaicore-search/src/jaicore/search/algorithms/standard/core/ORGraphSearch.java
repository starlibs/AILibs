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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

import jaicore.basic.IIterableAlgorithm;
import jaicore.basic.ILoggingCustomizable;
import jaicore.concurrent.TimeoutTimer;
import jaicore.concurrent.TimeoutTimer.TimeoutSubmitter;
import jaicore.graphvisualizer.events.graphEvents.GraphInitializedEvent;
import jaicore.graphvisualizer.events.graphEvents.NodeParentSwitchEvent;
import jaicore.graphvisualizer.events.graphEvents.NodeReachedEvent;
import jaicore.graphvisualizer.events.graphEvents.NodeRemovedEvent;
import jaicore.graphvisualizer.events.graphEvents.NodeTypeSwitchEvent;
import jaicore.logging.LoggerUtil;
import jaicore.search.algorithms.interfaces.IObservableORGraphSearch;
import jaicore.search.structure.core.GraphEventBus;
import jaicore.search.structure.core.GraphGenerator;
import jaicore.search.structure.core.Node;
import jaicore.search.structure.core.NodeExpansionDescription;
import jaicore.search.structure.core.OpenCollection;
import jaicore.search.structure.core.PriorityQueueOpen;
import jaicore.search.structure.graphgenerator.MultipleRootGenerator;
import jaicore.search.structure.graphgenerator.NodeGoalTester;
import jaicore.search.structure.graphgenerator.PathGoalTester;
import jaicore.search.structure.graphgenerator.RootGenerator;
import jaicore.search.structure.graphgenerator.SingleRootGenerator;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;

public class ORGraphSearch<T, A, V extends Comparable<V>>
		implements IObservableORGraphSearch<T, A, V>, IIterableAlgorithm<List<NodeExpansionDescription<T, A>>>, Iterator<List<NodeExpansionDescription<T, A>>>, ILoggingCustomizable {

	protected Logger logger = LoggerFactory.getLogger(ORGraphSearch.class);

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
	protected final Map<T, Node<T, V>> ext2int = new ConcurrentHashMap<>();

	/* search related objects */
	protected OpenCollection<Node<T, V>> open = new PriorityQueueOpen<>();

	protected final GraphGenerator<T, A> graphGenerator;
	protected final RootGenerator<T> rootGenerator;
	protected final SuccessorGenerator<T, A> successorGenerator;
	protected final boolean checkGoalPropertyOnEntirePath;
	protected final PathGoalTester<T> pathGoalTester;
	protected final NodeGoalTester<T> nodeGoalTester;

	/* computation of f */
	protected final INodeEvaluator<T, V> nodeEvaluator;
	private INodeEvaluator<T, V> timeoutNodeEvaluator;
	private TimeoutSubmitter timeoutSubmitter;
	private int timeoutForComputationOfF;

	protected final Queue<List<T>> solutions = new LinkedBlockingQueue<>();
	protected final Map<List<T>, Map<String, Object>> solutionAnnotations = new HashMap<>(); // for solutions that may have been acquired from some subroutine without really knowing all of the nodes
																								// on the path
	/* parallelization */
	protected int additionalThreadsForExpansion = 0;
	private Semaphore fComputationTickets;
	private ExecutorService pool;
	protected final AtomicInteger activeJobs = new AtomicInteger(0);

	protected final Set<T> expanded = new HashSet<>();
	private final boolean solutionReportingNodeEvaluator;

	/**
	 * Memorize the last expansion for when it is requested
	 */
	private List<NodeExpansionDescription<T, A>> lastExpansion = new ArrayList<>();
	private ParentDiscarding parentDiscarding;

	private class NodeBuilder implements Runnable {

		private final Node<T, V> expandedNodeInternal;
		private final NodeExpansionDescription<T, A> successorDescription;

		public NodeBuilder(Node<T, V> expandedNodeInternal, NodeExpansionDescription<T, A> successorDescription) {
			super();
			this.expandedNodeInternal = expandedNodeInternal;
			this.successorDescription = successorDescription;
		}

		@Override
		public void run() {
			try {
				if (ORGraphSearch.this.canceled || ORGraphSearch.this.interrupted)
					return;
				logger.debug("Start node creation.");
				lastExpansion.add(successorDescription);

				Node<T, V> newNode = newNode(expandedNodeInternal, successorDescription.getTo());

				/* update creation counter */
				createdCounter++;

				/* set timeout on thread that interrupts it after the timeout */
				int taskId = -1;
				if (timeoutForComputationOfF > 0) {
					if (timeoutSubmitter == null) {
						timeoutSubmitter = TimeoutTimer.getInstance().getSubmitter();
					}
					taskId = timeoutSubmitter.interruptMeAfterMS(timeoutForComputationOfF);
				}

				/* compute node label */
				V label = null;
				boolean computationTimedout = false;
				long startComputation = System.currentTimeMillis();
				try {
					label = nodeEvaluator.f(newNode);
					
					/* check whether the required time exceeded the timeout */
					long fTime = System.currentTimeMillis() - startComputation;
					if (timeoutForComputationOfF > 0 && fTime > timeoutForComputationOfF + 1000)
						logger.warn("Computation of f for node {} took {}ms, which is more than the allowed {}ms", newNode, fTime, timeoutForComputationOfF);
				} catch (InterruptedException e) {
					logger.debug("Received interrupt during computation of f.");
					graphEventBus.post(new NodeTypeSwitchEvent<>(newNode, "or_timedout"));
					newNode.setAnnotation("fError", "Timeout");
					computationTimedout = true;
					try {
						label = timeoutNodeEvaluator != null ? timeoutNodeEvaluator.f(newNode) : null;
					} catch (Throwable e2) {
						e2.printStackTrace();
					}
				} catch (Throwable e) {
					logger.error("Observed an execution during computation of f:\n{}", LoggerUtil.getExceptionInfo(e));
					newNode.setAnnotation("fError", e);
					graphEventBus.post(new NodeTypeSwitchEvent<>(newNode, "or_ffail"));
				}
				if (taskId >= 0)
					timeoutSubmitter.cancelTimeout(taskId);
				
				/* register time required to compute this node label */
				long fTime = System.currentTimeMillis() - startComputation;
				newNode.setAnnotation("fTime", fTime);
				
				/* if no label was computed, prune the node and cancel the computation */
				if (label == null) {
					if (!computationTimedout)
						logger.info("Not inserting node {} since its label is missing!", newNode);
					else
						logger.info("Not inserting node {} because computation of f-value timed out.", newNode);
					if (!newNode.getAnnotations().containsKey("fError"))
						newNode.setAnnotation("fError", "f-computer returned NULL");
					graphEventBus.post(new NodeTypeSwitchEvent<>(newNode, "or_pruned"));
					if (pool != null) {
						activeJobs.decrementAndGet();
						fComputationTickets.release();
					}
					return;
				}
				newNode.setInternalLabel(label);

				logger.info("Inserting successor {} of {} to OPEN. F-Value is {}", newNode, expandedNodeInternal, label);
				// assert !open.contains(newNode) && !expanded.contains(newNode.getPoint()) : "Inserted node is already in OPEN or even expanded!";

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
							open.remove(existingNode);
							open.add(newNode);
						} else {
							graphEventBus.post(new NodeRemovedEvent<>(newNode));
						}
						nodeProcessed = true;
					}

					/* if parent discarding is not only for OPEN but also for CLOSE (and the node was not on OPEN), check the list of expanded nodes */
					else if (parentDiscarding == ParentDiscarding.ALL) {

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

					if (!newNode.isGoal())
						open.add(newNode);
					graphEventBus.post(new NodeTypeSwitchEvent<>(newNode, "or_" + (newNode.isGoal() ? "solution" : "open")));
					createdCounter++;
				}

				/* Recognize solution in cache together with annotation */
				if (newNode.isGoal()) {
					List<T> solution = getTraversalPath(newNode);

					/* if the node evaluator has not reported the solution already anyway, register the solution and store its annotation */
					if (!solutionReportingNodeEvaluator && !solutions.contains(solution)) {
						solutions.add(solution);
						solutionAnnotations.put(solution, new HashMap<>());
						solutionAnnotations.get(solution).put("f", newNode.getInternalLabel());
					}
				}

				/* free resources if this is computed by helper threads */
				if (pool != null) {
					activeJobs.decrementAndGet();
					fComputationTickets.release();
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}

		}
	}

	public ORGraphSearch(GraphGenerator<T, A> graphGenerator, INodeEvaluator<T, V> pNodeEvaluator) {
		this(graphGenerator, pNodeEvaluator, ParentDiscarding.NONE);
	}

	@SuppressWarnings("unchecked")
	public ORGraphSearch(GraphGenerator<T, A> graphGenerator, INodeEvaluator<T, V> pNodeEvaluator, ParentDiscarding pd) {
		super();
		this.graphGenerator = graphGenerator;
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

		// /*setting a priorityqueueopen As a default open collection*/
		//
		// this.setOpen(new PriorityQueueOpen<Node<T,V>>());

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
				castedEvaluator.registerSolutionListener(this);
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
				((ISolutionReportingNodeEvaluator<T, V>) pNodeEvaluator).registerSolutionListener(this);
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

	private void labelNode(Node<T, V> node) throws Throwable {
		node.setInternalLabel(nodeEvaluator.f(node));
	}

	/**
	 * This method setups the graph by inserting the root nodes.
	 */
	protected synchronized void initGraph() throws Throwable {
		if (!initialized) {
			initialized = true;
			if (rootGenerator instanceof MultipleRootGenerator) {
				Collection<Node<T, V>> roots = ((MultipleRootGenerator<T>) rootGenerator).getRoots().stream().map(n -> newNode(null, n)).collect(Collectors.toList());
				for (Node<T, V> root : roots) {
					labelNode(root);
					open.add(root);
					root.setAnnotation("awa-level", 0);
					logger.info("Labeled root with {}", root.getInternalLabel());
				}
			} else {
				Node<T, V> root = newNode(null, ((SingleRootGenerator<T>) rootGenerator).getRoot());
				labelNode(root);
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

	public List<T> nextSolutionThatDominatesOpen() {
		List<T> currentlyBestSolution = null;
		V currentlyBestScore = null;
		do {
			List<T> solution = nextSolution();
			V scoreOfSolution = getFOfReturnedSolution(solution);
			if (currentlyBestScore == null || scoreOfSolution.compareTo(currentlyBestScore) < 0) {
				currentlyBestScore = scoreOfSolution;
				currentlyBestSolution = solution;
			}
		}
		while(open.peek().getInternalLabel().compareTo(currentlyBestScore) < 0);
		return currentlyBestSolution;
	}

	/**
	 * Find the shortest path to a goal starting from <code>start</code>.
	 *
	 * @param start
	 *            The initial node.
	 * @return A list of nodes from the initial point to a goal, <code>null</code> if a path doesn't exist.
	 */
	public List<T> nextSolution() {

		/* check whether solution has been canceled */
		if (canceled) {
			throw new IllegalStateException("Search has been canceled, no more solutions can be requested.");
		}

		/* do preliminary stuff: init graph (only for first call) and return unreturned solutions first */
		logger.info("Starting search for next solution. Size of OPEN is {}", open.size());
		try {
			initGraph();
		} catch (Throwable e) {
			e.printStackTrace();
			return null;
		}
		if (!solutions.isEmpty()) {
			logger.debug("Still have solution in cache, return it.");
			return solutions.poll();
		}
		do {

			/* busy waiting for new nodes in OPEN */
			while (open.isEmpty() && activeJobs.get() > 0) {
				logger.debug("Waiting 100ms, because OPEN size is {} and there are {} active jobs.", open.size(), activeJobs.get());
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					logger.info("Received interrupt signal");
					interrupted = true;
					break;
				}
			}
			if (open.isEmpty() || interrupted) {
				logger.debug("OPEN has size {} and interrupted is {}", open.size(), interrupted);
				break;
			}

			logger.debug("Iteration of main loop starts. Size of OPEN now {}. Now performing next expansion step.", open.size());
			step();
			if (!solutions.isEmpty()) {
				List<T> solution = solutions.poll();
				logger.debug("Iteration of main loop terminated. Found a solution to return. Size of OPEN now {}", open.size());
				return solution;
			}
			logger.debug("Iteration of main loop terminated. Size of OPEN now {}. Number of active jobs: {}", open.size(), activeJobs.get());
		} while ((!open.isEmpty() || activeJobs.get() > 0) && !interrupted);
		if (interrupted)
			logger.info("Algorithm was interrupted");
		if (open.isEmpty())
			logger.info("OPEN is empty, terminating (possibly returning a solution)");
		return solutions.isEmpty() ? null : solutions.poll();
	}

	protected boolean terminates() {
		return false;
	}

	/**
	 * Makes a single expansion and returns solution paths.
	 *
	 * @return The last found solution path.
	 */
	public List<NodeExpansionDescription<T, A>> nextExpansion() {
		if (!this.initialized) {
			try {
				initGraph();
			} catch (Throwable e) {
				e.printStackTrace();
				return null;
			}
			return lastExpansion;
		} else
			step();
		return lastExpansion;
	}

	protected void step() {
		if (beforeSelection()) {

			Node<T, V> nodeToExpand = open.peek();
			if (nodeToExpand == null)
				return;
			// assert parentDiscarding == ParentDiscarding.ALL || !expanded.contains(nodeToExpand.getPoint()) : "Node " + nodeToExpand.getString()
			// 		+ " has been selected for the second time for expansion.";
			afterSelection(nodeToExpand);
			step(nodeToExpand);
		}
	}

	public void step(Node<T, V> nodeToExpand) {

		// if (!(nodeEvaluator instanceof RandomizedDepthFirstEvaluator))
		// System.out.println(nodeToExpand.getAnnotations());

		/* if search has been interrupted, do not process next step */
		logger.debug("Step starts. Size of OPEN now {}", open.size());
		if (Thread.interrupted()) {
			logger.debug("Received interrupt signal before step.");
			interrupted = true;
			return;
		}
		lastExpansion.clear();
		assert nodeToExpand == null || !expanded.contains(nodeToExpand.getPoint()) : "Node selected for expansion already has been expanded: " + nodeToExpand;
		open.remove(nodeToExpand);
		assert !open.contains(nodeToExpand) : "The selected node " + nodeToExpand + " was not really removed from OPEN!";
		logger.debug("Removed {} from OPEN for expansion. OPEN size now {}", nodeToExpand, open.size());
		assert ext2int.containsKey(nodeToExpand.getPoint()) : "Trying to expand a node whose point is not available in the ext2int map";
		beforeExpansion(nodeToExpand);
		expandNode(nodeToExpand);
		afterExpansion(nodeToExpand);
		if (Thread.interrupted()) {
			logger.debug("Received interrupt signal during step.");
			interrupted = true;
		}
		logger.debug("Step ends. Size of OPEN now {}", open.size());
	}

	private void expandNode(Node<T, V> expandedNodeInternal) {
		graphEventBus.post(new NodeTypeSwitchEvent<Node<T, V>>(expandedNodeInternal, "or_expanding"));
		logger.info("Expanding node {} with f-value {}", expandedNodeInternal, expandedNodeInternal.getInternalLabel());
		assert !expanded.contains(expandedNodeInternal.getPoint()) : "Node " + expandedNodeInternal + " expanded twice!!";
		expanded.add(expandedNodeInternal.getPoint());
		assert expanded.contains(expandedNodeInternal.getPoint()) : "Expanded node " + expandedNodeInternal + " was not inserted into the set of expanded nodes!";

		/* compute successors */
		logger.debug("Start computation of successors");
		final Collection<NodeExpansionDescription<T, A>> successorDescriptions = new ArrayList<>();
		{
			Thread t = new Thread(new Runnable() {

				@Override
				public void run() {
					if (ORGraphSearch.this.canceled || ORGraphSearch.this.interrupted)
						return;
					int taskId = -1;
					if (timeoutForComputationOfF > 0) {
						if (timeoutSubmitter == null) {
							timeoutSubmitter = TimeoutTimer.getInstance().getSubmitter();
						}
						taskId = timeoutSubmitter.interruptMeAfterMS(timeoutForComputationOfF);
					}
					successorDescriptions.addAll(successorGenerator.generateSuccessors(expandedNodeInternal.getPoint()));
					if (taskId >= 0)
						timeoutSubmitter.cancelTimeout(taskId);
				}
			}, "Node Builder for some child of " + expandedNodeInternal);
			logger.debug("Starting computation of successors in thread {}", t);
			t.start();
			try {
				t.join();
			} catch (InterruptedException e) {
				logger.debug("Search has been interrupted");
				interrupted = true;
				return;
			}
			logger.debug("Finished computation of successors");
		}

		/* attach successors to search graph */
//		System.out.println(expanded.contains(expandedNodeInternal.getPoint()));
		if (additionalThreadsForExpansion < 1) {
			successorDescriptions.stream().forEach(successorDescription -> {

				/* perform synchronized computation. The computation is outourced, because it may receive an interrupt-signal, and we do not want the main-thread to be interrupted */
				Thread t = new Thread(new NodeBuilder(expandedNodeInternal, successorDescription), "Node Builder for some child of " + expandedNodeInternal);
				logger.debug("Starting computation of successor in thread {}", t);
				t.start();
				try {
					t.join();
				} catch (InterruptedException e) {
					logger.debug("Search has been interrupted");
					interrupted = true;
					return;
				}
				logger.debug("Finished computation of successor", t);
			});
		} else {
			successorDescriptions.stream().forEach(successorDescription -> {
				if (interrupted)
					return;
				try {
					fComputationTickets.acquire();
				} catch (InterruptedException e) {
					e.printStackTrace();
					interrupted = true;
				}
				if (interrupted)
					return;
				activeJobs.incrementAndGet();
				pool.submit(new NodeBuilder(expandedNodeInternal, successorDescription));
			});
		}
		logger.debug("Finished expansion of node {}. Size of OPEN is now {}. Number of active jobs is {}", expandedNodeInternal, open.size(), activeJobs.get());

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

	public Map<String, Object> getNodeAnnotations(T node) {
		Node<T, V> intNode = ext2int.get(node);
		return intNode.getAnnotations();
	}

	public Object getNodeAnnotation(T node, String annotation) {
		Node<T, V> intNode = ext2int.get(node);
		return intNode.getAnnotation(annotation);
	}

	public Map<String, Object> getAnnotationsOfReturnedSolution(List<T> solution) {
		return solutionAnnotations.get(solution);
	}

	public Object getAnnotationOfReturnedSolution(List<T> solution, String annotation) {
		return solutionAnnotations.get(solution).get(annotation);
	}

	public V getFOfReturnedSolution(List<T> solution) {
		@SuppressWarnings("unchecked")
		V annotation = (V) getAnnotationOfReturnedSolution(solution, "f");
		if (annotation == null) {
			throw new IllegalArgumentException(
					"There is no solution annotation for the given solution. Please check whether the solution was really produced by the algorithm. If so, please check that its annotation was added into the list of annotations before the solution itself was added to the solution set");
		}
		return annotation;
	}

	public void cancel() {
		StringBuilder sb = new StringBuilder();
		for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
			sb.append("\n" + ste.toString());
		}
		logger.info("Search has been canceled. Cancel came from: {}", sb.toString());
		this.canceled = true;
		this.interrupted = true;
		if (this.pool != null)
			this.pool.shutdownNow();
		if (nodeEvaluator instanceof ICancelableNodeEvaluator) {
			logger.info("Canceling node evaluator.");
			((ICancelableNodeEvaluator) nodeEvaluator).cancel();
		}
		if (timeoutSubmitter != null)
			timeoutSubmitter.close();
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

	protected synchronized Node<T, V> newNode(Node<T, V> parent, T t2) {
		return newNode(parent, t2, null);
	}

	public INodeEvaluator<T, V> getNodeEvaluator() {
		return this.nodeEvaluator;
	}

	protected synchronized Node<T, V> newNode(Node<T, V> parent, T t2, V evaluation) {
		assert parent == null || expanded.contains(parent.getPoint()) : "Generating successors of an unexpanded node " + parent + ". List of expanded nodes:\n" + expanded.stream().map(n -> "\n\t" + n.toString()).collect(Collectors.joining());
		assert !open.contains(parent) : "Parent node " + parent + " is still on OPEN, which must not be the case!";

		/* create new node and check whether it is a goal */
		Node<T, V> newNode = new Node<>(parent, t2);
		if (evaluation != null)
			newNode.setInternalLabel(evaluation);

		/* check loop */
		assert parent == null || !parent.externalPath().contains(t2) : "There is a loop in the underlying graph. The following path contains the last node twice: " + newNode.externalPath().stream().map(n -> n.toString()).reduce("", (s,t) -> s + "\n\t\t" + t);

		/* currently, we only support tree search */
		assert !ext2int.containsKey(t2) : "Reached node " + t2 + " for the second time.\nt\tFirst path:" + ext2int.get(t2).externalPath().stream().map(n -> n.toString()).reduce("", (s,t) -> s + "\n\t\t" + t)
				+ "\n\tSecond Path:" + newNode.externalPath().stream().map(n -> n.toString()).reduce("", (s,t) -> s + "\n\t\t" + t);

		/* register node in map and create annotation object */
		ext2int.put(t2, newNode);

		/* detect whether node is solution */
		if (checkGoalPropertyOnEntirePath ? pathGoalTester.isGoal(newNode.externalPath()) : nodeGoalTester.isGoal(newNode.getPoint()))
			newNode.setGoal(true);

		/* send events for this new node */
		if (parent == null) {
			this.graphEventBus.post(new GraphInitializedEvent<Node<T, V>>(newNode));
		} else {
			this.graphEventBus.post(new NodeReachedEvent<Node<T, V>>(parent, newNode, "or_" + (newNode.isGoal() ? "solution" : "created")));
			logger.debug("Sent message for creation of node {} as a successor of {}", newNode, parent);
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
		try {
			initGraph();
		} catch (Throwable e) {
			e.printStackTrace();
			return;
		}

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

	@Override
	public boolean hasNext() {
		if (!initialized) {
			try {
				initGraph();
			} catch (Throwable e) {
				e.printStackTrace();
				return false;
			}
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
	public void receiveSolutionEvent(SolutionFoundEvent<T, V> solution) {
		try {
			logger.info("Received solution with f-value {}", solution.getF());
			if (solutionAnnotations.containsKey(solution.getSolution()))
				throw new IllegalStateException("Solution is reported for the second time already!");
			solutionAnnotations.put(solution.getSolution(), new HashMap<>());
			solutionAnnotations.get(solution.getSolution()).put("f", solution.getF());
			solutions.add(solution.getSolution());
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@Subscribe
	public void receiveSolutionAnnotationEvent(SolutionAnnotationEvent<T, V> solution) {
		try {
			logger.debug("Received solution annotation: {}", solution);
			if (!solutionAnnotations.containsKey(solution.getSolution()))
				throw new IllegalStateException("Solution annotation is reported for a solution that has not been reported previously!");
			solutionAnnotations.get(solution.getSolution()).put(solution.getAnnotationName(), solution.getAnnotationValue());
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@Subscribe
	public void receiveNodeAnnotationEvent(NodeAnnotationEvent<T> event) {
		try {
			T nodeExt = event.getNode();
			logger.debug("Received annotation {} with value {} for node {}", event.getAnnotationName(), event.getAnnotationValue(), event.getNode());
			if (!ext2int.containsKey(nodeExt))
				throw new IllegalArgumentException("Received annotation for a node I don't know!");
			Node<T, V> nodeInt = ext2int.get(nodeExt);
			nodeInt.setAnnotation(event.getAnnotationName(), event.getAnnotationValue());
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public int getAdditionalThreadsForExpansion() {
		return additionalThreadsForExpansion;
	}

	public void parallelizeNodeExpansion(int threadsForExpansion) {
		if (this.pool != null)
			throw new UnsupportedOperationException("The number of additional threads can be only set once per search!");
		if (threadsForExpansion < 1)
			throw new IllegalArgumentException("Number of threads should be at least 1 for " + this.getClass().getName());
		this.fComputationTickets = new Semaphore(threadsForExpansion);
		this.additionalThreadsForExpansion = threadsForExpansion;
		AtomicInteger counter = new AtomicInteger(0);
		this.pool = Executors.newFixedThreadPool(threadsForExpansion, r -> {
			Thread t = new Thread(r);
			t.setName("ORGraphSearch-worker-" + counter.incrementAndGet());
			return t;
		});
	}

	public int getTimeoutForComputationOfF() {
		return timeoutForComputationOfF;
	}

	public void setTimeoutForComputationOfF(int timeoutInMS, INodeEvaluator<T, V> timeoutEvaluator) {
		this.timeoutForComputationOfF = timeoutInMS;
		this.timeoutNodeEvaluator = timeoutEvaluator;
	}

	/**
	 * @return the openCollection
	 */
	public OpenCollection<Node<T, V>> getOpen() {
		return open;
	}

	/**
	 * @param open
	 *            the openCollection to set
	 */
	public void setOpen(OpenCollection<Node<T, V>> collection) {

		collection.clear();
		collection.addAll(open);
		open = collection;
	}

	@Override
	public void setLoggerName(String name) {
		logger.info("Switching logger from {} to {}", logger.getName(), name);
		logger = LoggerFactory.getLogger(name);
		logger.info("Activated logger {} with name {}", name, logger.getName());
	}

	@Override
	public String getLoggerName() {
		return logger.getName();
	}

	@Override
	public void registerListener(Object listener) {
		this.graphEventBus.register(listener);
	}

	@Override
	public GraphGenerator<T, A> getGraphGenerator() {
		return graphGenerator;
	}
}
