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
import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.events.AlgorithmInitializedEvent;
import jaicore.basic.algorithm.events.SolutionCandidateFoundEvent;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.basic.algorithm.exceptions.AlgorithmTimeoutedException;
import jaicore.concurrent.InterruptionTimerTask;
import jaicore.graphvisualizer.events.graph.GraphInitializedEvent;
import jaicore.graphvisualizer.events.graph.NodeAddedEvent;
import jaicore.graphvisualizer.events.graph.NodeParentSwitchEvent;
import jaicore.graphvisualizer.events.graph.NodeRemovedEvent;
import jaicore.graphvisualizer.events.graph.NodeTypeSwitchEvent;
import jaicore.logging.LoggerUtil;
import jaicore.logging.ToJSONStringUtil;
import jaicore.search.algorithms.standard.bestfirst.events.EvaluatedSearchSolutionCandidateFoundEvent;
import jaicore.search.algorithms.standard.bestfirst.events.NodeAnnotationEvent;
import jaicore.search.algorithms.standard.bestfirst.events.NodeExpansionCompletedEvent;
import jaicore.search.algorithms.standard.bestfirst.events.NodeExpansionJobSubmittedEvent;
import jaicore.search.algorithms.standard.bestfirst.events.RemovedGoalNodeFromOpenEvent;
import jaicore.search.algorithms.standard.bestfirst.events.RolloutEvent;
import jaicore.search.algorithms.standard.bestfirst.events.SolutionAnnotationEvent;
import jaicore.search.algorithms.standard.bestfirst.events.SuccessorComputationCompletedEvent;
import jaicore.search.algorithms.standard.bestfirst.exceptions.ControlledNodeEvaluationException;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.DecoratingNodeEvaluator;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.ICancelableNodeEvaluator;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.IPotentiallyGraphDependentNodeEvaluator;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.IPotentiallySolutionReportingNodeEvaluator;
import jaicore.search.core.interfaces.AOptimalPathInORGraphSearch;
import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.model.other.EvaluatedSearchGraphPath;
import jaicore.search.model.travesaltree.Node;
import jaicore.search.model.travesaltree.NodeExpansionDescription;
import jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;
import jaicore.search.structure.graphgenerator.MultipleRootGenerator;
import jaicore.search.structure.graphgenerator.NodeGoalTester;
import jaicore.search.structure.graphgenerator.PathGoalTester;
import jaicore.search.structure.graphgenerator.RootGenerator;
import jaicore.search.structure.graphgenerator.SingleRootGenerator;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;

/**
 *
 * @author fmohr, wever
 *
 * @param <I>
 * @param <N>
 * @param <A>
 * @param <V>
 */
public class BestFirst<I extends GraphSearchWithSubpathEvaluationsInput<N, A, V>, N, A, V extends Comparable<V>> extends AOptimalPathInORGraphSearch<I, N, A, V> {

	private Logger logger = LoggerFactory.getLogger(BestFirst.class);
	private String loggerName;

	private static final String SPACER = "\n\t\t";

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
	private final List<NodeExpansionDescription<N, A>> lastExpansion = new ArrayList<>();
	protected final Queue<EvaluatedSearchGraphPath<N, A, V>> solutions = new LinkedBlockingQueue<>();
	protected final Queue<EvaluatedSearchSolutionCandidateFoundEvent<N, A, V>> pendingSolutionFoundEvents = new LinkedBlockingQueue<>();

	/* communication */
	protected final Map<N, Node<N, V>> ext2int = new ConcurrentHashMap<>();

	/* search graph model */
	protected Queue<Node<N, V>> open = new PriorityQueue<>((n1, n2) -> n1.getInternalLabel().compareTo(n2.getInternalLabel()));
	private Node<N, V> nodeSelectedForExpansion; // the node that will be expanded next
	private final Map<N, Thread> expanding = new HashMap<>(); // EXPANDING contains the nodes being expanded and the threads doing this job
	private final Set<N> closed = new HashSet<>(); // CLOSED contains only node but not paths

	/* parallelization */
	protected int additionalThreadsForNodeAttachment = 0;
	private ExecutorService pool;
	protected final AtomicInteger activeJobs = new AtomicInteger(0); // this is the number of jobs for which a worker is currently running in the pool
	private final Lock activeJobsCounterLock = new ReentrantLock(); // lock that has to be locked before accessing the open queue
	private final Lock openLock = new ReentrantLock(); // lock that has to be locked before accessing the open queue
	private final Lock nodeSelectionLock = new ReentrantLock(true);
	private final Condition numberOfActiveJobsHasChanged = this.activeJobsCounterLock.newCondition(); // condition that is signaled whenever a node is added to the open queue

	public BestFirst(final I problem) {
		this(ConfigFactory.create(IBestFirstConfig.class), problem);
	}

