package jaicore.search.algorithms.standard.bestfirst;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.aeonbits.owner.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

import jaicore.basic.ILoggingCustomizable;
import jaicore.basic.TimeOut;
import jaicore.basic.algorithm.AlgorithmEvent;
import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.AlgorithmFinishedEvent;
import jaicore.basic.algorithm.AlgorithmInitializedEvent;
import jaicore.basic.algorithm.AlgorithmState;
import jaicore.basic.algorithm.IAlgorithmConfig;
import jaicore.basic.algorithm.SolutionCandidateFoundEvent;
import jaicore.concurrent.InterruptionTimerTask;
import jaicore.concurrent.NamedTimerTask;
import jaicore.graphvisualizer.events.graphEvents.GraphInitializedEvent;
import jaicore.graphvisualizer.events.graphEvents.NodeParentSwitchEvent;
import jaicore.graphvisualizer.events.graphEvents.NodeReachedEvent;
import jaicore.graphvisualizer.events.graphEvents.NodeRemovedEvent;
import jaicore.graphvisualizer.events.graphEvents.NodeTypeSwitchEvent;
import jaicore.logging.LoggerUtil;
import jaicore.search.algorithms.standard.AbstractORGraphSearch;
import jaicore.search.algorithms.standard.bestfirst.events.EvaluatedSearchSolutionCandidateFoundEvent;
import jaicore.search.algorithms.standard.bestfirst.events.NodeAnnotationEvent;
import jaicore.search.algorithms.standard.bestfirst.events.NodeExpansionCompletedEvent;
import jaicore.search.algorithms.standard.bestfirst.events.NodeExpansionJobSubmittedEvent;
import jaicore.search.algorithms.standard.bestfirst.events.SolutionAnnotationEvent;
import jaicore.search.algorithms.standard.bestfirst.events.SuccessorComputationCompletedEvent;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.DecoratingNodeEvaluator;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.ICancelableNodeEvaluator;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.IGraphDependentNodeEvaluator;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.ISolutionReportingNodeEvaluator;
import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.model.other.EvaluatedSearchGraphPath;
import jaicore.search.model.probleminputs.GeneralEvaluatedTraversalTree;
import jaicore.search.model.travesaltree.Node;
import jaicore.search.model.travesaltree.NodeExpansionDescription;
import jaicore.search.structure.graphgenerator.MultipleRootGenerator;
import jaicore.search.structure.graphgenerator.NodeGoalTester;
import jaicore.search.structure.graphgenerator.PathGoalTester;
import jaicore.search.structure.graphgenerator.RootGenerator;
import jaicore.search.structure.graphgenerator.SingleRootGenerator;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;

