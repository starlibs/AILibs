package ai.libs.jaicore.search.algorithms.standard.bestfirst;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import org.api4.java.ai.graphsearch.problem.IPathSearchWithPathEvaluationsInput;
import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.IPathGoalTester;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.ICancelablePathEvaluator;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPotentiallyGraphDependentPathEvaluator;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPotentiallySolutionReportingPathEvaluator;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPotentiallyUncertaintyAnnotatingPathEvaluator;
import org.api4.java.algorithm.events.IAlgorithmEvent;
import org.api4.java.algorithm.events.result.ISolutionCandidateFoundEvent;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.api4.java.common.control.ILoggingCustomizable;
import org.api4.java.datastructure.graph.implicit.IGraphGenerator;
import org.api4.java.datastructure.graph.implicit.INewNodeDescription;
import org.api4.java.datastructure.graph.implicit.IRootGenerator;
import org.api4.java.datastructure.graph.implicit.ISuccessorGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

import ai.libs.jaicore.basic.algorithm.AlgorithmInitializedEvent;
import ai.libs.jaicore.concurrent.GlobalTimer;
import ai.libs.jaicore.graphvisualizer.events.graph.GraphInitializedEvent;
import ai.libs.jaicore.graphvisualizer.events.graph.NodeAddedEvent;
import ai.libs.jaicore.graphvisualizer.events.graph.NodeInfoAlteredEvent;
import ai.libs.jaicore.graphvisualizer.events.graph.NodeParentSwitchEvent;
import ai.libs.jaicore.graphvisualizer.events.graph.NodeRemovedEvent;
import ai.libs.jaicore.graphvisualizer.events.graph.NodeTypeSwitchEvent;
import ai.libs.jaicore.interrupt.InterruptionTimerTask;
import ai.libs.jaicore.logging.LoggerUtil;
import ai.libs.jaicore.logging.ToJSONStringUtil;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.events.EvaluatedSearchSolutionCandidateFoundEvent;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.events.FValueEvent;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.events.NodeAnnotationEvent;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.events.NodeExpansionCompletedEvent;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.events.NodeExpansionJobSubmittedEvent;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.events.RemovedGoalNodeFromOpenEvent;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.events.RolloutEvent;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.events.SolutionAnnotationEvent;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.events.SuccessorComputationCompletedEvent;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.nodeevaluation.DecoratingNodeEvaluator;
import ai.libs.jaicore.search.core.interfaces.AOptimalPathInORGraphSearch;
import ai.libs.jaicore.search.model.other.EvaluatedSearchGraphPath;
import ai.libs.jaicore.search.model.travesaltree.BackPointerPath;

/**
 *
 * @author fmohr, wever
 *
 * @param <I>
 * @param <N>
 * @param <A>
 * @param <V>
 */
public class BestFirst<I extends IPathSearchWithPathEvaluationsInput<N, A, V>, N, A, V extends Comparable<V>> extends AOptimalPathInORGraphSearch<I, N, A, V> {

	private Logger bfLogger = LoggerFactory.getLogger(BestFirst.class);
	private String loggerName;

	private static final String SPACER = "\n\t\t";

	public enum ParentDiscarding {
		NONE, OPEN, ALL
	}

	/* problem definition */
	protected final IGraphGenerator<N, A> graphGenerator;
	protected final IRootGenerator<N> rootGenerator;
	protected final ISuccessorGenerator<N, A> successorGenerator;
	protected final IPathGoalTester<N, A> pathGoalTester;
	protected final IPathEvaluator<N, A, V> nodeEvaluator;

	/* algorithm configuration */
	private int timeoutForComputationOfF;
	private IPathEvaluator<N, A, V> timeoutNodeEvaluator;

	/* automatically derived auxiliary variables */
	private final boolean solutionReportingNodeEvaluator;
	private final boolean cancelableNodeEvaluator;

	/* general algorithm state and statistics */
	private int createdCounter;
	private int expandedCounter;
	private boolean initialized = false;
	private final List<INewNodeDescription<N, A>> lastExpansion = new ArrayList<>();
	protected final Queue<EvaluatedSearchGraphPath<N, A, V>> solutions = new LinkedBlockingQueue<>();
	protected final Queue<EvaluatedSearchSolutionCandidateFoundEvent<N, A, V>> pendingSolutionFoundEvents = new LinkedBlockingQueue<>();

	/* communication */
	protected final Map<N, BackPointerPath<N, A, V>> ext2int = new ConcurrentHashMap<>();

	/* search graph model */
	protected Queue<BackPointerPath<N, A, V>> open = new PriorityQueue<>((n1, n2) -> n1.getScore().compareTo(n2.getScore()));
	private BackPointerPath<N, A, V> nodeSelectedForExpansion; // the node that will be expanded next
	private final Map<N, Thread> expanding = new HashMap<>(); // EXPANDING contains the nodes being expanded and the threads doing this job
	private final Set<N> closed = new HashSet<>(); // CLOSED contains only node but not paths

	/* parallelization */
	protected int additionalThreadsForNodeAttachment = 0;
	private ExecutorService pool;
	private Collection<Thread> threadsOfPool = new ArrayList<>(); // the worker threads of the pool
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
		this.pathGoalTester = problem.getGoalTester();

		/* if the node evaluator is graph dependent, communicate the generator to it */
		this.nodeEvaluator = problem.getPathEvaluator();
		if (this.nodeEvaluator == null) {
			throw new IllegalArgumentException("Cannot work with node evaulator that is null");
		} else if (this.nodeEvaluator instanceof DecoratingNodeEvaluator<?, ?, ?>) {
			DecoratingNodeEvaluator<N, A, V> castedEvaluator = (DecoratingNodeEvaluator<N, A, V>) this.nodeEvaluator;
			if (castedEvaluator.requiresGraphGenerator()) {
				this.bfLogger.info("{} is a graph dependent node evaluator. Setting its graph generator now ...", castedEvaluator);
				castedEvaluator.setGenerator(this.graphGenerator, this.pathGoalTester);
			}
			if (castedEvaluator.reportsSolutions()) {
				this.bfLogger.info("{} is a solution reporter. Register the search algo in its event bus", castedEvaluator);
				castedEvaluator.registerSolutionListener(this);
				this.solutionReportingNodeEvaluator = true;
			} else {
				this.solutionReportingNodeEvaluator = false;
			}
		} else {
			if (this.nodeEvaluator instanceof IPotentiallyGraphDependentPathEvaluator) {
				this.bfLogger.info("{} is a graph dependent node evaluator. Setting its graph generator now ...", this.nodeEvaluator);
				((IPotentiallyGraphDependentPathEvaluator<N, A, V>) this.nodeEvaluator).setGenerator(this.graphGenerator, this.pathGoalTester);
			}

			/* if the node evaluator is a solution reporter, register in his event bus */
			if (this.nodeEvaluator instanceof IPotentiallySolutionReportingPathEvaluator) {
				this.bfLogger.info("{} is a solution reporter. Register the search algo in its event bus", this.nodeEvaluator);
				((IPotentiallySolutionReportingPathEvaluator<N, A, V>) this.nodeEvaluator).registerSolutionListener(this);
				this.solutionReportingNodeEvaluator = true;
			} else {
				this.solutionReportingNodeEvaluator = false;
			}
		}
		this.cancelableNodeEvaluator = this.nodeEvaluator instanceof ICancelablePathEvaluator;

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