	public BestFirst(final IBestFirstConfig config, final I problem) {
		super(config, problem);
		this.graphGenerator = problem.getGraphGenerator();
		this.rootGenerator = this.graphGenerator.getRootGenerator();
		this.successorGenerator = this.graphGenerator.getSuccessorGenerator();
		this.checkGoalPropertyOnEntirePath = !(this.graphGenerator.getGoalTester() instanceof NodeGoalTester);
		if (this.checkGoalPropertyOnEntirePath) {
			this.nodeGoalTester = null;
			this.pathGoalTester = (PathGoalTester<N>) this.graphGenerator.getGoalTester();
		} else {
			this.nodeGoalTester = (NodeGoalTester<N>) this.graphGenerator.getGoalTester();
			this.pathGoalTester = null;
		}

		/* if the node evaluator is graph dependent, communicate the generator to it */
		this.nodeEvaluator = problem.getNodeEvaluator();
		if (this.nodeEvaluator == null) {
			throw new IllegalArgumentException("Cannot work with node evaulator that is null");
		} else if (this.nodeEvaluator instanceof DecoratingNodeEvaluator<?, ?>) {
			DecoratingNodeEvaluator<N, V> castedEvaluator = (DecoratingNodeEvaluator<N, V>) this.nodeEvaluator;
			if (castedEvaluator.requiresGraphGenerator()) {
				this.logger.info("{} is a graph dependent node evaluator. Setting its graph generator now ...", castedEvaluator);
				castedEvaluator.setGenerator(this.graphGenerator);
			}
			if (castedEvaluator.reportsSolutions()) {
				this.logger.info("{} is a solution reporter. Register the search algo in its event bus", castedEvaluator);
				castedEvaluator.registerSolutionListener(this);
				this.solutionReportingNodeEvaluator = true;
			} else {
				this.solutionReportingNodeEvaluator = false;
			}
		} else {
			if (this.nodeEvaluator instanceof IPotentiallyGraphDependentNodeEvaluator) {
				this.logger.info("{} is a graph dependent node evaluator. Setting its graph generator now ...", this.nodeEvaluator);
				((IPotentiallyGraphDependentNodeEvaluator<N, V>) this.nodeEvaluator).setGenerator(this.graphGenerator);
			}

			/* if the node evaluator is a solution reporter, register in his event bus */
			if (this.nodeEvaluator instanceof IPotentiallySolutionReportingNodeEvaluator) {
				this.logger.info("{} is a solution reporter. Register the search algo in its event bus", this.nodeEvaluator);
				((IPotentiallySolutionReportingNodeEvaluator<N, V>) this.nodeEvaluator).registerSolutionListener(this);
				this.solutionReportingNodeEvaluator = true;
			} else {
				this.solutionReportingNodeEvaluator = false;
			}
		}
		this.cancelableNodeEvaluator = this.nodeEvaluator instanceof ICancelableNodeEvaluator;

		/*
		 * add shutdown hook so as to cancel the search once the overall program is
		 * shutdown
		 */
		Runtime.getRuntime().addShutdownHook(new Thread(() -> BestFirst.this.cancel(), "Shutdown hook thread for " + BestFirst.this));
	}

	/** BLOCK A: Internal behavior of the algorithm **/

	private enum ENodeType {

		OR_PRUNED("or_pruned"), OR_TIMEDOUT("or_timedout"), OR_OPEN("or_open"), OR_SOLUTION("or_solution"), OR_CREATED("or_created"), OR_CLOSED("or_closed");

		private String name;

		private ENodeType(final String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return this.name;
		}
	}

	private enum ENodeAnnotation {
		F_ERROR("fError"), F_MESSAGE("fMessage"), F_TIME("fTime");

		private String name;