public class BestFirst<I extends GeneralEvaluatedTraversalTree<N, A, V>, N, A, V extends Comparable<V>> extends AbstractORGraphSearch<I, EvaluatedSearchGraphPath<N, A, V>, N, A, V, Node<N, V>, A>
		implements ILoggingCustomizable {

	public enum ParentDiscarding {
		NONE, OPEN, ALL
	}

	/* problem definition */
	protected final GraphGenerator<N, A> graphGenerator;
	protected final RootGenerator<N> rootGenerator;
	protected final SuccessorGenerator<N, A> successorGenerator;
	protected final PathGoalTester<N> pathGoalTester;
	protected final NodeGoalTester<N> nodeGoalTester;
	protected final INodeEvaluator<N, V> nodeEvaluator;

	/* algorithm configuration */
	private IAlgorithmConfig config;
	private ParentDiscarding parentDiscarding;
	private int timeoutForComputationOfF;
	private INodeEvaluator<N, V> timeoutNodeEvaluator;

	/* automatically derived auxiliary variables */
	protected final boolean checkGoalPropertyOnEntirePath;
	private final boolean solutionReportingNodeEvaluator;
	private final boolean cancelableNodeEvaluator;

	/* general algorithm state and statistics */
	private int createdCounter;
	private int expandedCounter;
	private boolean initialized = false;
	private Timer timer;
	private List<NodeExpansionDescription<N, A>> lastExpansion = new ArrayList<>();
	protected final Queue<EvaluatedSearchGraphPath<N, A, V>> solutions = new LinkedBlockingQueue<>();
	protected final Queue<EvaluatedSearchSolutionCandidateFoundEvent<N, A, V>> pendingSolutionFoundEvents = new LinkedBlockingQueue<>();

	/* communication */
	private Logger logger = LoggerFactory.getLogger(BestFirst.class);
	protected final Map<N, Node<N, V>> ext2int = new ConcurrentHashMap<>();

	/* search graph model */
	protected Queue<Node<N, V>> open = new PriorityQueue<>();
	private Node<N, V> nodeSelectedForExpansion; // the node that will be expanded next
	private final Map<N, Thread> expanding = new HashMap<>(); // EXPANDING contains the nodes being expanded and the threads doing this job
	private final Set<N> closed = new HashSet<>(); // CLOSED contains only node but not paths

	/* parallelization */
	protected int additionalThreadsForNodeAttachment = 0;
	private ExecutorService pool;
	protected final AtomicInteger activeJobs = new AtomicInteger(0);
	private Lock activeJobsCounterLock = new ReentrantLock(); // lock that has to be locked before accessing the open queue
	private Lock openLock = new ReentrantLock(); // lock that has to be locked before accessing the open queue
	private Lock nodeSelectionLock = new ReentrantLock(true);
	private Condition numberOfActiveJobsHasChanged = this.activeJobsCounterLock.newCondition(); // condition that is signaled whenever a node is added to the open queue

	public BestFirst(final I problem) {
		this(problem, ConfigFactory.create(IBestFirstConfig.class));
	}

	@SuppressWarnings("unchecked")
	public BestFirst(final I problem, final IBestFirstConfig config) {
		super(problem);
		this.graphGenerator = problem.getGraphGenerator();
		this.rootGenerator = this.graphGenerator.getRootGenerator();
		this.successorGenerator = this.graphGenerator.getSuccessorGenerator();
		this.checkGoalPropertyOnEntirePath = !(this.graphGenerator.getGoalTester() instanceof NodeGoalTester);
		if (this.checkGoalPropertyOnEntirePath) {
			this.nodeGoalTester = null;
			this.pathGoalTester = (PathGoalTester<N>) this.graphGenerator.getGoalTester();
			;
		} else {
			this.nodeGoalTester = (NodeGoalTester<N>) this.graphGenerator.getGoalTester();
			this.pathGoalTester = null;
		}

		/* set parent discarding */
		this.config = config;
		this.parentDiscarding = config.parentDiscarding();

		/* if the node evaluator is graph dependent, communicate the generator to it */
		this.nodeEvaluator = problem.getNodeEvaluator();
		if (this.nodeEvaluator == null) {
			throw new IllegalArgumentException("Cannot work with node evaulator that is null");
		} else if (this.nodeEvaluator instanceof DecoratingNodeEvaluator<?, ?>) {
			DecoratingNodeEvaluator<N, V> castedEvaluator = (DecoratingNodeEvaluator<N, V>) this.nodeEvaluator;
			if (castedEvaluator.isGraphDependent()) {
				this.logger.info("{} is a graph dependent node evaluator. Setting its graph generator now ...", castedEvaluator);
				castedEvaluator.setGenerator(this.graphGenerator);
			}
			if (castedEvaluator.isSolutionReporter()) {
				this.logger.info("{} is a solution reporter. Register the search algo in its event bus", castedEvaluator);
				castedEvaluator.registerSolutionListener(this);
				this.solutionReportingNodeEvaluator = true;
			} else {
				this.solutionReportingNodeEvaluator = false;
			}
		} else {
			if (this.nodeEvaluator instanceof IGraphDependentNodeEvaluator) {
				this.logger.info("{} is a graph dependent node evaluator. Setting its graph generator now ...", this.nodeEvaluator);
				((IGraphDependentNodeEvaluator<N, A, V>) this.nodeEvaluator).setGenerator(this.graphGenerator);
			}

			/* if the node evaluator is a solution reporter, register in his event bus */
			if (this.nodeEvaluator instanceof ISolutionReportingNodeEvaluator) {
				this.logger.info("{} is a solution reporter. Register the search algo in its event bus", this.nodeEvaluator);
				((ISolutionReportingNodeEvaluator<N, V>) this.nodeEvaluator).registerSolutionListener(this);
				this.solutionReportingNodeEvaluator = true;
			} else {
				this.solutionReportingNodeEvaluator = false;
			}
		}
		cancelableNodeEvaluator = this.nodeEvaluator instanceof ICancelableNodeEvaluator;

		/* add shutdown hook so as to cancel the search once the overall program is shutdown */
		Runtime.getRuntime().addShutdownHook(new Thread(() -> BestFirst.this.cancel(), "Shutdown hook thread for " + BestFirst.this));
	}

	/** BLOCK A: Internal behavior of the algorithm **/

	private class NodeBuilder implements Runnable {

		private final Collection<N> todoList;
		private final Node<N, V> expandedNodeInternal;
		private final NodeExpansionDescription<N, A> successorDescription;

		public NodeBuilder(final Collection<N> todoList, final Node<N, V> expandedNodeInternal, final NodeExpansionDescription<N, A> successorDescription) {
			super();
			this.todoList = todoList;
			this.expandedNodeInternal = expandedNodeInternal;
			this.successorDescription = successorDescription;
		}

		private void communicateJobFinished() {
			synchronized (this.todoList) {
				this.todoList.remove(this.successorDescription.getTo());
				if (this.todoList.isEmpty()) {
					postEvent(new NodeExpansionCompletedEvent<>(this.expandedNodeInternal));
				}
			}
		}

		@Override
		public void run() {
			try {
				if (isStopCriterionSatisfied()) {
					this.communicateJobFinished();
					return;
				}
				BestFirst.this.logger.debug("Start node creation.");
				BestFirst.this.lastExpansion.add(this.successorDescription);

				Node<N, V> newNode = BestFirst.this.newNode(this.expandedNodeInternal, this.successorDescription.getTo());

				/* update creation counter */
				BestFirst.this.createdCounter++;

				/* compute node label */
				try {
					labelNode(newNode);
					if (newNode.getInternalLabel() == null) {
						postEvent(new NodeTypeSwitchEvent<>(newNode, "or_pruned"));
						return;
					}
					if (isStopCriterionSatisfied()) {
						this.communicateJobFinished();
						return;
					}
				}
				catch (InterruptedException e) {
					if (!isShutdownInitialized())
						logger.warn("Leaving node building routine due to interrupt. This leaves the search inconsistent; the node should be attached again!");
					return;
				}
				catch (TimeoutException e) {
					BestFirst.this.logger.debug("Node evaluation of {} has timed out.", newNode);
					newNode.setAnnotation("fError", e);
					postEvent(new NodeTypeSwitchEvent<>(newNode, "or_timedout"));
					return;
				}
				catch (Throwable e) {
					BestFirst.this.logger.error("Observed an exception during computation of f:\n{}", LoggerUtil.getExceptionInfo(e));
					newNode.setAnnotation("fError", e);
					postEvent(new NodeTypeSwitchEvent<>(newNode, "or_ffail"));
					return;
				}

				/* depending on the algorithm setup, now decide how to proceed with the node */

				/* if we discard (either only on OPEN or on both OPEN and CLOSED) */
				boolean nodeProcessed = false;
				if (BestFirst.this.parentDiscarding != ParentDiscarding.NONE) {
					BestFirst.this.openLock.lockInterruptibly();
					try {
						/* determine whether we already have the node AND it is worse than the one we want to insert */
						Optional<Node<N, V>> existingIdenticalNodeOnOpen = BestFirst.this.open.stream().filter(n -> n.getPoint().equals(newNode.getPoint())).findFirst();
						if (existingIdenticalNodeOnOpen.isPresent()) {
							Node<N, V> existingNode = existingIdenticalNodeOnOpen.get();
							if (newNode.compareTo(existingNode) < 0) {
								postEvent(new NodeTypeSwitchEvent<>(newNode, "or_" + (newNode.isGoal() ? "solution" : "open")));
								postEvent(new NodeRemovedEvent<>(existingNode));
								BestFirst.this.open.remove(existingNode);
								if (newNode.getInternalLabel() == null)
									throw new IllegalArgumentException("Cannot insert nodes with value NULL into OPEN!");
								BestFirst.this.open.add(newNode);
							} else {
								postEvent(new NodeRemovedEvent<>(newNode));
							}
							nodeProcessed = true;
						}

						/* if parent discarding is not only for OPEN but also for CLOSE (and the node was not on OPEN), check the list of expanded nodes */
						else if (BestFirst.this.parentDiscarding == ParentDiscarding.ALL) {
							/* reopening, if the node is already on CLOSED */
							Optional<N> existingIdenticalNodeOnClosed = BestFirst.this.closed.stream().filter(n -> n.equals(newNode.getPoint())).findFirst();
							if (existingIdenticalNodeOnClosed.isPresent()) {
								Node<N, V> node = BestFirst.this.ext2int.get(existingIdenticalNodeOnClosed.get());
								if (newNode.compareTo(node) < 0) {
									node.setParent(newNode.getParent());
									node.setInternalLabel(newNode.getInternalLabel());
									BestFirst.this.closed.remove(node.getPoint());
									BestFirst.this.open.add(node);
									postEvent(new NodeParentSwitchEvent<Node<N, V>>(node, node.getParent(), newNode.getParent()));
								}
								postEvent(new NodeRemovedEvent<Node<N, V>>(newNode));
								nodeProcessed = true;
							}
						}
					} finally {
						BestFirst.this.openLock.unlock();
					}
				}

				/* if parent discarding is turned off OR if the node was node processed by a parent discarding rule, just insert it on OPEN */
				if (!nodeProcessed) {
					if (!newNode.isGoal()) {
						BestFirst.this.openLock.lockInterruptibly();
						synchronized (expanding) {
							try {
								assert !BestFirst.this.closed
										.contains(newNode.getPoint()) : "Currently only tree search is supported. But now we add a node to OPEN whose point has already been expanded before.";
								expanding.keySet().forEach(node -> {
									assert !node.equals(newNode.getPoint()) : Thread.currentThread() + " cannot add node to OPEN that is currently being expanded by " + expanding.get(node)
											+ ".\n\tFrom: " + newNode.getParent().getPoint() + "\n\tTo: " + node;
								});
								if (newNode.getInternalLabel() == null)
									throw new IllegalArgumentException("Cannot insert nodes with value NULL into OPEN!");
								BestFirst.this.logger.info("Inserting successor {} of {} to OPEN. F-Value is {}", newNode, this.expandedNodeInternal, newNode.getInternalLabel());
								BestFirst.this.open.add(newNode);
							} finally {
								BestFirst.this.openLock.unlock();
							}
						}
					}
					postEvent(new NodeTypeSwitchEvent<>(newNode, "or_" + (newNode.isGoal() ? "solution" : "open")));
					BestFirst.this.createdCounter++;
				}

				/* Recognize solution in cache together with annotation */
				if (newNode.isGoal()) {
					EvaluatedSearchGraphPath<N, A, V> solution = new EvaluatedSearchGraphPath<>(newNode.externalPath(), null, newNode.getInternalLabel());

					/* if the node evaluator has not reported the solution already anyway, register the solution */
					if (!BestFirst.this.solutionReportingNodeEvaluator) {
						registerSolution(solution);
					}
				}
			} catch (InterruptedException e) {
				logger.info("Node builder has been interrupted, finishing execution.");
			} catch (Throwable e) {
				e.printStackTrace();
			} finally {

				/* free resources if this is computed by helper threads and notify the listeners */
				assert !Thread.holdsLock(openLock) : "Node Builder must not hold a lock on OPEN when locking the active jobs counter";
				BestFirst.this.activeJobsCounterLock.lock(); // cannot be interruptible without opening more cases
				try {
					if (BestFirst.this.pool != null) {
						BestFirst.this.activeJobs.decrementAndGet();
					}
				} finally {
					BestFirst.this.numberOfActiveJobsHasChanged.signalAll();
					BestFirst.this.activeJobsCounterLock.unlock();
				}
				this.communicateJobFinished();
			}
		}
	}

	protected Node<N, V> newNode(final Node<N, V> parent, final N t2) throws InterruptedException {
		return this.newNode(parent, t2, null);
	}

	protected Node<N, V> newNode(final Node<N, V> parent, final N t2, final V evaluation) throws InterruptedException {
		this.openLock.lockInterruptibly();
		try {
			assert !this.open.contains(parent) : "Parent node " + parent + " is still on OPEN, which must not be the case! OPEN class: " + this.open.getClass().getName() + ". OPEN size: "
					+ this.open.size();
		} finally {
			this.openLock.unlock();
		}

		/* create new node and check whether it is a goal */
		Node<N, V> newNode = new Node<>(parent, t2);
		if (evaluation != null) {
			newNode.setInternalLabel(evaluation);
		}

		/* check loop */
		assert parent == null || !parent.externalPath().contains(t2) : "There is a loop in the underlying graph. The following path contains the last node twice: "
				+ newNode.externalPath().stream().map(n -> n.toString()).reduce("", (s, t) -> s + "\n\t\t" + t);

		/* currently, we only support tree search */
		assert !this.ext2int.containsKey(t2) : "Reached node " + t2 + " for the second time.\nt\tFirst path:"
				+ this.ext2int.get(t2).externalPath().stream().map(n -> n.toString()).reduce("", (s, t) -> s + "\n\t\t" + t) + "\n\tSecond Path:"
				+ newNode.externalPath().stream().map(n -> n.toString()).reduce("", (s, t) -> s + "\n\t\t" + t);

		/* register node in map and create annotation object */
		this.ext2int.put(t2, newNode);

		/* detect whether node is solution */
		if (this.checkGoalPropertyOnEntirePath ? this.pathGoalTester.isGoal(newNode.externalPath()) : this.nodeGoalTester.isGoal(newNode.getPoint())) {
			newNode.setGoal(true);
		}

		/* send events for this new node */
		if (parent == null) {
			postEvent(new GraphInitializedEvent<Node<N, V>>(newNode));
		} else {
			postEvent(new NodeReachedEvent<Node<N, V>>(parent, newNode, "or_" + (newNode.isGoal() ? "solution" : "created")));
			this.logger.debug("Sent message for creation of node {} as a successor of {}", newNode, parent);
		}
		return newNode;
	}

	protected void labelNode(final Node<N, V> node) throws Exception {

		/* define timeouter for label computation */
		InterruptionTimerTask interruptionTask = null;
		AtomicBoolean timedout = new AtomicBoolean(false);
		if (BestFirst.this.timeoutForComputationOfF > 0) {
			interruptionTask = new InterruptionTimerTask("Timeout for Node-Labeling in " + BestFirst.this, Thread.currentThread(), () -> timedout.set(true));
			logger.debug("Scheduling timeout for f-value computation. Allowed time: {}ms", timeoutForComputationOfF);
			timer.schedule(interruptionTask, timeoutForComputationOfF);
		}

		/* compute f */
		V label = null;
		boolean computationTimedout = false;
		long startComputation = System.currentTimeMillis();
		try {
			label = BestFirst.this.nodeEvaluator.f(node);
			if (isStopCriterionSatisfied()) {
				return;
			}

			/* check whether the required time exceeded the timeout */
			long fTime = System.currentTimeMillis() - startComputation;
			if (BestFirst.this.timeoutForComputationOfF > 0 && fTime > BestFirst.this.timeoutForComputationOfF + 1000) {
				BestFirst.this.logger.warn("Computation of f for node {} took {}ms, which is more than the allowed {}ms", node, fTime, BestFirst.this.timeoutForComputationOfF);
			}
		} catch (InterruptedException e) {
			logger.info("Thread {} received interrupt in node evaluation. Timeout flag is {}", Thread.currentThread(), timedout.get());
			if (timedout.get()) {
				BestFirst.this.logger.debug("Received interrupt during computation of f.");
				postEvent(new NodeTypeSwitchEvent<>(node, "or_timedout"));
				node.setAnnotation("fError", "Timeout");
				computationTimedout = true;
				Thread.interrupted(); // set interrupt state of thread to FALSE, because interrupt
				try {
					label = BestFirst.this.timeoutNodeEvaluator != null ? BestFirst.this.timeoutNodeEvaluator.f(node) : null;
				} catch (Throwable e2) {
					e2.printStackTrace();
				}
			} else {
				logger.info("Received external interrupt. Forwarding this interrupt.");
				throw e;
			}
		}
		if (interruptionTask != null) {
			interruptionTask.cancel();
		}

		/* register time required to compute this node label */
		long fTime = System.currentTimeMillis() - startComputation;
		node.setAnnotation("fTime", fTime);
		logger.debug("Computed label {} for {} in {}ms", label, node, fTime);

		/* if no label was computed, prune the node and cancel the computation */
		if (label == null) {
			if (!computationTimedout) {
				BestFirst.this.logger.info("Not inserting node {} since its label is missing!", node);
			} else {
				BestFirst.this.logger.info("Not inserting node {} because computation of f-value timed out.", node);
			}
			if (!node.getAnnotations().containsKey("fError")) {
				node.setAnnotation("fError", "f-computer returned NULL");
			}
			return;
		}

		/* eventually set the label */
		node.setInternalLabel(label);
		assert node.getInternalLabel() != null : "Node label must not be NULL";
	}

	/**
	 * This method setups the graph by inserting the root nodes.
	 */
	protected void initGraph() throws Exception {
		if (!this.initialized) {
			this.initialized = true;
			if (this.rootGenerator instanceof MultipleRootGenerator) {
				for (N n0 : ((MultipleRootGenerator<N>) this.rootGenerator).getRoots()) {
					Node<N, V> root = this.newNode(null, n0);
					this.labelNode(root);
					this.openLock.lockInterruptibly();
					try {
						if (root == null)
							throw new IllegalArgumentException("Cannot add NULL as a node to OPEN");
						this.open.add(root);
					} finally {
						this.openLock.unlock();
					}
					this.logger.info("Labeled root with {}", root.getInternalLabel());
				}
			} else {
				Node<N, V> root = this.newNode(null, ((SingleRootGenerator<N>) this.rootGenerator).getRoot());
				if (root == null)
					throw new IllegalArgumentException("Cannot add NULL as a node to OPEN");
				this.labelNode(root);
				if (root.getInternalLabel() == null) {
					throw new IllegalArgumentException("The node evaluator has assigned NULL to the root node, which impedes an initialization of the search graph. Node evaluator: " + nodeEvaluator);
				}
				this.openLock.lockInterruptibly();
				try {
					this.open.add(root);
				} finally {
					this.openLock.unlock();
				}
			}

		}

	}

	protected void selectNodeForNextExpansion(Node<N, V> node) throws InterruptedException {
		assert node != null : "Cannot select node NULL for expansion!";
		nodeSelectionLock.lockInterruptibly();
		try {
			openLock.lockInterruptibly();
			try {
				assert !open.contains(null) : "OPEN contains NULL";
				assert !open.stream().filter(n -> n.getInternalLabel() == null).findAny().isPresent() : "OPEN contains an element with value NULL";
				int openSizeBefore = open.size();
				assert nodeSelectedForExpansion == null : "Node selected for expansion must be NULL when setting it!";
				nodeSelectedForExpansion = node;
				assert open.contains(node) : "OPEN must contain the node to be expanded.\n\tOPEN size: " + open.size() + "\n\tNode to be expanded: " + node + ".\n\tOPEN: "
						+ open.stream().map(n -> "\n\t\t" + n).collect(Collectors.joining());
				open.remove(nodeSelectedForExpansion);
				int openSizeAfter = open.size();
				assert this.ext2int.containsKey(nodeSelectedForExpansion.getPoint()) : "A node chosen for expansion has no entry in the ext2int map!";
				assert openSizeAfter == openSizeBefore - 1 : "OPEN size must descrease by one when selecting node for expansion";
			} finally {
				openLock.unlock();
			}
		} finally {
			nodeSelectionLock.unlock();
		}
	}

	/**
	 * This method conducts the expansion of the next node. Unless the next node has been selected from outside, it selects the first node on OPEN (if OPEN is empty but active jobs are running, it waits until those terminate)
	 * 
	 * @return
	 * @throws InterruptedException
	 * @throws AlgorithmExecutionCanceledException
	 * @throws TimeoutException
	 */
	protected NodeExpansionJobSubmittedEvent<N, A, V> expandNextNode() throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException {

		/* Preliminarily check that the active jobs are less than the additional threads */
		assert additionalThreadsForNodeAttachment == 0 || activeJobs.get() < additionalThreadsForNodeAttachment : "Cannot expand nodes if number of active jobs (" + activeJobs.get()
				+ " is at least as high as the threads available for node attachment (" + additionalThreadsForNodeAttachment + ")";

		/* Step 1: determine node that will be expanded next. Either it already has been set or it will be the first of OPEN. If necessary, we wait for potential incoming nodes */
		final Node<N, V> nodeSelectedForExpansion;
		{
			Node<N, V> tmpNodeSelectedForExpansion = null; // necessary workaround as setting final variables in a try-block is not reasonably possible
			nodeSelectionLock.lockInterruptibly();
			try {
				if (this.nodeSelectedForExpansion == null) {
					activeJobsCounterLock.lockInterruptibly();
					try {
						this.logger.debug("No next node has been selected. Choosing the first from OPEN.");
						while (this.open.isEmpty() && this.activeJobs.get() > 0) {
							logger.info("Await condition as open queue is empty and active jobs is " + this.activeJobs.get() + "...");
							this.numberOfActiveJobsHasChanged.await();
							logger.info("Got signaled");
							this.checkTermination();
						}
						openLock.lock();
						try {
							if (this.open.isEmpty())
								return null;
							selectNodeForNextExpansion(open.peek());
						} finally {
							openLock.unlock();
						}
					} finally {
						activeJobsCounterLock.unlock();
					}
				}
				assert this.nodeSelectedForExpansion != null : "We have not selected any node for expansion, but this must be the case at this point.";
				tmpNodeSelectedForExpansion = this.nodeSelectedForExpansion;
				this.nodeSelectedForExpansion = null;
			} finally {
				nodeSelectionLock.unlock();
			}
			assert this.nodeSelectedForExpansion == null : "The object variable for the next selected node must be NULL at the end of the select step.";
			nodeSelectedForExpansion = tmpNodeSelectedForExpansion;
			synchronized (expanding) {
				this.expanding.put(nodeSelectedForExpansion.getPoint(), Thread.currentThread());
				assert this.expanding.keySet().contains(tmpNodeSelectedForExpansion.getPoint()) : "The node selected for expansion should be in the EXPANDING map by now.";
			}
			assert !open.contains(nodeSelectedForExpansion) : "Node selected for expansion is still on OPEN";
			assert nodeSelectedForExpansion != null : "We have not selected any node for expansion, but this must be the case at this point.";
			this.checkTermination();
		}

		/* Step 2: compute the successors in the underlying graph */
		this.beforeExpansion(nodeSelectedForExpansion);
		postEvent(new NodeTypeSwitchEvent<Node<N, V>>(nodeSelectedForExpansion, "or_expanding"));
		this.logger.info("Expanding node {} with f-value {}", nodeSelectedForExpansion, nodeSelectedForExpansion.getInternalLabel());
		this.logger.debug("Start computation of successors");
		final List<NodeExpansionDescription<N, A>> successorDescriptions;
		{
			List<NodeExpansionDescription<N, A>> tmpSuccessorDescriptions = null;
			tmpSuccessorDescriptions = BestFirst.this.successorGenerator.generateSuccessors(nodeSelectedForExpansion.getPoint());
			successorDescriptions = tmpSuccessorDescriptions;
		}
		this.checkTermination();
		this.logger.debug("Finished computation of successors. Sending SuccessorComputationCompletedEvent with {} successors for {}", successorDescriptions.size(), nodeSelectedForExpansion);
		postEvent(new SuccessorComputationCompletedEvent<>(nodeSelectedForExpansion, successorDescriptions));

		/* step 3: trigger node builders that compute node details and decide whether and how to integrate the successors into the search */
		List<N> todoList = successorDescriptions.stream().map(d -> d.getTo()).collect(Collectors.toList());
		for (NodeExpansionDescription<N, A> successorDescription : successorDescriptions) {
			NodeBuilder nb = new NodeBuilder(todoList, nodeSelectedForExpansion, successorDescription);
			if (this.additionalThreadsForNodeAttachment < 1) {
				nb.run();
			} else {
				activeJobsCounterLock.lockInterruptibly();
				try {
					this.activeJobs.incrementAndGet();
				} finally {
					numberOfActiveJobsHasChanged.signalAll();
					activeJobsCounterLock.unlock();
				}
				this.pool.submit(nb);
			}
		}
		this.checkTermination();
		this.logger.debug("Finished expansion of node {}. Size of OPEN is now {}. Number of active jobs is {}", nodeSelectedForExpansion, this.open.size(), this.activeJobs.get());

		/* step 4: update statistics, send closed notifications, and possibly return a solution */
		this.expandedCounter++;
		synchronized (expanding) {
			this.expanding.remove(nodeSelectedForExpansion.getPoint());
			assert !expanding.containsKey(nodeSelectedForExpansion.getPoint()) : "Expanded node " + nodeSelectedForExpansion + " was not removed from EXPANDING!";
		}
		this.closed.add(nodeSelectedForExpansion.getPoint());
		assert this.closed.contains(nodeSelectedForExpansion.getPoint()) : "Expanded node " + nodeSelectedForExpansion + " was not inserted into CLOSED!";
		postEvent(new NodeTypeSwitchEvent<Node<N, V>>(nodeSelectedForExpansion, "or_closed"));
		NodeExpansionJobSubmittedEvent<N, A, V> nodeCompletionEvent = new NodeExpansionJobSubmittedEvent<>(nodeSelectedForExpansion, successorDescriptions);
		this.afterExpansion(nodeSelectedForExpansion);
		this.checkTermination();
		this.openLock.lockInterruptibly();
		try {
			this.logger.debug("Step ends. Size of OPEN now {}", this.open.size());
		} finally {
			this.openLock.unlock();
		}
		return nodeCompletionEvent;
	}

	protected EvaluatedSearchSolutionCandidateFoundEvent<N, A, V> registerSolution(final EvaluatedSearchGraphPath<N, A, V> solutionPath) {
		EvaluatedSearchSolutionCandidateFoundEvent<N, A, V> solutionEvent = super.registerSolution(solutionPath); // this emits an event on the event bus
		assert !solutions.contains(solutionEvent.getSolutionCandidate()) : "Registering solution " + solutionEvent.getSolutionCandidate() + " for the second time!";
		this.solutions.add(solutionEvent.getSolutionCandidate());
		synchronized (pendingSolutionFoundEvents) {
			this.pendingSolutionFoundEvents.add(solutionEvent);
		}
		return solutionEvent;
	}

	protected void shutdown() {

		if (isShutdownInitialized())
			return;

		/* set state to inactive*/
		this.logger.info("Invoking shutdown routine ...");

		super.shutdown();

		/* interrupt the expanding threads */
		synchronized (expanding) {
			expanding.values().forEach(t -> t.interrupt());
		}

		/* cancel ongoing work */
		if (additionalThreadsForNodeAttachment > 0) {
			if (this.pool != null) {
				this.logger.info("Triggering shutdown of builder thread pool with interrupt");
				this.pool.shutdownNow();
			}
			try {
				this.pool.awaitTermination(10, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				logger.warn("Got interrupted during shutdown!");
			}
			if (!this.pool.isTerminated())
				logger.error("Worker pool has not been shutdown correctly!");
			else
				logger.info("Worker pool has been shut down.");
			logger.info("Setting number of active jobs to 0.");
			activeJobsCounterLock.lock();
			try {
				activeJobs.set(0);
				numberOfActiveJobsHasChanged.signalAll();
			} finally {
				activeJobsCounterLock.unlock();
			}
		}

		/* cancel node evaluator */
		if (cancelableNodeEvaluator) {
			this.logger.info("Canceling node evaluator.");
			((ICancelableNodeEvaluator) this.nodeEvaluator).cancel();
		}

		/* cancel timer (wait until possible if necessary) */
		if (timer != null) {
			logger.info("Waiting for timer lock to shutdown the timer ...");
			timer.cancel();
		}
		logger.info("Shutdown completed");
	}

	@Subscribe
	public void receiveSolutionCandidateEvent(final EvaluatedSearchSolutionCandidateFoundEvent<N, A, V> solutionEvent) {
		try {
			this.logger.info("Received solution with f-value {} and annotations {}", solutionEvent.getSolutionCandidate().getScore(), solutionEvent.getSolutionCandidate().getAnnotations());
			this.registerSolution(solutionEvent.getSolutionCandidate()); // unpack this solution and plug it into the registration process
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@Subscribe
	public void receiveSolutionCandidateAnnotationEvent(final SolutionAnnotationEvent<N, A, V> event) {
		try {
			this.logger.debug("Received solution annotation: {}", event);
			postEvent(event);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@Subscribe
	public void receiveNodeAnnotationEvent(final NodeAnnotationEvent<N> event) {
		try {
			N nodeExt = event.getNode();
			this.logger.debug("Received annotation {} with value {} for node {}", event.getAnnotationName(), event.getAnnotationValue(), event.getNode());
			if (!this.ext2int.containsKey(nodeExt)) {
				throw new IllegalArgumentException("Received annotation for a node I don't know!");
			}
			Node<N, V> nodeInt = this.ext2int.get(nodeExt);
			nodeInt.setAnnotation(event.getAnnotationName(), event.getAnnotationValue());
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	protected void insertNodeIntoLocalGraph(final Node<N, V> node) throws InterruptedException {
		Node<N, V> localVersionOfParent = null;
		List<Node<N, V>> path = node.path();
		Node<N, V> leaf = path.get(path.size() - 1);
		for (Node<N, V> nodeOnPath : path) {
			if (!this.ext2int.containsKey(nodeOnPath.getPoint())) {
				assert nodeOnPath.getParent() != null : "Want to insert a new node that has no parent. That must not be the case! Affected node is: " + nodeOnPath.getPoint();
				assert this.ext2int.containsKey(nodeOnPath.getParent().getPoint()) : "Want to insert a node whose parent is unknown locally";
				Node<N, V> newNode = this.newNode(localVersionOfParent, nodeOnPath.getPoint(), nodeOnPath.getInternalLabel());
				if (!newNode.isGoal() && !newNode.getPoint().equals(leaf.getPoint())) {
					postEvent(new NodeTypeSwitchEvent<Node<N, V>>(newNode, "or_closed"));
				}
				localVersionOfParent = newNode;
			} else {
				localVersionOfParent = this.getLocalVersionOfNode(nodeOnPath);
			}
		}
	}

	/**
	 * This is relevant if we work with several copies of a node (usually if we need to copy the search space somewhere).
	 *
	 * @param node
	 * @return
	 */
	protected Node<N, V> getLocalVersionOfNode(final Node<N, V> node) {
		return this.ext2int.get(node.getPoint());
	}

	/** BLOCK B: Controlling the algorithm from the outside **/

	/**
	 * This method can be used to create an initial graph different from just root nodes. This can be interesting if the search is distributed and we want to search only an excerpt of the original one.
	 *
	 * @param initialNodes
	 */
	public void bootstrap(final Collection<Node<N, V>> initialNodes) throws InterruptedException {

		if (this.initialized) {
			throw new UnsupportedOperationException("Bootstrapping is only supported if the search has already been initialized.");
		}

		/* now initialize the graph */
		try {
			this.initGraph();
		} catch (Throwable e) {
			e.printStackTrace();
			return;
		}

		this.openLock.lockInterruptibly();
		try {
			/* remove previous roots from open */
			this.open.clear();
			/* now insert new nodes, and the leaf ones in open */
			for (Node<N, V> node : initialNodes) {
				this.insertNodeIntoLocalGraph(node);
				if (node == null)
					throw new IllegalArgumentException("Cannot add NULL as a node to OPEN");
				if (node.getInternalLabel() == null)
					throw new IllegalArgumentException("Cannot insert node with label NULL");
				this.open.add(this.getLocalVersionOfNode(node));
			}
		} finally {
			this.openLock.unlock();
		}
	}

	@Override
	public AlgorithmEvent nextWithException() throws Exception {
		switch (getState()) {
		case created: {
			this.logger.info("Initializing BestFirst search {} with {} CPUs and a timeout of {}ms", this, this.config.cpus(), this.config.timeout());
			timer = new Timer("Timer of BestFirst search " + this);
			if (config.timeout() > 0) {
				timer.schedule(new NamedTimerTask("Timeout for the entire search of " + BestFirst.this) {

					@Override
					public void run() {
						logger.info("Timeout triggered. Setting timeout flag to true and initiating shutdown.");
						setTimeouted(true);
						shutdown();
					}

				}, config.timeout());
			}
			this.parallelizeNodeExpansion(this.config.cpus());
			this.initGraph();
			switchState(AlgorithmState.active);
			AlgorithmEvent event = new AlgorithmInitializedEvent();
			postEvent(event);
			return event;
		}
		case active: {
			synchronized (pendingSolutionFoundEvents) {
				if (!this.pendingSolutionFoundEvents.isEmpty()) {
					return pendingSolutionFoundEvents.poll(); // these already have been posted over the event bus but are now returned to the controller for respective handling
				}
			}
			AlgorithmEvent event;

			try {

				/* if worker threads are used for expansion, make sure that there is at least one that is not busy */
				if (additionalThreadsForNodeAttachment > 0) {
					activeJobsCounterLock.lockInterruptibly();
					try {
						while (additionalThreadsForNodeAttachment <= activeJobs.get()) {
							checkTermination();
							logger.info("Waiting as {} jobs are running but only {} threads are available", activeJobs.get(), additionalThreadsForNodeAttachment);
							numberOfActiveJobsHasChanged.await();
						}
					} finally {
						activeJobsCounterLock.unlock();
					}
				}

				/* now conduct node expansion */
				checkTermination();
				event = this.expandNextNode();

				/* if no event has occurred, still check whether a solution has arrived in the meantime prior to setting the algorithm state to inactive */
				if (event == null) {
					synchronized (pendingSolutionFoundEvents) {
						if (!this.pendingSolutionFoundEvents.isEmpty())
							event = pendingSolutionFoundEvents.poll();
						else {
							event = new AlgorithmFinishedEvent();
							logger.info("No event was returned and there are no pending solutions. Number of active jobs: {}. Setting state to inactive.", activeJobs.get());
						}
					}
				}
			} catch (TimeoutException e) {
				event = new AlgorithmFinishedEvent();
			}

			/* if the event is the finish event, shutdown */
			if (event instanceof AlgorithmFinishedEvent) {
				logger.info("Shutting down the search.");
				unregisterThreadAndShutdown();
			}
			if (!(event instanceof SolutionCandidateFoundEvent)) // solution events are sent via the super-class
				postEvent(event);
			return event;
		}
		default:
			throw new IllegalStateException("BestFirst search is in state " + getState() + " in which next must not be called!");
		}
	}

	public void selectNodeForNextExpansion(N node) throws InterruptedException {
		selectNodeForNextExpansion(ext2int.get(node));
	}

	@Override
	public void cancel() {
		super.cancel();
		this.logger.info("Set cancel flag to true.");
	}

	@SuppressWarnings("unchecked")
	public NodeExpansionJobSubmittedEvent<N, A, V> nextNodeExpansion() {
		while (hasNext()) {
			AlgorithmEvent e = next();
			if (e instanceof NodeExpansionJobSubmittedEvent)
				return (NodeExpansionJobSubmittedEvent<N, A, V>) e;
		}
		return null;
	}

	public EvaluatedSearchGraphPath<N, A, V> nextSolutionThatDominatesOpen() throws InterruptedException, AlgorithmExecutionCanceledException {
		EvaluatedSearchGraphPath<N, A, V> currentlyBestSolution = null;
		V currentlyBestScore = null;
		boolean loopCondition = true;
		while (loopCondition) {
			EvaluatedSearchGraphPath<N, A, V> solution = this.nextSolution();
			V scoreOfSolution = solution.getScore();
			if (currentlyBestScore == null || scoreOfSolution.compareTo(currentlyBestScore) < 0) {
				currentlyBestScore = scoreOfSolution;
				currentlyBestSolution = solution;
			}

			this.openLock.lockInterruptibly();
			try {
				loopCondition = this.open.peek().getInternalLabel().compareTo(currentlyBestScore) < 0;
			} finally {
				this.openLock.unlock();
			}
		}
		return currentlyBestSolution;
	}

	/** BLOCK C: Hooks **/

	protected void afterInitialization() {
	}

	protected boolean beforeSelection() {
		return true;
	}

	protected void afterSelection(final Node<N, V> node) {
	}

	protected void beforeExpansion(final Node<N, V> node) {
	}

	protected void afterExpansion(final Node<N, V> node) {
	}

	/** BLOCK D: Getters and Setters **/

	public List<N> getCurrentPathToNode(final N node) {
		return this.ext2int.get(node).externalPath();
	}

	public INodeEvaluator<N, V> getNodeEvaluator() {
		return this.nodeEvaluator;
	}

	public int getAdditionalThreadsForExpansion() {
		return this.additionalThreadsForNodeAttachment;
	}

	private void parallelizeNodeExpansion(final int threadsForExpansion) {
		if (this.pool != null) {
			throw new UnsupportedOperationException("The number of additional threads can be only set once per search!");
		}
		if (threadsForExpansion < 1) {
			throw new IllegalArgumentException("Number of threads should be at least 1 for " + this.getClass().getName());
		}
		this.additionalThreadsForNodeAttachment = threadsForExpansion;
		AtomicInteger counter = new AtomicInteger(0);
		this.pool = Executors.newFixedThreadPool(threadsForExpansion, r -> {
			Thread t = new Thread(r);
			t.setName("ORGraphSearch-worker-" + counter.incrementAndGet());
			return t;
		});
	}

	public int getTimeoutForComputationOfF() {
		return this.timeoutForComputationOfF;
	}

	public void setTimeoutForComputationOfF(final int timeoutInMS, final INodeEvaluator<N, V> timeoutEvaluator) {
		this.timeoutForComputationOfF = timeoutInMS;
		this.timeoutNodeEvaluator = timeoutEvaluator;
	}

	/**
	 * @return the openCollection
	 */
	public List<Node<N, V>> getOpen() {
		return Collections.unmodifiableList(new ArrayList<>(this.open));
	}

	public Node<N, V> getInternalRepresentationOf(N node) {
		return ext2int.get(node);
	}

	/**
	 * @param open
	 *            the openCollection to set
	 */
	public void setOpen(final Queue<Node<N, V>> collection) {
		this.openLock.lock();
		try {
			collection.clear();
			collection.addAll(this.open);
			this.open = collection;
		} finally {
			this.openLock.unlock();
		}
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger.info("Switching logger from {} to {}", this.logger.getName(), name);
		this.logger = LoggerFactory.getLogger(name);
		this.logger.info("Activated logger {} with name {}", name, this.logger.getName());
	}

	@Override
	public String getLoggerName() {
		return this.logger.getName();
	}

	public Queue<EvaluatedSearchGraphPath<N, A, V>> getSolutionQueue() {
		return this.solutions;
	}

	@Override
	public void setNumCPUs(final int numberOfCPUs) {
		this.config.setProperty(IAlgorithmConfig.K_CPUS, String.valueOf(numberOfCPUs));
	}

	@Override
	public int getNumCPUs() {
		return this.config.cpus();
	}

	@Override
	public void setTimeout(final int timeout, final TimeUnit timeUnit) {
		setTimeout(new TimeOut(timeout, timeUnit));	
	}
	
	@Override
	public void setTimeout(final TimeOut timeout) {
		this.config.setProperty(IAlgorithmConfig.K_TIMEOUT, String.valueOf(timeout.milliseconds()));
	}

	@Override
	public TimeOut getTimeout() {
		return new TimeOut(this.config.timeout(), TimeUnit.MILLISECONDS);
	}

	/**
	 * Check how many times a node was expanded.
	 *
	 * @return A counter of how many times a node was expanded.
	 */
	public int getExpandedCounter() {
		return this.expandedCounter;
	}

	public int getCreatedCounter() {
		return this.createdCounter;
	}

	public V getFValue(final N node) {
		return this.getFValue(this.ext2int.get(node));
	}

	public V getFValue(final Node<N, V> node) {
		return node.getInternalLabel();
	}

	public Map<String, Object> getNodeAnnotations(final N node) {
		Node<N, V> intNode = this.ext2int.get(node);
		return intNode.getAnnotations();
	}

	public Object getNodeAnnotation(final N node, final String annotation) {
		Node<N, V> intNode = this.ext2int.get(node);
		return intNode.getAnnotation(annotation);
	}

	@Override
	public EvaluatedSearchGraphPath<N, A, V> getSolutionProvidedToCall() {
		return getBestSeenSolution();
	}

}