	private class NodeBuilder implements Runnable {

		private final Collection<N> todoList;
		private final BackPointerPath<N, A, V> expandedNodeInternal;
		private final INewNodeDescription<N, A> successorDescription;

		public NodeBuilder(final Collection<N> todoList, final BackPointerPath<N, A, V> expandedNodeInternal, final INewNodeDescription<N, A> successorDescription) {
			super();
			this.todoList = todoList;
			this.expandedNodeInternal = expandedNodeInternal;
			this.successorDescription = successorDescription;
		}

		private void communicateJobFinished() {
			synchronized (this.todoList) {
				this.todoList.remove(this.successorDescription.getTo());
				if (this.todoList.isEmpty()) {
					BestFirst.this.post(new NodeExpansionCompletedEvent<>(BestFirst.this, this.expandedNodeInternal));
				}
			}
		}

		@Override
		public void run() {
			BestFirst.this.bfLogger.debug("Start node creation.");
			long start = System.currentTimeMillis();
			try {
				if (BestFirst.this.isStopCriterionSatisfied()) {
					this.communicateJobFinished();
					return;
				}
				BestFirst.this.lastExpansion.add(this.successorDescription);

				BackPointerPath<N, A, V> newNode = BestFirst.this.newNode(this.expandedNodeInternal, this.successorDescription.getTo(), this.successorDescription.getArcLabel());

				/* update creation counter */
				BestFirst.this.createdCounter++;

				/* compute node label */
				try {
					BestFirst.this.labelNode(newNode);
					if (newNode.getScore() == null) {
						BestFirst.this.post(new NodeTypeSwitchEvent<>(BestFirst.this, newNode, ENodeType.OR_PRUNED.toString()));
						return;
					} else {
						BestFirst.this.post(new NodeInfoAlteredEvent<>(BestFirst.this, newNode));
					}
					if (BestFirst.this.isStopCriterionSatisfied()) {
						this.communicateJobFinished();
						return;
					}
				} catch (InterruptedException e) {
					if (!BestFirst.this.isShutdownInitialized()) {
						BestFirst.this.bfLogger.warn("Leaving node building routine due to interrupt. This leaves the search inconsistent; the node should be attached again!");
					}
					BestFirst.this.bfLogger.debug("Worker has been interrupted, exiting.");
					BestFirst.this.post(new NodeAnnotationEvent<>(BestFirst.this, newNode, ENodeAnnotation.F_ERROR.toString(), e));
					BestFirst.this.post(new NodeTypeSwitchEvent<>(BestFirst.this, newNode, ENodeType.OR_PRUNED.toString()));
					BestFirst.this.post(new NodeInfoAlteredEvent<>(BestFirst.this, newNode));
					Thread.currentThread().interrupt();
					return;
				} catch (TimeoutException e) {
					BestFirst.this.bfLogger.debug("Node evaluation of {} has timed out.", newNode.hashCode());
					newNode.setAnnotation(ENodeAnnotation.F_ERROR.toString(), e);
					BestFirst.this.post(new NodeAnnotationEvent<>(BestFirst.this, newNode, ENodeAnnotation.F_ERROR.toString(), e));
					BestFirst.this.post(new NodeTypeSwitchEvent<>(BestFirst.this, newNode, ENodeType.OR_TIMEDOUT.toString()));
					BestFirst.this.post(new NodeInfoAlteredEvent<>(BestFirst.this, newNode));
					return;
				} catch (Exception e) {
					BestFirst.this.bfLogger.debug("Observed an exception during computation of f:\n{}", LoggerUtil.getExceptionInfo(e));
					newNode.setAnnotation(ENodeAnnotation.F_ERROR.toString(), e);
					BestFirst.this.post(new NodeAnnotationEvent<>(BestFirst.this, newNode, ENodeAnnotation.F_ERROR.toString(), e));
					BestFirst.this.post(new NodeTypeSwitchEvent<>(BestFirst.this, newNode, ENodeType.OR_PRUNED.toString()));

					BestFirst.this.post(new NodeInfoAlteredEvent<>(BestFirst.this, newNode));
					return;
				}

				/* depending on the algorithm setup, now decide how to proceed with the node */

				/* if we discard (either only on OPEN or on both OPEN and CLOSED) */
				boolean nodeProcessed = false;
				if (BestFirst.this.getConfig().parentDiscarding() != ParentDiscarding.NONE) {
					BestFirst.this.openLock.lockInterruptibly();
					try {

						/* determine whether we already have the node AND it is worse than the one we want to insert */
						Optional<BackPointerPath<N, A, V>> existingIdenticalNodeOnOpen = BestFirst.this.open.stream().filter(n -> n.getHead().equals(newNode.getHead())).findFirst();
						if (existingIdenticalNodeOnOpen.isPresent()) {
							BackPointerPath<N, A, V> existingNode = existingIdenticalNodeOnOpen.get();
							if (newNode.getScore().compareTo(existingNode.getScore()) < 0) {
								BestFirst.this.post(new NodeTypeSwitchEvent<>(BestFirst.this, newNode, (newNode.isGoal() ? ENodeType.OR_SOLUTION.toString() : ENodeType.OR_OPEN.toString())));
								BestFirst.this.post(new NodeRemovedEvent<>(BestFirst.this, existingNode));
								BestFirst.this.open.remove(existingNode);
								if (newNode.getScore() == null) {
									throw new IllegalArgumentException("Cannot insert nodes with value NULL into OPEN!");
								}
								BestFirst.this.open.add(newNode);
							} else {
								BestFirst.this.post(new NodeRemovedEvent<>(BestFirst.this, newNode));
							}
							nodeProcessed = true;
						}

						/*
						 * if parent discarding is not only for OPEN but also for CLOSE (and the node
						 * was not on OPEN), check the list of expanded nodes
						 */
						else if (BestFirst.this.getConfig().parentDiscarding() == ParentDiscarding.ALL) {
							/* reopening, if the node is already on CLOSED */
							Optional<N> existingIdenticalNodeOnClosed = BestFirst.this.closed.stream().filter(n -> n.equals(newNode.getHead())).findFirst();
							if (existingIdenticalNodeOnClosed.isPresent()) {
								BackPointerPath<N, A, V> node = BestFirst.this.ext2int.get(existingIdenticalNodeOnClosed.get());
								if (newNode.getScore().compareTo(node.getScore()) < 0) {
									node.setParent(newNode.getParent());
									node.setScore(newNode.getScore());
									BestFirst.this.closed.remove(node.getHead());
									BestFirst.this.open.add(node);
									BestFirst.this.post(new NodeParentSwitchEvent<BackPointerPath<N, A, V>>(BestFirst.this, node, node.getParent(), newNode.getParent()));
								}
								BestFirst.this.post(new NodeRemovedEvent<BackPointerPath<N, A, V>>(BestFirst.this, newNode));
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
								assert !BestFirst.this.closed.contains(newNode.getHead()) : "Currently only tree search is supported. But now we add a node to OPEN whose point has already been expanded before.";
								BestFirst.this.expanding.keySet().forEach(node -> {
									assert !node.equals(newNode.getHead()) : Thread.currentThread() + " cannot add node to OPEN that is currently being expanded by " + BestFirst.this.expanding.get(node) + ".\n\tFrom: "
											+ newNode.getParent().getHead() + "\n\tTo: " + node;
								});
								if (newNode.getScore() == null) {
									throw new IllegalArgumentException("Cannot insert nodes with value NULL into OPEN!");
								}
								BestFirst.this.bfLogger.debug("Inserting successor {} of {} to OPEN. F-Value is {}", newNode.hashCode(), this.expandedNodeInternal, newNode.getScore());
								BestFirst.this.open.add(newNode);
							} finally {
								BestFirst.this.openLock.unlock();
							}
						}
					}
					BestFirst.this.post(new NodeTypeSwitchEvent<>(BestFirst.this, newNode, (newNode.isGoal() ? ENodeType.OR_SOLUTION.toString() : ENodeType.OR_OPEN.toString())));
					BestFirst.this.createdCounter++;
				}

				/* Recognize solution in cache together with annotation */
				if (newNode.isGoal()) {
					EvaluatedSearchGraphPath<N, A, V> solution = new EvaluatedSearchGraphPath<>(newNode, newNode.getScore());

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
				BestFirst.this.bfLogger.info("Node builder has been interrupted, finishing execution.");
			} catch (Exception e) {
				BestFirst.this.bfLogger.error("An unexpected exception occurred while building nodes", e);
			} finally {

				/*
				 * free resources if this is computed by helper threads and notify the listeners
				 */
				assert !Thread.holdsLock(BestFirst.this.openLock) : "Node Builder must not hold a lock on OPEN when locking the active jobs counter";
				BestFirst.this.bfLogger.debug("Trying to decrement active jobs by one.");
				BestFirst.this.bfLogger.trace("Waiting for activeJobsCounterlock to become free.");
				BestFirst.this.activeJobsCounterLock.lock(); // cannot be interruptible without opening more cases
				BestFirst.this.bfLogger.trace("Acquired activeJobsCounterLock for decrement.");
				try {
					if (BestFirst.this.pool != null) {
						BestFirst.this.activeJobs.decrementAndGet();
						BestFirst.this.bfLogger.trace("Decremented job counter.");
					}
				} finally {
					BestFirst.this.numberOfActiveJobsHasChanged.signalAll();
					BestFirst.this.activeJobsCounterLock.unlock();
					BestFirst.this.bfLogger.trace("Released activeJobsCounterLock after decrement.");
				}
				this.communicateJobFinished();
				BestFirst.this.bfLogger.debug("Builder exits. Build process took {}ms. Interrupt-flag is {}", System.currentTimeMillis() - start, Thread.currentThread().isInterrupted());
			}
		}
	}