		private ENodeAnnotation(final String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return this.name;
		}
	}

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
					BestFirst.this.post(new NodeExpansionCompletedEvent<>(BestFirst.this.getId(), this.expandedNodeInternal));
				}
			}
		}

		@Override
		public void run() {
			try {
				if (BestFirst.this.isStopCriterionSatisfied()) {
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
					BestFirst.this.labelNode(newNode);
					if (newNode.getInternalLabel() == null) {
						BestFirst.this.post(new NodeTypeSwitchEvent<>(BestFirst.this.getId(), newNode, ENodeType.OR_PRUNED.toString()));
						return;
					}
					if (BestFirst.this.isStopCriterionSatisfied()) {
						this.communicateJobFinished();
						return;
					}
				} catch (InterruptedException e) {
					if (!BestFirst.this.isShutdownInitialized()) {
						BestFirst.this.logger.warn("Leaving node building routine due to interrupt. This leaves the search inconsistent; the node should be attached again!");
					}
					BestFirst.this.logger.debug("Worker has been interrupted, exiting.");
					BestFirst.this.post(new NodeAnnotationEvent<>(BestFirst.this.getId(), newNode, ENodeAnnotation.F_ERROR.toString(), e));
					BestFirst.this.post(new NodeTypeSwitchEvent<>(BestFirst.this.getId(), newNode, ENodeType.OR_PRUNED.toString()));
					Thread.currentThread().interrupt();
					return;
				} catch (TimeoutException e) {
					BestFirst.this.logger.debug("Node evaluation of {} has timed out.", newNode);
					newNode.setAnnotation(ENodeAnnotation.F_ERROR.toString(), e);
					BestFirst.this.post(new NodeAnnotationEvent<>(BestFirst.this.getId(), newNode, ENodeAnnotation.F_ERROR.toString(), e));
					BestFirst.this.post(new NodeTypeSwitchEvent<>(BestFirst.this.getId(), newNode, ENodeType.OR_TIMEDOUT.toString()));
					return;
				} catch (ControlledNodeEvaluationException e) {
					BestFirst.this.logger.debug("Node evaluation failed with a controlled exception.");
					newNode.setAnnotation(ENodeAnnotation.F_ERROR.toString(), e);
					BestFirst.this.post(new NodeAnnotationEvent<>(BestFirst.this.getId(), newNode, ENodeAnnotation.F_ERROR.toString(), e));
					BestFirst.this.post(new NodeAnnotationEvent<>(BestFirst.this.getId(), newNode, ENodeAnnotation.F_MESSAGE.toString(), "hello, yes it is pruned!"));
					BestFirst.this.post(new NodeTypeSwitchEvent<>(BestFirst.this.getId(), newNode, ENodeType.OR_PRUNED.toString()));

					return;
				} catch (Exception e) {
					BestFirst.this.logger.debug("Observed an exception during computation of f:\n{}", LoggerUtil.getExceptionInfo(e));
					newNode.setAnnotation(ENodeAnnotation.F_ERROR.toString(), e);
					BestFirst.this.post(new NodeAnnotationEvent<>(BestFirst.this.getId(), newNode, ENodeAnnotation.F_ERROR.toString(), e));
					BestFirst.this.post(new NodeTypeSwitchEvent<>(BestFirst.this.getId(), newNode, ENodeType.OR_PRUNED.toString()));
					return;
				}

				/* depending on the algorithm setup, now decide how to proceed with the node */

				/* if we discard (either only on OPEN or on both OPEN and CLOSED) */
				boolean nodeProcessed = false;
				if (BestFirst.this.getConfig().parentDiscarding() != ParentDiscarding.NONE) {
					BestFirst.this.openLock.lockInterruptibly();
					try {

						/* determine whether we already have the node AND it is worse than the one we want to insert */
						Optional<Node<N, V>> existingIdenticalNodeOnOpen = BestFirst.this.open.stream().filter(n -> n.getPoint().equals(newNode.getPoint())).findFirst();
						if (existingIdenticalNodeOnOpen.isPresent()) {
							Node<N, V> existingNode = existingIdenticalNodeOnOpen.get();
							if (newNode.getInternalLabel().compareTo(existingNode.getInternalLabel()) < 0) {
								BestFirst.this.post(new NodeTypeSwitchEvent<>(BestFirst.this.getId(), newNode, (newNode.isGoal() ? ENodeType.OR_SOLUTION.toString() : ENodeType.OR_OPEN.toString())));
								BestFirst.this.post(new NodeRemovedEvent<>(BestFirst.this.getId(), existingNode));
								BestFirst.this.open.remove(existingNode);
								if (newNode.getInternalLabel() == null) {
									throw new IllegalArgumentException("Cannot insert nodes with value NULL into OPEN!");
								}
								BestFirst.this.open.add(newNode);
							} else {
								BestFirst.this.post(new NodeRemovedEvent<>(BestFirst.this.getId(), newNode));
							}
							nodeProcessed = true;
						}

						/*
						 * if parent discarding is not only for OPEN but also for CLOSE (and the node
						 * was not on OPEN), check the list of expanded nodes
						 */
						else if (BestFirst.this.getConfig().parentDiscarding() == ParentDiscarding.ALL) {
							/* reopening, if the node is already on CLOSED */
							Optional<N> existingIdenticalNodeOnClosed = BestFirst.this.closed.stream().filter(n -> n.equals(newNode.getPoint())).findFirst();
							if (existingIdenticalNodeOnClosed.isPresent()) {
								Node<N, V> node = BestFirst.this.ext2int.get(existingIdenticalNodeOnClosed.get());
								if (newNode.getInternalLabel().compareTo(node.getInternalLabel()) < 0) {
									node.setParent(newNode.getParent());
									node.setInternalLabel(newNode.getInternalLabel());
									BestFirst.this.closed.remove(node.getPoint());
									BestFirst.this.open.add(node);
									BestFirst.this.post(new NodeParentSwitchEvent<Node<N, V>>(BestFirst.this.getId(), node, node.getParent(), newNode.getParent()));
								}
								BestFirst.this.post(new NodeRemovedEvent<Node<N, V>>(BestFirst.this.getId(), newNode));
								nodeProcessed = true;
							}
						}
					} finally {
						BestFirst.this.openLock.unlock();
					}
				}

				/*
				 * if parent discarding is turned off OR if the node was node processed by a
				 * parent discarding rule, just insert it on OPEN
				 */
				if (!nodeProcessed) {
					if (!newNode.isGoal()) {
						BestFirst.this.openLock.lockInterruptibly();
						synchronized (BestFirst.this.expanding) {
							try {
								assert !BestFirst.this.closed.contains(newNode.getPoint()) : "Currently only tree search is supported. But now we add a node to OPEN whose point has already been expanded before.";
								BestFirst.this.expanding.keySet().forEach(node -> {
									assert !node.equals(newNode.getPoint()) : Thread.currentThread() + " cannot add node to OPEN that is currently being expanded by " + BestFirst.this.expanding.get(node) + ".\n\tFrom: "
											+ newNode.getParent().getPoint() + "\n\tTo: " + node;
								});
								if (newNode.getInternalLabel() == null) {
									throw new IllegalArgumentException("Cannot insert nodes with value NULL into OPEN!");
								}
								BestFirst.this.logger.debug("Inserting successor {} of {} to OPEN. F-Value is {}", newNode, this.expandedNodeInternal, newNode.getInternalLabel());
								BestFirst.this.open.add(newNode);
							} finally {
								BestFirst.this.openLock.unlock();
							}
						}
					}
					BestFirst.this.post(new NodeTypeSwitchEvent<>(BestFirst.this.getId(), newNode, (newNode.isGoal() ? ENodeType.OR_SOLUTION.toString() : ENodeType.OR_OPEN.toString())));
					BestFirst.this.createdCounter++;
				}

				/* Recognize solution in cache together with annotation */
				if (newNode.isGoal()) {
					EvaluatedSearchGraphPath<N, A, V> solution = new EvaluatedSearchGraphPath<>(newNode.externalPath(), null, newNode.getInternalLabel());

					/*
					 * if the node evaluator has not reported the solution already anyway, register
					 * the solution
					 */
					if (!BestFirst.this.solutionReportingNodeEvaluator) {
						BestFirst.this.registerSolution(solution);
					}
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt(); // interrupt myself. This is for the case that the main thread executes this part
				BestFirst.this.logger.info("Node builder has been interrupted, finishing execution.");
			} catch (Exception e) {
				BestFirst.this.logger.error("An unexpected exception occurred while building nodes", e);
			} finally {

				/*
				 * free resources if this is computed by helper threads and notify the listeners
				 */
				assert !Thread.holdsLock(BestFirst.this.openLock) : "Node Builder must not hold a lock on OPEN when locking the active jobs counter";
				BestFirst.this.logger.debug("Trying to decrement active jobs by one.");
				BestFirst.this.logger.trace("Waiting for lock to become free.");
				BestFirst.this.activeJobsCounterLock.lock(); // cannot be interruptible without opening more cases
				BestFirst.this.logger.trace("Acquired activeJobsCounterLock for decrement.");
				try {
					if (BestFirst.this.pool != null) {
						BestFirst.this.activeJobs.decrementAndGet();
						BestFirst.this.logger.trace("Decremented job counter.");
					}
				} finally {
					BestFirst.this.numberOfActiveJobsHasChanged.signalAll();
					BestFirst.this.activeJobsCounterLock.unlock();
					BestFirst.this.logger.trace("Released activeJobsCounterLock after decrement.");
				}
				this.communicateJobFinished();
				BestFirst.this.logger.debug("Builder exits. Interrupt-flag is {}", Thread.currentThread().isInterrupted());
			}
		}
	}

	protected Node<N, V> newNode(final Node<N, V> parent, final N t2) throws InterruptedException {
		return this.newNode(parent, t2, null);
	}

	protected Node<N, V> newNode(final Node<N, V> parent, final N t2, final V evaluation) throws InterruptedException {
		this.openLock.lockInterruptibly();
		try {
			assert !this.open.contains(parent) : "Parent node " + parent + " is still on OPEN, which must not be the case! OPEN class: " + this.open.getClass().getName() + ". OPEN size: " + this.open.size();
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
				+ newNode.externalPath().stream().map(N::toString).reduce("", (s, t) -> s + SPACER + t);

		/* currently, we only support tree search */
		assert !this.ext2int.containsKey(t2) : "Reached node " + t2 + " for the second time.\nt\tFirst path:" + this.ext2int.get(t2).externalPath().stream().map(n -> n + "").reduce("", (s, t) -> s + SPACER + t) + "\n\tSecond Path:"
		+ newNode.externalPath().stream().map(N::toString).reduce("", (s, t) -> s + SPACER + t);

		/* register node in map and create annotation object */
		this.ext2int.put(t2, newNode);

		/* detect whether node is solution */
		if (this.checkGoalPropertyOnEntirePath ? this.pathGoalTester.isGoal(newNode.externalPath()) : this.nodeGoalTester.isGoal(newNode.getPoint())) {
			newNode.setGoal(true);
		}

		/* send events for this new node */
		if (parent == null) {
			this.post(new GraphInitializedEvent<Node<N, V>>(this.getId(), newNode));
		} else {
			this.post(new NodeAddedEvent<Node<N, V>>(this.getId(), parent, newNode, (newNode.isGoal() ? ENodeType.OR_SOLUTION.toString() : ENodeType.OR_CREATED.toString())));
			this.logger.debug("Sent message for creation of node {} as a successor of {}", newNode, parent);
		}
		return newNode;
	}

	protected void labelNode(final Node<N, V> node) throws AlgorithmTimeoutedException, AlgorithmExecutionCanceledException, InterruptedException, AlgorithmException {

		/* define timeouter for label computation */
		this.logger.trace("Computing node label for {}", node);
		InterruptionTimerTask interruptionTask = null;
		AtomicBoolean timedout = new AtomicBoolean(false);
		if (BestFirst.this.timeoutForComputationOfF > 0) {
			interruptionTask = new InterruptionTimerTask("Timeout for Node-Labeling in " + BestFirst.this, Thread.currentThread(), () -> timedout.set(true));
			this.logger.debug("Scheduling timeout for f-value computation. Allowed time: {}ms", this.timeoutForComputationOfF);
			this.getTimerAndCreateIfNotExistent().schedule(interruptionTask, this.timeoutForComputationOfF);
		}

		/* compute f */
		V label = null;
		boolean computationTimedout = false;
		long startComputation = System.currentTimeMillis();
		try {
			this.logger.trace("Calling f-function of node evaluator for {}", node);
			label = this.computeTimeoutAware(() -> BestFirst.this.nodeEvaluator.f(node));
			this.logger.trace("Determined f-value of {}", label);
			if (this.isStopCriterionSatisfied()) {
				return;
			}

			/* check whether the required time exceeded the timeout */
			long fTime = System.currentTimeMillis() - startComputation;
			if (BestFirst.this.timeoutForComputationOfF > 0 && fTime > BestFirst.this.timeoutForComputationOfF + 1000) {
				BestFirst.this.logger.warn("Computation of f for node {} took {}ms, which is more than the allowed {}ms", node, fTime, BestFirst.this.timeoutForComputationOfF);
			}
		} catch (InterruptedException e) {
			this.logger.info("Thread {} received interrupt in node evaluation. Timeout flag is {}", Thread.currentThread(), timedout.get());
			if (timedout.get()) {
				BestFirst.this.logger.debug("Received interrupt during computation of f.");
				this.post(new NodeTypeSwitchEvent<>(this.getId(), node, ENodeType.OR_TIMEDOUT.toString()));
				node.setAnnotation(ENodeAnnotation.F_ERROR.toString(), "Timeout");
				computationTimedout = true;
				Thread.interrupted(); // set interrupt state of thread to FALSE, because interrupt
				try {
					label = BestFirst.this.timeoutNodeEvaluator != null ? BestFirst.this.timeoutNodeEvaluator.f(node) : null;
				} catch (Exception e2) {
					this.logger.error("An unexpected exception occurred while labeling node {}", node, e2);
				}
			} else {
				this.logger.info("Received external interrupt. Forwarding this interrupt.");
				throw e;
			}
		}
		if (interruptionTask != null) {
			interruptionTask.cancel();
		}

		/* register time required to compute this node label */
		long fTime = System.currentTimeMillis() - startComputation;
		node.setAnnotation(ENodeAnnotation.F_TIME.toString(), fTime);
		this.logger.debug("Computed label {} for {} in {}ms", label, node, fTime);

		/* if no label was computed, prune the node and cancel the computation */
		if (label == null) {
			if (!computationTimedout) {
				BestFirst.this.logger.debug("Not inserting node {} since its label is missing!", node);
			} else {
				BestFirst.this.logger.debug("Not inserting node {} because computation of f-value timed out.", node);
			}
			if (!node.getAnnotations().containsKey(ENodeAnnotation.F_ERROR.toString())) {
				node.setAnnotation(ENodeAnnotation.F_ERROR.toString(), "f-computer returned NULL");
			}
			return;
		}

		/* eventually set the label */
		node.setInternalLabel(label);
		assert node.getInternalLabel() != null : "Node label must not be NULL";
	}

	/**
	 * This method setups the graph by inserting the root nodes.
	 *
	 * @throws InterruptedException
	 * @throws AlgorithmExecutionCanceledException
	 * @throws TimeoutException
	 * @throws AlgorithmException
	 */
	protected void initGraph() throws AlgorithmTimeoutedException, AlgorithmExecutionCanceledException, InterruptedException, AlgorithmException {
		if (!this.initialized) {
			this.initialized = true;
			if (this.rootGenerator instanceof MultipleRootGenerator) {
				for (N n0 : ((MultipleRootGenerator<N>) this.rootGenerator).getRoots()) {
					Node<N, V> root = this.newNode(null, n0);
					this.labelNode(root);
					this.checkAndConductTermination();
					this.openLock.lockInterruptibly();
					try {
						if (root == null) {
							throw new IllegalArgumentException("Root for MultipleRootGenerator is null. Cannot add NULL as a node to OPEN");
						}
						this.open.add(root);
					} finally {
						this.openLock.unlock();
					}
					this.logger.debug("Labeled root with {}", root.getInternalLabel());
				}
			} else {
				Node<N, V> root = this.newNode(null, ((SingleRootGenerator<N>) this.rootGenerator).getRoot());
				if (root == null) {
					throw new IllegalArgumentException("Root for SingleRootGenerator is null. Cannot add NULL as a node to OPEN");
				}
				this.labelNode(root);
				this.checkAndConductTermination();
				if (root.getInternalLabel() == null) {
					throw new IllegalArgumentException("The node evaluator has assigned NULL to the root node, which impedes an initialization of the search graph. Node evaluator: " + this.nodeEvaluator);
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

	protected void selectNodeForNextExpansion(final Node<N, V> node) throws InterruptedException {
		assert node != null : "Cannot select node NULL for expansion!";
		this.nodeSelectionLock.lockInterruptibly();
		try {
			this.openLock.lockInterruptibly();
			try {
				assert !this.open.contains(null) : "OPEN contains NULL";
				assert this.open.stream().noneMatch(n -> n.getInternalLabel() == null) : "OPEN contains an element with value NULL";
				int openSizeBefore = this.open.size();
				assert this.nodeSelectedForExpansion == null : "Node selected for expansion must be NULL when setting it!";
				this.nodeSelectedForExpansion = node;
				assert this.open.contains(node) : "OPEN must contain the node to be expanded.\n\tOPEN size: " + this.open.size() + "\n\tNode to be expanded: " + node + ".\n\tOPEN: "
				+ this.open.stream().map(n -> SPACER + n).collect(Collectors.joining());
				this.open.remove(this.nodeSelectedForExpansion);
				int openSizeAfter = this.open.size();
				assert this.ext2int.containsKey(this.nodeSelectedForExpansion.getPoint()) : "A node chosen for expansion has no entry in the ext2int map!";
				assert openSizeAfter == openSizeBefore - 1 : "OPEN size must descrease by one when selecting node for expansion";
			} finally {
				this.openLock.unlock();
			}
		} finally {
			this.nodeSelectionLock.unlock();
		}
	}

	/**
	 * This method conducts the expansion of the next node. Unless the next node has been selected from outside, it selects the first node on OPEN (if OPEN is empty but active jobs are running, it waits until those terminate)
	 *
	 * @return
	 * @throws InterruptedException
	 * @throws AlgorithmExecutionCanceledException
	 * @throws TimeoutException
	 * @throws AlgorithmException
	 */
	protected AlgorithmEvent expandNextNode() throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException, AlgorithmException {

		/*
		 * Preliminarily check that the active jobs are less than the additional threads
		 */
		assert this.additionalThreadsForNodeAttachment == 0 || this.activeJobs.get() < this.additionalThreadsForNodeAttachment : "Cannot expand nodes if number of active jobs (" + this.activeJobs.get()
		+ " is at least as high as the threads available for node attachment (" + this.additionalThreadsForNodeAttachment + ")";

		/*
		 * Step 1: determine node that will be expanded next. Either it already has been
		 * set or it will be the first of OPEN. If necessary, we wait for potential
		 * incoming nodes
		 */
		final Node<N, V> actualNodeSelectedForExpansion;
		{
			Node<N, V> tmpNodeSelectedForExpansion = null; // necessary workaround as setting final variables in a try-block is not reasonably possible
			this.nodeSelectionLock.lockInterruptibly();
			try {
				if (this.nodeSelectedForExpansion == null) {
					this.activeJobsCounterLock.lockInterruptibly();
					this.logger.trace("Acquired activeJobsCounterLock for read");
					try {
						this.logger.debug("No next node has been selected. Choosing the first from OPEN.");
						while (this.open.isEmpty() && this.activeJobs.get() > 0) {
							this.logger.trace("Await condition as open queue is empty and active jobs is {} ...", this.activeJobs.get());
							this.numberOfActiveJobsHasChanged.await();
							this.logger.trace("Got signaled");
							this.checkAndConductTermination();
						}
						this.openLock.lock();
						try {
							if (this.open.isEmpty()) {
								return null;
							}
							this.selectNodeForNextExpansion(this.open.peek());
						} finally {
							this.openLock.unlock();
						}
					} finally {
						this.activeJobsCounterLock.unlock();
						this.logger.trace("Released activeJobsCounterLock after read");
					}
				}
				assert this.nodeSelectedForExpansion != null : "We have not selected any node for expansion, but this must be the case at this point.";
				tmpNodeSelectedForExpansion = this.nodeSelectedForExpansion;
				this.nodeSelectedForExpansion = null;
			} finally {
				this.nodeSelectionLock.unlock();
			}
			assert this.nodeSelectedForExpansion == null : "The object variable for the next selected node must be NULL at the end of the select step.";
			actualNodeSelectedForExpansion = tmpNodeSelectedForExpansion;
			synchronized (this.expanding) {
				this.expanding.put(actualNodeSelectedForExpansion.getPoint(), Thread.currentThread());
				assert this.expanding.keySet().contains(tmpNodeSelectedForExpansion.getPoint()) : "The node selected for expansion should be in the EXPANDING map by now.";
			}
			assert !this.open.contains(actualNodeSelectedForExpansion) : "Node selected for expansion is still on OPEN";
			assert actualNodeSelectedForExpansion != null : "We have not selected any node for expansion, but this must be the case at this point.";
			this.checkTerminationAndUnregisterFromExpand(actualNodeSelectedForExpansion);
		}

		/* steps 2 and 3 only for non-goal nodes */
		AlgorithmEvent expansionEvent;
		if (!actualNodeSelectedForExpansion.isGoal()) {

			/* Step 2: compute the successors in the underlying graph */
			this.beforeExpansion(actualNodeSelectedForExpansion);
			this.post(new NodeTypeSwitchEvent<Node<N, V>>(this.getId(), actualNodeSelectedForExpansion, "or_expanding"));
			this.logger.debug("Expanding node {} with f-value {}", actualNodeSelectedForExpansion, actualNodeSelectedForExpansion.getInternalLabel());
			this.logger.debug("Start computation of successors");
			final List<NodeExpansionDescription<N, A>> successorDescriptions;
			List<NodeExpansionDescription<N, A>> tmpSuccessorDescriptions = null;
			try {
				assert !actualNodeSelectedForExpansion.isGoal() : "Goal nodes must not be expanded!";
				tmpSuccessorDescriptions = this.computeTimeoutAware(() -> {
					this.logger.trace("Invoking getSuccessors");
					return BestFirst.this.successorGenerator.generateSuccessors(actualNodeSelectedForExpansion.getPoint());
				});
				assert tmpSuccessorDescriptions != null : "Successor descriptions must never be null!";
				this.logger.trace("Received {} successor descriptions", tmpSuccessorDescriptions.size());
			} catch (Exception e) {
				this.checkTerminationAndUnregisterFromExpand(actualNodeSelectedForExpansion); // make sure that we unregister from expand
				throw new AlgorithmException(e, "Exception occured in successor generation."); // if this was a real exception, throw it.
			}
			successorDescriptions = tmpSuccessorDescriptions;

			this.checkTerminationAndUnregisterFromExpand(actualNodeSelectedForExpansion);
			this.logger.debug("Finished computation of successors. Sending SuccessorComputationCompletedEvent with {} successors for {}", successorDescriptions.size(), actualNodeSelectedForExpansion);
			this.post(new SuccessorComputationCompletedEvent<>(this.getId(), actualNodeSelectedForExpansion, successorDescriptions));

			/*
			 * step 3: trigger node builders that compute node details and decide whether
			 * and how to integrate the successors into the search
			 */
			List<N> todoList = successorDescriptions.stream().map(d -> d.getTo()).collect(Collectors.toList());
			for (NodeExpansionDescription<N, A> successorDescription : successorDescriptions) {
				NodeBuilder nb = new NodeBuilder(todoList, actualNodeSelectedForExpansion, successorDescription);
				this.logger.trace("Number of additional threads for node attachment is {}", this.additionalThreadsForNodeAttachment);
				if (this.additionalThreadsForNodeAttachment < 1) {
					nb.run();
				} else {
					this.lockConditionSafeleyWhileExpandingNode(this.activeJobsCounterLock, actualNodeSelectedForExpansion); // acquires the lock and shuts down properly when being interrupted
					this.logger.trace("Acquired activeJobsCounterLock for increment");
					try {
						this.activeJobs.incrementAndGet();
					} finally {
						this.numberOfActiveJobsHasChanged.signalAll();
						this.activeJobsCounterLock.unlock();
						this.logger.trace("Released activeJobsCounterLock after increment");
					}
					if (this.isShutdownInitialized()) {
						break;
					}
					this.pool.submit(nb);
				}
			}
			this.logger.debug("Finished expansion of node {}. Size of OPEN is now {}. Number of active jobs is {}", actualNodeSelectedForExpansion, this.open.size(), this.activeJobs.get());
			this.checkTerminationAndUnregisterFromExpand(actualNodeSelectedForExpansion);
			expansionEvent = new NodeExpansionJobSubmittedEvent<>(this.getId(), actualNodeSelectedForExpansion, successorDescriptions);
		} else {
			expansionEvent = new RemovedGoalNodeFromOpenEvent<>(this.getId(), actualNodeSelectedForExpansion);
		}

		/*
		 * step 4: update statistics, send closed notifications, and possibly return a
		 * solution
		 */
		this.expandedCounter++;
		synchronized (this.expanding) {
			this.expanding.remove(actualNodeSelectedForExpansion.getPoint());
			assert !this.expanding.containsKey(actualNodeSelectedForExpansion.getPoint()) : actualNodeSelectedForExpansion + " was expanded and it was not removed from EXPANDING!";
		}
		this.closed.add(actualNodeSelectedForExpansion.getPoint());
		assert this.closed.contains(actualNodeSelectedForExpansion.getPoint()) : "Expanded node " + actualNodeSelectedForExpansion + " was not inserted into CLOSED!";
		this.post(new NodeTypeSwitchEvent<Node<N, V>>(this.getId(), actualNodeSelectedForExpansion, ENodeType.OR_CLOSED.toString()));
		this.afterExpansion(actualNodeSelectedForExpansion);
		this.checkAndConductTermination();
		this.openLock.lockInterruptibly();
		try {
			this.logger.debug("Step ends. Size of OPEN now {}", this.open.size());
		} finally {
			this.openLock.unlock();
		}
		return expansionEvent;
	}

	@Override
	protected EvaluatedSearchSolutionCandidateFoundEvent<N, A, V> registerSolution(final EvaluatedSearchGraphPath<N, A, V> solutionPath) {
		EvaluatedSearchSolutionCandidateFoundEvent<N, A, V> solutionEvent = super.registerSolution(solutionPath); // this emits an event on the event bus
		assert !this.solutions.contains(solutionEvent.getSolutionCandidate()) : "Registering solution " + solutionEvent.getSolutionCandidate() + " for the second time!";
		this.solutions.add(solutionEvent.getSolutionCandidate());
		synchronized (this.pendingSolutionFoundEvents) {
			this.pendingSolutionFoundEvents.add(solutionEvent);
		}
		return solutionEvent;
	}

	private void lockConditionSafeleyWhileExpandingNode(final Lock l, final Node<N, V> node) throws AlgorithmTimeoutedException, AlgorithmExecutionCanceledException, InterruptedException {
		try {
			l.lockInterruptibly();
		} catch (InterruptedException e) { // if we are interrupted during a wait, we must still conduct a controlled shutdown
			this.logger.debug("Received an interrupt while waiting for {} to become available.", l);
			Thread.currentThread().interrupt();
			this.checkTerminationAndUnregisterFromExpand(node);
		}
	}

	private void unregisterFromExpand(final Node<N, V> node) {
		assert this.expanding.containsKey(node.getPoint()) : "Cannot unregister a node that is not being expanded currently";
		assert this.expanding.get(node.getPoint()) == Thread.currentThread() : "Thread " + Thread.currentThread() + " cannot unregister other thread " + this.expanding.get(node.getPoint()) + " from expansion map!";
		this.logger.debug("Removing {} from EXPANDING.", node.getPoint());
		this.expanding.remove(node.getPoint());
	}

	/**
	 * This is a small extension of the checkTermination method that makes sure that the current thread is not counted as a worker for an expanding node. This is important to make sure that the thread does not interrupt itself on a shutdown
	 *
	 * @throws TimeoutException
	 * @throws AlgorithmExecutionCanceledException
	 * @throws InterruptedException
	 */
	private void checkTerminationAndUnregisterFromExpand(final Node<N, V> node) throws AlgorithmTimeoutedException, AlgorithmExecutionCanceledException, InterruptedException {
		if (this.isStopCriterionSatisfied()) {
			this.unregisterFromExpand(node);
			assert !this.expanding.containsKey(node.getPoint()) : "Expanded node " + this.nodeSelectedForExpansion + " was not removed from EXPANDING!";
		}
		super.checkAndConductTermination();
	}

	@Override
	protected void shutdown() {

		if (this.isShutdownInitialized()) {
			return;
		}

		/* set state to inactive */
		this.logger.info("Invoking shutdown routine ...");
		this.logger.debug("First conducting general algorithm shutdown routine ...");
		super.shutdown();
		this.logger.debug("General algorithm shutdown routine completed. Now conducting BestFirst-specific shutdown activities.");

		/* interrupt the expanding threads */
		this.logger.debug("Interrupting {} active expansion threads.", this.expanding.size());
		synchronized (this.expanding) {
			for (Thread t : this.expanding.values()) {
				this.interruptThreadAsPartOfShutdown(t);
			}
		}

		/* cancel ongoing work */
		if (this.additionalThreadsForNodeAttachment > 0) {
			this.logger.debug("Shutting down worker pool.");
			if (this.pool != null) {
				this.logger.info("Triggering shutdown of builder thread pool with interrupt");
				this.pool.shutdownNow();
			}
			try {
				this.logger.debug("Waiting 3 seconds for pool shutdown.");
				if (this.pool != null) {
					this.pool.awaitTermination(3, TimeUnit.DAYS);
				} else {
					this.logger.error("Apparently, the pool was unexpectedly not set and thus null.");
				}
			} catch (InterruptedException e) {
				this.logger.warn("Got interrupted during shutdown!", e);
			}
			if (this.pool != null) {
				assert this.pool.isTerminated() : "The worker pool has not been shutdown correctly!";
				if (!this.pool.isTerminated()) {
					this.logger.error("Worker pool has not been shutdown correctly!");
				} else {
					this.logger.info("Worker pool has been shut down.");
				}
			}
			this.logger.info("Setting number of active jobs to 0.");
			this.activeJobsCounterLock.lock();
			try {
				this.logger.trace("Acquired activeJobsCounterLock for setting it to 0");
				this.activeJobs.set(0);
				this.numberOfActiveJobsHasChanged.signalAll();
			} finally {
				this.activeJobsCounterLock.unlock();
				this.logger.trace("Released activeJobsCounterLock after reset");
			}
			this.logger.debug("Pool shutdown completed.");
		} else {
			this.logger.debug("No additional threads for node attachment have been admitted, so there is no pool to close down.");
		}

		/* cancel node evaluator */
		if (this.cancelableNodeEvaluator) {
			this.logger.info("Canceling node evaluator.");
			((ICancelableNodeEvaluator) this.nodeEvaluator).cancelActiveTasks();
		}
		this.logger.info("Shutdown completed");
	}

	@Subscribe
	public void receiveSolutionCandidateEvent(final EvaluatedSearchSolutionCandidateFoundEvent<N, A, V> solutionEvent) {
		try {
			this.logger.info("Received solution with f-value {} and annotations {}", solutionEvent.getSolutionCandidate().getScore(), solutionEvent.getSolutionCandidate().getAnnotations());
			this.registerSolution(solutionEvent.getSolutionCandidate()); // unpack this solution and plug it into the registration process
		} catch (Exception e) {
			this.logger.error("An unexpected exception occurred while receiving EvaluatedSearchSolutionCandidateFoundEvent.", e);
		}
	}

	@Subscribe
	public void receiveRolloutEvent(final RolloutEvent<N, V> event) {
		try {
			this.logger.debug("Received rollout event: {}", event);
			this.post(event);
		} catch (Exception e) {
			this.logger.error("An unexpected exception occurred while receiving RolloutEvent", e);
		}
	}

	@Subscribe
	public void receiveSolutionCandidateAnnotationEvent(final SolutionAnnotationEvent<N, A, V> event) {
		try {
			this.logger.debug("Received solution annotation: {}", event);
			this.post(event);
		} catch (Exception e) {
			this.logger.error("An unexpected exception occurred receiveSolutionCandidateAnnotationEvent.", e);
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
		} catch (Exception e) {
			this.logger.error("An unexpected exception occurred while receiving node annotation event ", e);
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
					this.post(new NodeTypeSwitchEvent<Node<N, V>>(this.getId(), newNode, "or_closed"));
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
		} catch (Exception e) {
			this.logger.error("An unexpected exception occurred while the graph should be initialized.", e);
			return;
		}

		this.openLock.lockInterruptibly();
		try {
			/* remove previous roots from open */
			this.open.clear();
			/* now insert new nodes, and the leaf ones in open */
			for (Node<N, V> node : initialNodes) {
				this.insertNodeIntoLocalGraph(node);
				if (node == null) {
					throw new IllegalArgumentException("Cannot add NULL as a node to OPEN");
				}
				if (node.getInternalLabel() == null) {
					throw new IllegalArgumentException("Cannot insert node with label NULL");
				}
				this.open.add(this.getLocalVersionOfNode(node));
			}
		} finally {
			this.openLock.unlock();
		}
	}

	@Override
	public AlgorithmEvent nextWithException() throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException, AlgorithmException {
		switch (this.getState()) {
		case created: {
			AlgorithmInitializedEvent event = this.activate();
			this.logger.info("Initializing BestFirst search {} with {} CPUs and a timeout of {}ms", this.getId(), this.getConfig().cpus(), this.getConfig().timeout());
			int additionalCPUs = this.getConfig().cpus() - 1;
			if (additionalCPUs > 0) {
				this.parallelizeNodeExpansion(additionalCPUs);
			}
			this.initGraph();
			this.logger.info("Search initialized, returning activation event.");
			return event;
		}
		case active: {
			synchronized (this.pendingSolutionFoundEvents) {
				if (!this.pendingSolutionFoundEvents.isEmpty()) {
					return this.pendingSolutionFoundEvents.poll(); // these already have been posted over the event bus but are now returned to the controller for respective handling
				}
			}
			AlgorithmEvent event;

			/* if worker threads are used for expansion, make sure that there is at least one that is not busy */
			if (this.additionalThreadsForNodeAttachment > 0) {
				boolean poolSlotFree = false;
				boolean haveLock = false;
				do {
					this.checkAndConductTermination();
					try {
						this.activeJobsCounterLock.lockInterruptibly();
						haveLock = true;
						this.logger.trace("Acquired activeJobsCounterLock for read");
						this.logger.debug("The pool is currently busy with {}/{} jobs.", this.activeJobs.get(), this.additionalThreadsForNodeAttachment);
						if (this.additionalThreadsForNodeAttachment > this.activeJobs.get()) {
							poolSlotFree = true;
						}
						this.logger.trace("Number of active jobs is now {}", this.activeJobs.get());
						if (!poolSlotFree) {
							this.logger.trace("Releasing activeJobsCounterLock for a wait.");
							try {
								haveLock = false;
								this.numberOfActiveJobsHasChanged.await();
								haveLock = true;
							} catch (InterruptedException e) { // if we are interrupted during a wait, we must still conduct a controlled shutdown
								this.logger.debug("Received an interrupt while waiting for number of active jobs to change.");
								if (!this.isShutdownInitialized()) { // if the algorithm has not been shutdown yet, we do this now by interrupting ourselves explicitly and invoking the check
									this.activeJobsCounterLock.unlock(); // we can (and MUST) unlock now to avoid deadlocks
									Thread.currentThread().interrupt();
									this.checkAndConductTermination();
								} else {
									throw e; // if the algorithm has already been shut down, just throw the exception
								}
							}
							this.logger.trace("Re-acquired activeJobsCounterLock after a wait.");
							this.logger.debug("Number of active jobs has changed. Let's see whether we can enter now ...");
						}
					} finally {
						if (haveLock) {
							this.logger.trace("Trying to unlock activeJobsCounterLock");
							this.activeJobsCounterLock.unlock();
							haveLock = false;
							this.logger.trace("Released activeJobsCounterLock after read.");
						} else {
							this.logger.trace("Don't need to give lock free, because we came to the finally-block via an exception.");
						}
					}
				} while (!poolSlotFree);
			}

			/* expand next node */
			this.checkAndConductTermination();
			event = this.expandNextNode();

			/* if no event has occurred, still check whether a solution has arrived in the meantime prior to setting the algorithm state to inactive */
			if (event == null) {
				synchronized (this.pendingSolutionFoundEvents) {
					if (!this.pendingSolutionFoundEvents.isEmpty()) {
						event = this.pendingSolutionFoundEvents.poll();
					} else {
						this.logger.info("No event was returned and there are no pending solutions. Number of active jobs: {}. Setting state to inactive.", this.activeJobs.get());
						return this.terminate();
					}
				}
			}

			if (!(event instanceof SolutionCandidateFoundEvent)) {
				this.post(event);
			}
			return event;
		}
		default:
			throw new IllegalStateException("BestFirst search is in state " + this.getState() + " in which next must not be called!");
		}

	}

	public void selectNodeForNextExpansion(final N node) throws InterruptedException {
		this.selectNodeForNextExpansion(this.ext2int.get(node));
	}

	@SuppressWarnings("unchecked")
	public NodeExpansionJobSubmittedEvent<N, A, V> nextNodeExpansion() {
		while (this.hasNext()) {
			AlgorithmEvent e = this.next();
			if (e instanceof NodeExpansionJobSubmittedEvent) {
				return (NodeExpansionJobSubmittedEvent<N, A, V>) e;
			}
		}
		return null;
	}

	public EvaluatedSearchGraphPath<N, A, V> nextSolutionThatDominatesOpen() throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException, AlgorithmException {
		EvaluatedSearchGraphPath<N, A, V> currentlyBestSolution = null;
		V currentlyBestScore = null;
		boolean loopCondition = true;
		while (loopCondition) {
			EvaluatedSearchGraphPath<N, A, V> solution = this.nextSolutionCandidate();
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
		/* intentionally left blank */
	}

	protected boolean beforeSelection() {
		return true;
	}

	protected void afterSelection(final Node<N, V> node) {
		/* intentionally left blank */
	}

	protected void beforeExpansion(final Node<N, V> node) {
		/* intentionally left blank */
	}

	protected void afterExpansion(final Node<N, V> node) {
		/* intentionally left blank */
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
		int maxThreads = this.getConfig().threads() > 0 ? this.getConfig().threads() : -1;
		if (maxThreads > 0 && this.additionalThreadsForNodeAttachment > maxThreads - 2) { // timer and main thread must not add up here
			this.additionalThreadsForNodeAttachment = Math.min(this.additionalThreadsForNodeAttachment, maxThreads - 2);
		}
		if (this.additionalThreadsForNodeAttachment < 1) {
			this.logger.info("Effectively not parallelizing, since only {} threads are allowed by configuration, and 2 are needed for control and maintenance.", this.getConfig().threads());
			this.additionalThreadsForNodeAttachment = 0;
			return;
		}
		AtomicInteger counter = new AtomicInteger(0);
		this.pool = Executors.newFixedThreadPool(this.additionalThreadsForNodeAttachment, r -> {
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

	public Node<N, V> getInternalRepresentationOf(final N node) {
		return this.ext2int.get(node);
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
	public String getLoggerName() {
		return this.loggerName;
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger.info("Switching logger from {} to {}", this.logger.getName(), name);
		this.loggerName = name;
		this.logger = LoggerFactory.getLogger(name);
		this.logger.info("Activated logger {} with name {}", name, this.logger.getName());
		if (this.nodeEvaluator instanceof ILoggingCustomizable) {
			this.logger.info("Setting logger of node evaluator {} to {}.nodeevaluator", this.nodeEvaluator, name);
			((ILoggingCustomizable) this.nodeEvaluator).setLoggerName(name + ".nodeevaluator");
		} else {
			this.logger.info("Node evaluator {} does not implement ILoggingCustomizable, so its logger won't be customized.", this.nodeEvaluator);
		}
		super.setLoggerName(this.loggerName + "._orgraphsearch");
	}

	public Queue<EvaluatedSearchGraphPath<N, A, V>> getSolutionQueue() {
		return this.solutions;
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
	public IBestFirstConfig getConfig() {
		return (IBestFirstConfig) super.getConfig();
	}

	@Override
	public String toString() {
		Map<String, Object> fields = new HashMap<>();
		fields.put("graphGenerator", this.graphGenerator);
		fields.put("nodeEvaluator", this.nodeEvaluator);
		return ToJSONStringUtil.toJSONString(this.getClass().getSimpleName(), fields);
	}
}