	protected BackPointerPath<N, A, V> newNode(final BackPointerPath<N, A, V> parent, final N t2, final A arc) throws InterruptedException {
		return this.newNode(parent, t2, arc, null);
	}

	protected BackPointerPath<N, A, V> newNode(final BackPointerPath<N, A, V> parent, final N t2, final A arc, final V evaluation) throws InterruptedException {
		this.openLock.lockInterruptibly();
		try {
			assert !this.open.contains(parent) : "Parent node " + parent + " is still on OPEN, which must not be the case! OPEN class: " + this.open.getClass().getName() + ". OPEN size: " + this.open.size();
		} finally {
			this.openLock.unlock();
		}

		/* create new node and check whether it is a goal */
		BackPointerPath<N, A, V> newNode = new BackPointerPath<>(parent, t2, arc);
		if (evaluation != null) {
			newNode.setScore(evaluation);
		}

		/* check loop */
		assert parent == null || !parent.getNodes().contains(t2) : "There is a loop in the underlying graph. The following path contains the last node twice: "
				+ newNode.getNodes().stream().map(N::toString).reduce("", (s, t) -> s + SPACER + t);

		/* currently, we only support tree search */
		assert !this.ext2int.containsKey(t2) : "Reached node " + t2 + " for the second time.\nt\tFirst path:" + this.ext2int.get(t2).getNodes().stream().map(n -> n + "").reduce("", (s, t) -> s + SPACER + t) + "\n\tSecond Path:"
		+ newNode.getNodes().stream().map(N::toString).reduce("", (s, t) -> s + SPACER + t);

		/* register node in map and create annotation object */
		this.ext2int.put(t2, newNode);

		/* detect whether node is solution */
		if (this.pathGoalTester.isGoal(newNode)) {
			newNode.setGoal(true);
		}

		/* send events for this new node */
		if (parent == null) {
			this.post(new GraphInitializedEvent<BackPointerPath<N, A, V>>(this, newNode));
		} else {
			this.post(new NodeAddedEvent<BackPointerPath<N, A, V>>(this, parent, newNode, (newNode.isGoal() ? ENodeType.OR_SOLUTION.toString() : ENodeType.OR_CREATED.toString())));
			this.bfLogger.debug("Sent message for creation of node {} as a successor of {}", newNode.hashCode(), parent.hashCode());
		}
		return newNode;
	}

	protected void labelNode(final BackPointerPath<N, A, V> node) throws AlgorithmTimeoutedException, AlgorithmExecutionCanceledException, InterruptedException, AlgorithmException {

		/* define timeouter for label computation */
		this.bfLogger.debug("Computing node label for node with hash code {}", node.hashCode());
		if (this.isStopCriterionSatisfied()) {
			this.bfLogger.debug("Found stop criterion to be true. Returning control.");
			return;
		}
		InterruptionTimerTask interruptionTask = null;
		AtomicBoolean timedout = new AtomicBoolean(false);
		if (BestFirst.this.timeoutForComputationOfF > 0) {
			interruptionTask = new InterruptionTimerTask("Timeout for Node-Labeling in " + BestFirst.this, Thread.currentThread(), () -> timedout.set(true));
			this.bfLogger.debug("Scheduling timeout for f-value computation. Allowed time: {}ms", this.timeoutForComputationOfF);
			GlobalTimer.getInstance().schedule(interruptionTask, this.timeoutForComputationOfF);
		}

		/* compute f */
		V label = null;
		boolean computationTimedout = false;
		long startComputation = System.currentTimeMillis();
		try {
			this.bfLogger.trace("Calling f-function of node evaluator for {}", node.hashCode());
			label = this.computeTimeoutAware(() -> BestFirst.this.nodeEvaluator.evaluate(node), "Node Labeling with " + BestFirst.this.nodeEvaluator, !this.threadsOfPool.contains(Thread.currentThread())); // shutdown algorithm on exception
			// iff
			// this is not a worker thread
			this.bfLogger.trace("Determined f-value of {}", label);
			if (this.isStopCriterionSatisfied()) {
				return;
			}

			/* check whether the required time exceeded the timeout */
			long fTime = System.currentTimeMillis() - startComputation;
			if (BestFirst.this.timeoutForComputationOfF > 0 && fTime > BestFirst.this.timeoutForComputationOfF + 1000) {
				BestFirst.this.bfLogger.warn("Computation of f for node {} took {}ms, which is more than the allowed {}ms", node, fTime, BestFirst.this.timeoutForComputationOfF);
			}
		} catch (InterruptedException e) {
			this.bfLogger.info("Thread {} received interrupt in node evaluation. Timeout flag is {}", Thread.currentThread(), timedout.get());
			if (timedout.get()) {
				BestFirst.this.bfLogger.debug("Received interrupt during computation of f.");
				this.post(new NodeTypeSwitchEvent<>(this, node, ENodeType.OR_TIMEDOUT.toString()));
				node.setAnnotation(ENodeAnnotation.F_ERROR.toString(), "Timeout");
				computationTimedout = true;
				Thread.interrupted(); // set interrupt state of thread to FALSE, because interrupt
				try {
					label = BestFirst.this.timeoutNodeEvaluator != null ? BestFirst.this.timeoutNodeEvaluator.evaluate(node) : null;
				} catch (Exception e2) {
					this.bfLogger.error("An unexpected exception occurred while labeling node {}", node, e2);
				}
			} else {
				this.bfLogger.info("Received external interrupt. Forwarding this interrupt.");
				throw e;
			}
		}
		if (interruptionTask != null) {
			interruptionTask.cancel();
		}

		/* register time required to compute this node label */
		long fTime = System.currentTimeMillis() - startComputation;
		node.setAnnotation(ENodeAnnotation.F_TIME.toString(), fTime);
		this.bfLogger.debug("Computed label {} for {} in {}ms", label, node.hashCode(), fTime);

		/* if no label was computed, prune the node and cancel the computation */
		if (label == null) {
			if (!computationTimedout) {
				BestFirst.this.bfLogger.debug("Not inserting node {} since its label is missing!", node.hashCode());
			} else {
				BestFirst.this.bfLogger.debug("Not inserting node {} because computation of f-value timed out.", node.hashCode());
			}
			if (!node.getAnnotations().containsKey(ENodeAnnotation.F_ERROR.toString())) {
				node.setAnnotation(ENodeAnnotation.F_ERROR.toString(), "f-computer returned NULL");
			}
			return;
		}

		/* check whether an uncertainty-value is present if the node evaluator is an uncertainty-measuring evaluator */
		assert !(this.nodeEvaluator instanceof IPotentiallyUncertaintyAnnotatingPathEvaluator) || !((IPotentiallyUncertaintyAnnotatingPathEvaluator<?, ?, ?>) this.nodeEvaluator).annotatesUncertainty()
		|| node.getAnnotation(ENodeAnnotation.F_UNCERTAINTY.name()) != null : "Uncertainty-based node evaluator (" + this.nodeEvaluator.getClass().getName() + ") claims to annotate uncertainty but has not assigned any uncertainty to " + node.getHead() + " with label " + label;

		/* eventually set the label */
		node.setScore(label);
		assert node.getScore() != null : "Node label must not be NULL";

		this.post(new NodeInfoAlteredEvent<BackPointerPath<N, A, V>>(this, node));
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
			for (N n0 : this.rootGenerator.getRoots()) {
				BackPointerPath<N, A, V> root = this.newNode(null, n0, null);
				if (root == null) {
					throw new IllegalArgumentException("Root cannot be null. Cannot add NULL as a node to OPEN");
				}
				try {
					this.labelNode(root);
				} catch (AlgorithmException e) {
					throw new AlgorithmException("Graph initialization failed: Could not compute the label for the root node due to an exception.", e);
				}
				this.bfLogger.debug("Labeled root with {}", root.getScore());
				this.checkAndConductTermination();
				if (root.getScore() == null) {
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

	protected void selectNodeForNextExpansion(final BackPointerPath<N, A, V> node) throws InterruptedException {
		assert node != null : "Cannot select node NULL for expansion!";
		this.nodeSelectionLock.lockInterruptibly();
		try {
			this.openLock.lockInterruptibly();
			try {
				assert !this.open.contains(null) : "OPEN contains NULL";
				assert this.open.stream().noneMatch(n -> n.getScore() == null) : "OPEN contains an element with value NULL";
				int openSizeBefore = this.open.size();
				assert this.nodeSelectedForExpansion == null : "Node selected for expansion must be NULL when setting it!";
				this.nodeSelectedForExpansion = node;
				assert this.open.contains(node) : "OPEN must contain the node to be expanded.\n\tOPEN size: " + this.open.size() + "\n\tNode to be expanded: " + node + ".\n\tOPEN: "
				+ this.open.stream().map(n -> SPACER + n).collect(Collectors.joining());
				this.open.remove(this.nodeSelectedForExpansion);
				int openSizeAfter = this.open.size();
				assert this.ext2int.containsKey(this.nodeSelectedForExpansion.getHead()) : "A node chosen for expansion has no entry in the ext2int map!";
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
	protected IAlgorithmEvent expandNextNode() throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException, AlgorithmException {

		/* Preliminarily check that the active jobs are less than the additional threads */
		assert this.additionalThreadsForNodeAttachment == 0 || this.activeJobs.get() < this.additionalThreadsForNodeAttachment : "Cannot expand nodes if number of active jobs (" + this.activeJobs.get()
		+ " is at least as high as the threads available for node attachment (" + this.additionalThreadsForNodeAttachment + ")";

		/*
		 * Step 1: determine node that will be expanded next. Either it already has been set
		 * or it will be the first of OPEN. If necessary, we wait for potential incoming nodes
		 */
		long startTimeOfExpansion = System.currentTimeMillis();
		final BackPointerPath<N, A, V> actualNodeSelectedForExpansion;
		BackPointerPath<N, A, V> tmpNodeSelectedForExpansion = null; // necessary workaround as setting final variables in a try-block is not reasonably possible
		this.nodeSelectionLock.lockInterruptibly();
		try {
			if (this.nodeSelectedForExpansion == null) {
				this.activeJobsCounterLock.lockInterruptibly();
				this.bfLogger.trace("Acquired activeJobsCounterLock for read.");
				boolean stopCriterionSatisfied = this.isStopCriterionSatisfied();
				try {
					this.bfLogger.debug("No next node has been selected. Choosing the first from OPEN.");
					while (this.open.isEmpty() && this.activeJobs.get() > 0 && !stopCriterionSatisfied) {
						this.bfLogger.trace("Await condition as open queue is empty and active jobs is {} ...", this.activeJobs.get());
						this.numberOfActiveJobsHasChanged.await();
						this.bfLogger.trace("Got signaled");
						stopCriterionSatisfied = this.isStopCriterionSatisfied();
					}
					if (!stopCriterionSatisfied) {
						this.openLock.lock();
						try {
							if (this.open.isEmpty()) {
								return null;
							}
							this.selectNodeForNextExpansion(this.open.peek());
						} finally {
							this.openLock.unlock();
						}
					}
				} finally {
					this.activeJobsCounterLock.unlock();
					this.bfLogger.trace("Released activeJobsCounterLock after read. Now checking termination.");
					this.checkAndConductTermination();
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
			this.expanding.put(actualNodeSelectedForExpansion.getHead(), Thread.currentThread());
			assert this.expanding.keySet().contains(tmpNodeSelectedForExpansion.getHead()) : "The node selected for expansion should be in the EXPANDING map by now.";
		}
		assert !this.open.contains(actualNodeSelectedForExpansion) : "Node selected for expansion is still on OPEN";
		assert actualNodeSelectedForExpansion != null : "We have not selected any node for expansion, but this must be the case at this point.";
		this.checkTerminationAndUnregisterFromExpand(actualNodeSelectedForExpansion);

		/* steps 2 and 3 only for non-goal nodes */
		IAlgorithmEvent expansionEvent;
		if (!actualNodeSelectedForExpansion.isGoal()) {

			/* Step 2: compute the successors in the underlying graph */
			this.beforeExpansion(actualNodeSelectedForExpansion);
			this.post(new NodeTypeSwitchEvent<BackPointerPath<N, A, V>>(this, actualNodeSelectedForExpansion, "or_expanding"));
			this.bfLogger.debug("Expanding node {} with f-value {}", actualNodeSelectedForExpansion.hashCode(), actualNodeSelectedForExpansion.getScore());
			this.bfLogger.debug("Start computation of successors");
			final List<INewNodeDescription<N, A>> successorDescriptions;
			List<INewNodeDescription<N, A>> tmpSuccessorDescriptions = null;
			assert !actualNodeSelectedForExpansion.isGoal() : "Goal nodes must not be expanded!";
			tmpSuccessorDescriptions = this.computeTimeoutAware(() -> {
				this.bfLogger.trace("Invoking getSuccessors");
				return BestFirst.this.successorGenerator.generateSuccessors(actualNodeSelectedForExpansion.getHead());
			}, "Successor generation", !this.threadsOfPool.contains(Thread.currentThread())); // shutdown algorithm on exception iff this is not one of the worker threads
			assert tmpSuccessorDescriptions != null : "Successor descriptions must never be null!";
			if (this.bfLogger.isTraceEnabled()) {
				this.bfLogger.trace("Received {} successor descriptions for node with hash code {}. The first 1000 of these are \n\t{}", tmpSuccessorDescriptions.size(), actualNodeSelectedForExpansion.getHead(),
						tmpSuccessorDescriptions.stream().limit(1000).map(s -> s.getTo().toString()).collect(Collectors.joining("\n\t")));
			}
			successorDescriptions = tmpSuccessorDescriptions;
			this.checkTerminationAndUnregisterFromExpand(actualNodeSelectedForExpansion);
			this.bfLogger.debug("Finished computation of successors. Sending SuccessorComputationCompletedEvent with {} successors for {}", successorDescriptions.size(), actualNodeSelectedForExpansion.hashCode());
			this.post(new SuccessorComputationCompletedEvent<>(this, actualNodeSelectedForExpansion, successorDescriptions));

			/*
			 * step 3: trigger node builders that compute node details and decide whether
			 * and how to integrate the successors into the search
			 */
			List<N> todoList = successorDescriptions.stream().map(INewNodeDescription::getTo).collect(Collectors.toList());
			long lastTerminationCheck = System.currentTimeMillis();
			for (INewNodeDescription<N, A> successorDescription : successorDescriptions) {
				NodeBuilder nb = new NodeBuilder(todoList, actualNodeSelectedForExpansion, successorDescription);
				this.bfLogger.trace("Number of additional threads for node attachment is {}", this.additionalThreadsForNodeAttachment);
				if (this.additionalThreadsForNodeAttachment < 1) {
					nb.run();
				} else {
					this.lockConditionSafeleyWhileExpandingNode(this.activeJobsCounterLock, actualNodeSelectedForExpansion); // acquires the lock and shuts down properly when being interrupted
					this.bfLogger.trace("Acquired activeJobsCounterLock for increment");
					try {
						this.activeJobs.incrementAndGet();
					} finally {
						this.numberOfActiveJobsHasChanged.signalAll();
						this.activeJobsCounterLock.unlock();
						this.bfLogger.trace("Released activeJobsCounterLock after increment");
					}
					if (this.isShutdownInitialized()) {
						break;
					}
					this.pool.submit(nb);
				}

				/* frequently check termination conditions */
				if (System.currentTimeMillis() - lastTerminationCheck > 50) {
					if (this.expanding.containsKey(actualNodeSelectedForExpansion)) {
						this.checkTerminationAndUnregisterFromExpand(actualNodeSelectedForExpansion);
					} else { // maybe the node has been removed from the expansion list during a timeout or a cancel
						this.checkAndConductTermination();
					}
					lastTerminationCheck = System.currentTimeMillis();
				}
			}
			this.bfLogger.debug("Finished expansion of node {} after {}ms. Size of OPEN is now {}. Number of active jobs is {}", actualNodeSelectedForExpansion.hashCode(), System.currentTimeMillis() - startTimeOfExpansion, this.open.size(),
					this.activeJobs.get());
			this.checkTerminationAndUnregisterFromExpand(actualNodeSelectedForExpansion);
			expansionEvent = new NodeExpansionJobSubmittedEvent<>(this, actualNodeSelectedForExpansion, successorDescriptions);
		} else {
			expansionEvent = new RemovedGoalNodeFromOpenEvent<>(this, actualNodeSelectedForExpansion);
		}

		/*
		 * step 4: update statistics, send closed notifications, and possibly return a
		 * solution
		 */
		this.expandedCounter++;
		synchronized (this.expanding) {
			this.expanding.remove(actualNodeSelectedForExpansion.getHead());
			assert !this.expanding.containsKey(actualNodeSelectedForExpansion.getHead()) : actualNodeSelectedForExpansion + " was expanded and it was not removed from EXPANDING!";
		}
		this.closed.add(actualNodeSelectedForExpansion.getHead());
		assert this.closed.contains(actualNodeSelectedForExpansion.getHead()) : "Expanded node " + actualNodeSelectedForExpansion + " was not inserted into CLOSED!";
		this.post(new NodeTypeSwitchEvent<BackPointerPath<N, A, V>>(this, actualNodeSelectedForExpansion, ENodeType.OR_CLOSED.toString()));
		this.afterExpansion(actualNodeSelectedForExpansion);
		this.checkAndConductTermination();
		this.openLock.lockInterruptibly();
		try {
			this.bfLogger.debug("Step ends. Size of OPEN now {}", this.open.size());
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

	private void lockConditionSafeleyWhileExpandingNode(final Lock l, final BackPointerPath<N, A, V> node) throws AlgorithmTimeoutedException, AlgorithmExecutionCanceledException, InterruptedException {
		try {
			l.lockInterruptibly();
		} catch (InterruptedException e) { // if we are interrupted during a wait, we must still conduct a controlled shutdown
			this.bfLogger.debug("Received an interrupt while waiting for {} to become available.", l);
			Thread.currentThread().interrupt();
			this.checkTerminationAndUnregisterFromExpand(node);
		}
	}

	private void unregisterFromExpand(final BackPointerPath<N, A, V> node) {
		assert this.expanding.containsKey(node.getHead()) : "Cannot unregister a node that is not being expanded currently";
		assert this.expanding.get(node.getHead()) == Thread.currentThread() : "Thread " + Thread.currentThread() + " cannot unregister other thread " + this.expanding.get(node.getHead()) + " from expansion map!";
		this.bfLogger.debug("Removing {} from EXPANDING.", node.hashCode());
		this.expanding.remove(node.getHead());
	}

	/**
	 * This is a small extension of the checkTermination method that makes sure that the current thread is not counted as a worker for an expanding node. This is important to make sure that the thread does not interrupt itself on a shutdown
	 *
	 * @throws TimeoutException
	 * @throws AlgorithmExecutionCanceledException
	 * @throws InterruptedException
	 */
	private void checkTerminationAndUnregisterFromExpand(final BackPointerPath<N, A, V> node) throws AlgorithmTimeoutedException, AlgorithmExecutionCanceledException, InterruptedException {
		if (this.isStopCriterionSatisfied()) {
			this.unregisterFromExpand(node);
			assert !this.expanding.containsKey(node.getHead()) : "Expanded node " + this.nodeSelectedForExpansion + " was not removed from EXPANDING!";
		}
		super.checkAndConductTermination();
	}

	@Override
	protected void shutdown() {

		/* check that the shutdown is not invoked by one of the workers or an interrupted thread */
		if (this.threadsOfPool.contains(Thread.currentThread())) {
			this.bfLogger.error("Worker thread {} must not shutdown the algorithm!", Thread.currentThread());
		}
		assert !Thread.currentThread().isInterrupted() : "The thread should not be interrupted when shutdown is called.";

		if (this.isShutdownInitialized()) {
			return;
		}

		/* set state to inactive */
		this.bfLogger.info("Invoking shutdown routine ...");
		this.bfLogger.debug("First conducting general algorithm shutdown routine ...");
		super.shutdown();
		this.bfLogger.debug("General algorithm shutdown routine completed. Now conducting BestFirst-specific shutdown activities.");

		/* interrupt the expanding threads */
		synchronized (this.expanding) {
			int interruptedThreads = 0;
			for (Entry<N, Thread> entry : this.expanding.entrySet()) {
				Thread t = entry.getValue();
				if (t.equals(Thread.currentThread())) {
					this.expanding.remove(entry.getKey());
					this.bfLogger.debug("Removing node {} with thread {} from expansion map, since this thread is realizing the shutdown.", entry.getKey(), t);
				} else {
					if (!this.hasThreadBeenInterruptedDuringShutdown(t)) {
						this.interruptThreadAsPartOfShutdown(t);
						interruptedThreads++;
					} else {
						this.bfLogger.debug("Not interrupting thread {} again, since it already has been interrupted during shutdown.", t);
					}
				}
			}
			this.bfLogger.debug("Interrupted {} active expansion threads.", interruptedThreads);
		}

		/* cancel ongoing work */
		if (this.additionalThreadsForNodeAttachment > 0) {
			this.bfLogger.debug("Shutting down worker pool.");
			if (this.pool != null) {
				this.bfLogger.info("Triggering shutdown of builder thread pool with interrupt");
				this.pool.shutdownNow();
			}
			try {
				this.bfLogger.debug("Waiting 3 days for pool shutdown.");
				if (this.pool != null) {
					this.pool.awaitTermination(3, TimeUnit.DAYS);
				} else {
					this.bfLogger.error("Apparently, the pool was unexpectedly not set and thus null.");
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				this.bfLogger.warn("Got interrupted during shutdown!", e);
			}
			if (this.pool != null) {
				assert this.pool.isTerminated() : "The worker pool has not been shutdown correctly!";
				if (!this.pool.isTerminated()) {
					this.bfLogger.error("Worker pool has not been shutdown correctly!");
				} else {
					this.bfLogger.info("Worker pool has been shut down.");
				}
			}
			this.bfLogger.info("Setting number of active jobs to 0.");
			this.bfLogger.trace("Waiting for activeJobsCounterLock.");
			this.activeJobsCounterLock.lock();
			try {
				this.bfLogger.trace("Acquired activeJobsCounterLock for setting it to 0");
				this.activeJobs.set(0);
				this.numberOfActiveJobsHasChanged.signalAll();
			} finally {
				this.activeJobsCounterLock.unlock();
				this.bfLogger.trace("Released activeJobsCounterLock after reset");
			}
			this.bfLogger.debug("Pool shutdown completed.");
		} else {
			this.bfLogger.debug("No additional threads for node attachment have been admitted, so there is no pool to close down.");
		}

		/* cancel node evaluator */
		if (this.cancelableNodeEvaluator) {
			this.bfLogger.info("Canceling node evaluator.");
			((ICancelablePathEvaluator) this.nodeEvaluator).cancelActiveTasks();
		}
		this.bfLogger.info("Shutdown completed");
	}

	@Subscribe
	public void receiveSolutionCandidateEvent(final EvaluatedSearchSolutionCandidateFoundEvent<N, A, V> solutionEvent) {
		try {
			this.bfLogger.info("Received solution with f-value {} and annotations {}", solutionEvent.getSolutionCandidate().getScore(), solutionEvent.getSolutionCandidate().getAnnotations());
			this.registerSolution(solutionEvent.getSolutionCandidate()); // unpack this solution and plug it into the registration process
		} catch (Exception e) {
			this.bfLogger.error("An unexpected exception occurred while receiving EvaluatedSearchSolutionCandidateFoundEvent.", e);
		}
	}

	@Subscribe
	public void receiveRolloutEvent(final RolloutEvent<N, V> event) {
		try {
			this.bfLogger.debug("Received rollout event: {}", event);
			this.post(event);
		} catch (Exception e) {
			this.bfLogger.error("An unexpected exception occurred while receiving RolloutEvent", e);
		}
	}

	@Subscribe
	public void receiveSolutionCandidateAnnotationEvent(final SolutionAnnotationEvent<N, A, V> event) {
		try {
			this.bfLogger.debug("Received solution annotation: {}", event);
			this.post(event);
		} catch (Exception e) {
			this.bfLogger.error("An unexpected exception occurred receiveSolutionCandidateAnnotationEvent.", e);
		}
	}

	@Subscribe
	public void receiveNodeAnnotationEvent(final NodeAnnotationEvent<N> event) {
		try {
			N nodeExt = event.getNode();
			this.bfLogger.debug("Received annotation {} with value {} for node {}", event.getAnnotationName(), event.getAnnotationValue(), event.getNode());
			if (!this.ext2int.containsKey(nodeExt)) {
				throw new IllegalArgumentException("Received annotation for a node I don't know!");
			}
			BackPointerPath<N, A, V> nodeInt = this.ext2int.get(nodeExt);
			nodeInt.setAnnotation(event.getAnnotationName(), event.getAnnotationValue());
		} catch (Exception e) {
			this.bfLogger.error("An unexpected exception occurred while receiving node annotation event ", e);
		}
	}

	protected void insertNodeIntoLocalGraph(final BackPointerPath<N, A, V> node) throws InterruptedException {
		BackPointerPath<N, A, V> localVersionOfParent = null;
		List<BackPointerPath<N, A, V>> path = node.path();
		BackPointerPath<N, A, V> leaf = path.get(path.size() - 1);
		for (BackPointerPath<N, A, V> nodeOnPath : path) {
			if (!this.ext2int.containsKey(nodeOnPath.getHead())) {
				assert nodeOnPath.getParent() != null : "Want to insert a new node that has no parent. That must not be the case! Affected node is: " + nodeOnPath.getHead();
				assert this.ext2int.containsKey(nodeOnPath.getParent().getHead()) : "Want to insert a node whose parent is unknown locally";
				BackPointerPath<N, A, V> newNode = this.newNode(localVersionOfParent, nodeOnPath.getHead(), nodeOnPath.getEdgeLabelToParent(), nodeOnPath.getScore());
				if (!newNode.isGoal() && !newNode.getHead().equals(leaf.getHead())) {
					this.post(new NodeTypeSwitchEvent<BackPointerPath<N, A, V>>(this, newNode, "or_closed"));
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
	protected BackPointerPath<N, A, V> getLocalVersionOfNode(final BackPointerPath<N, A, V> node) {
		return this.ext2int.get(node.getHead());
	}

	/** BLOCK B: Controlling the algorithm from the outside **/

	/**
	 * This method can be used to create an initial graph different from just root nodes. This can be interesting if the search is distributed and we want to search only an excerpt of the original one.
	 *
	 * @param initialNodes
	 */
	public void bootstrap(final Collection<BackPointerPath<N, A, V>> initialNodes) throws InterruptedException {

		if (this.initialized) {
			throw new UnsupportedOperationException("Bootstrapping is only supported if the search has already been initialized.");
		}

		/* now initialize the graph */
		try {
			this.initGraph();
		} catch (Exception e) {
			this.bfLogger.error("An unexpected exception occurred while the graph should be initialized.", e);
			return;
		}

		this.openLock.lockInterruptibly();
		try {
			/* remove previous roots from open */
			this.open.clear();
			/* now insert new nodes, and the leaf ones in open */
			for (BackPointerPath<N, A, V> node : initialNodes) {
				this.insertNodeIntoLocalGraph(node);
				if (node == null) {
					throw new IllegalArgumentException("Cannot add NULL as a node to OPEN");
				}
				if (node.getScore() == null) {
					throw new IllegalArgumentException("Cannot insert node with label NULL");
				}
				this.open.add(this.getLocalVersionOfNode(node));
			}
		} finally {
			this.openLock.unlock();
		}
	}

	@Override
	public IAlgorithmEvent nextWithException() throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException, AlgorithmException {
		try {
			this.registerActiveThread();
			switch (this.getState()) {
			case CREATED:
				AlgorithmInitializedEvent initEvent = this.activate();
				this.bfLogger.info("Initializing BestFirst search {} with the following configuration:\n\tCPUs: {}\n\tTimeout: {}ms\n\tNode Evaluator: {}", this, this.getConfig().cpus(), this.getConfig().timeout(), this.nodeEvaluator);
				int additionalCPUs = this.getConfig().cpus() - 1;
				if (additionalCPUs > 0) {
					this.parallelizeNodeExpansion(additionalCPUs);
				}
				this.initGraph();
				this.bfLogger.info("Search initialized, returning activation event.");
				return initEvent;

			case ACTIVE:
				synchronized (this.pendingSolutionFoundEvents) {
					if (!this.pendingSolutionFoundEvents.isEmpty()) {
						return this.pendingSolutionFoundEvents.poll(); // these already have been posted over the event bus but are now returned to the controller for respective handling
					}
				}
				IAlgorithmEvent event;

				/* if worker threads are used for expansion, make sure that there is at least one that is not busy */
				if (this.additionalThreadsForNodeAttachment > 0) {
					boolean poolSlotFree = false;
					boolean haveLock = false;
					do {
						this.checkAndConductTermination();
						try {
							this.activeJobsCounterLock.lockInterruptibly();
							haveLock = true;
							this.bfLogger.trace("Acquired activeJobsCounterLock for read");
							this.bfLogger.debug("The pool is currently busy with {}/{} jobs.", this.activeJobs.get(), this.additionalThreadsForNodeAttachment);
							if (this.additionalThreadsForNodeAttachment > this.activeJobs.get()) {
								poolSlotFree = true;
							}
							this.bfLogger.trace("Number of active jobs is now {}", this.activeJobs.get());
							if (!poolSlotFree) {
								this.bfLogger.trace("Releasing activeJobsCounterLock for a wait.");
								try {
									haveLock = false;
									this.numberOfActiveJobsHasChanged.await();
									haveLock = true;
								} catch (InterruptedException e) { // if we are interrupted during a wait, we must still conduct a controlled shutdown
									this.bfLogger.debug("Received an interrupt while waiting for number of active jobs to change.");
									this.activeJobsCounterLock.unlock();
									Thread.currentThread().interrupt();
									this.checkAndConductTermination();
								}
								this.bfLogger.trace("Re-acquired activeJobsCounterLock after a wait.");
								this.bfLogger.debug("Number of active jobs has changed. Let's see whether we can enter now ...");
							}
						} finally {
							if (haveLock) {
								this.bfLogger.trace("Trying to unlock activeJobsCounterLock");
								this.activeJobsCounterLock.unlock();
								haveLock = false;
								this.bfLogger.trace("Released activeJobsCounterLock after read.");
							} else {
								this.bfLogger.trace("Don't need to give lock free, because we came to the finally-block via an exception.");
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
							this.bfLogger.info("No event was returned and there are no pending solutions. Number of active jobs: {}. Setting state to inactive.", this.activeJobs.get());
							return this.terminate();
						}
					}
				}

				if (!(event instanceof ISolutionCandidateFoundEvent)) {
					this.post(event);
				}
				return event;

			default:
				throw new IllegalStateException("BestFirst search is in state " + this.getState() + " in which next must not be called!");
			}
		} finally {
			this.unregisterActiveThread();
		}
	}

	public void selectNodeForNextExpansion(final N node) throws InterruptedException {
		this.selectNodeForNextExpansion(this.ext2int.get(node));
	}

	@SuppressWarnings("unchecked")
	public NodeExpansionJobSubmittedEvent<N, A, V> nextNodeExpansion() {
		while (this.hasNext()) {
			IAlgorithmEvent e = this.next();
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
				loopCondition = this.open.peek().getScore().compareTo(currentlyBestScore) < 0;
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

	protected void afterSelection(final BackPointerPath<N, A, V> node) {
		/* intentionally left blank */
	}

	protected void beforeExpansion(final BackPointerPath<N, A, V> node) {
		/* intentionally left blank */
	}

	protected void afterExpansion(final BackPointerPath<N, A, V> node) {
		/* intentionally left blank */
	}

	/** BLOCK D: Getters and Setters **/

	public List<N> getCurrentPathToNode(final N node) {
		return this.ext2int.get(node).getNodes();
	}

	public IPathEvaluator<N, A, V> getNodeEvaluator() {
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

		int threadsForAlgorithm = (this.getConfig().threads() >= 0) ? this.getConfig().threads() : this.getConfig().cpus();

		this.additionalThreadsForNodeAttachment = threadsForExpansion;
		if (this.additionalThreadsForNodeAttachment > threadsForAlgorithm - 2) { // timer and main thread must not add up here
			this.additionalThreadsForNodeAttachment = Math.min(this.additionalThreadsForNodeAttachment, threadsForAlgorithm - 2);
		}
		if (this.additionalThreadsForNodeAttachment < 1) {
			this.bfLogger.info("Effectively not parallelizing, since only {} threads are allowed by configuration, and 2 are needed for control and maintenance.", threadsForAlgorithm);
			this.additionalThreadsForNodeAttachment = 0;
			return;
		}
		AtomicInteger counter = new AtomicInteger(0);
		this.pool = Executors.newFixedThreadPool(this.additionalThreadsForNodeAttachment, r -> {
			Thread t = new Thread(r);
			t.setName("ORGraphSearch-worker-" + counter.incrementAndGet());
			this.threadsOfPool.add(t);
			return t;
		});
	}

	public int getTimeoutForComputationOfF() {
		return this.timeoutForComputationOfF;
	}

	public void setTimeoutForComputationOfF(final int timeoutInMS, final IPathEvaluator<N, A, V> timeoutEvaluator) {
		this.timeoutForComputationOfF = timeoutInMS;
		this.timeoutNodeEvaluator = timeoutEvaluator;
	}

	/**
	 * @return the openCollection
	 */
	public List<BackPointerPath<N, A, V>> getOpen() {
		return Collections.unmodifiableList(new ArrayList<>(this.open));
	}

	public BackPointerPath<N, A, V> getInternalRepresentationOf(final N node) {
		return this.ext2int.get(node);
	}

	/**
	 * @param open
	 *            the openCollection to set
	 */
	public void setOpen(final Queue<BackPointerPath<N, A, V>> collection) {
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
		this.bfLogger.info("Switching logger from {} to {}", this.bfLogger.getName(), name);
		this.loggerName = name;
		this.bfLogger = LoggerFactory.getLogger(name);
		this.bfLogger.info("Activated logger {} with name {}", name, this.bfLogger.getName());
		if (this.nodeEvaluator instanceof ILoggingCustomizable) {
			this.bfLogger.info("Setting logger of node evaluator {} to {}.nodeevaluator", this.nodeEvaluator, name);
			((ILoggingCustomizable) this.nodeEvaluator).setLoggerName(name + ".nodeevaluator");
		} else {
			this.bfLogger.info("Node evaluator {} does not implement ILoggingCustomizable, so its logger won't be customized.", this.nodeEvaluator);
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

	public V getFValue(final BackPointerPath<N, A, V> node) {
		return node.getScore();
	}

	public Map<String, Object> getNodeAnnotations(final N node) {
		BackPointerPath<N, A, V> intNode = this.ext2int.get(node);
		return intNode.getAnnotations();
	}

	public Object getNodeAnnotation(final N node, final String annotation) {
		BackPointerPath<N, A, V> intNode = this.ext2int.get(node);
		return intNode.getAnnotation(annotation);
	}

	@Subscribe
	public void onFValueReceivedEvent(final FValueEvent<V> event) {
		this.post(event);
	}

	@Override
	public IBestFirstConfig getConfig() {
		return (IBestFirstConfig) super.getConfig();
	}

	public String toDetailedString() {
		Map<String, Object> fields = new HashMap<>();
		fields.put("graphGenerator", this.graphGenerator);
		fields.put("nodeEvaluator", this.nodeEvaluator);
		return ToJSONStringUtil.toJSONString(this.getClass().getSimpleName(), fields);
	}

	@Override
	public String toString() {
		return this.getId();
	}
}