package ai.libs.jaicore.search.algorithms.standard.bestfirst.nodeevaluation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.IPathGoalTester;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.ICancelablePathEvaluator;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPotentiallyGraphDependentPathEvaluator;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPotentiallySolutionReportingPathEvaluator;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPotentiallyUncertaintyAnnotatingPathEvaluator;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IUncertaintySource;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.PathEvaluationException;
import org.api4.java.algorithm.IAlgorithm;
import org.api4.java.algorithm.Timeout;
import org.api4.java.algorithm.events.IAlgorithmEvent;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.api4.java.common.attributedobjects.IObjectEvaluator;
import org.api4.java.common.control.ILoggingCustomizable;
import org.api4.java.datastructure.graph.ILabeledPath;
import org.api4.java.datastructure.graph.implicit.IGraphGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

import ai.libs.jaicore.basic.algorithm.AlgorithmInitializedEvent;
import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.logging.LoggerUtil;
import ai.libs.jaicore.logging.ToJSONStringUtil;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.ENodeAnnotation;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.events.EvaluatedSearchSolutionCandidateFoundEvent;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.events.NodeAnnotationEvent;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.events.NodeExpansionCompletedEvent;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.events.RolloutEvent;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.exceptions.RCNEPathCompletionFailedException;
import ai.libs.jaicore.search.algorithms.standard.gbf.SolutionEventBus;
import ai.libs.jaicore.search.algorithms.standard.random.RandomSearch;
import ai.libs.jaicore.search.model.other.EvaluatedSearchGraphPath;
import ai.libs.jaicore.search.model.other.SearchGraphPath;
import ai.libs.jaicore.search.model.travesaltree.BackPointerPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;
import ai.libs.jaicore.timing.TimedComputation;

public class RandomCompletionBasedNodeEvaluator<T, A, V extends Comparable<V>> extends TimeAwareNodeEvaluator<T, A, V>
implements IPotentiallyGraphDependentPathEvaluator<T, A, V>, IPotentiallySolutionReportingPathEvaluator<T, A, V>, ICancelablePathEvaluator, IPotentiallyUncertaintyAnnotatingPathEvaluator<T, A, V>, ILoggingCustomizable {

	private static final IAlgorithm<?, ?> ALGORITHM = null;
	private static final boolean LOG_FAILURES_AS_ERRORS = false;

	private String loggerName;
	private Logger logger = LoggerFactory.getLogger(RandomCompletionBasedNodeEvaluator.class);

	private final int timeoutForSingleCompletionEvaluationInMS;

	protected Set<List<T>> unsuccessfulPaths = Collections.synchronizedSet(new HashSet<>());
	protected Set<ILabeledPath<T, A>> postedSolutions = new HashSet<>();
	protected Map<List<T>, Integer> timesToComputeEvaluations = new HashMap<>();

	protected Map<List<T>, V> scoresOfSolutionPaths = new ConcurrentHashMap<>();
	protected Map<ILabeledPath<T, A>, V> fValues = new ConcurrentHashMap<>();
	protected Map<String, Integer> ppFails = new ConcurrentHashMap<>();
	protected Map<String, Integer> plFails = new ConcurrentHashMap<>();
	protected Map<String, Integer> plSuccesses = new ConcurrentHashMap<>();

	protected T root;
	protected IGraphGenerator<T, A> generator;
	protected IPathGoalTester<T, A> goalTester;
	protected long timestampOfFirstEvaluation;

	/* algorithm parameters */
	protected final Random random;
	protected final int desiredNumberOfSuccesfulSamples;
	protected final int maxSamples;
	private final Predicate<T> priorityPredicateForRDFS;

	private RandomSearch<T, A> completer;
	private final Semaphore completerInsertionSemaphore = new Semaphore(0); // this is required since the step-method of the completer is asynchronous
	protected final IObjectEvaluator<ILabeledPath<T, A>, V> solutionEvaluator;
	protected IUncertaintySource<T, A, V> uncertaintySource;
	protected SolutionEventBus<T> eventBus = new SolutionEventBus<>();
	private final Map<List<T>, V> bestKnownScoreUnderNodeInCompleterGraph = new HashMap<>();
	private boolean visualizeSubSearch;

	public RandomCompletionBasedNodeEvaluator(final Random random, final int samples, final IObjectEvaluator<ILabeledPath<T, A>, V> solutionEvaluator) {
		this(random, samples, samples, solutionEvaluator, -1, -1);
	}

	public RandomCompletionBasedNodeEvaluator(final Random random, final int desiredNumberOfSuccessfulSamples, final int maxSamples, final IObjectEvaluator<ILabeledPath<T, A>, V> solutionEvaluator) {
		this(random, desiredNumberOfSuccessfulSamples, maxSamples, solutionEvaluator, -1, -1);
	}

	public RandomCompletionBasedNodeEvaluator(final Random random, final int desiredNumberOfSuccessfulSamples, final int maxSamples, final IObjectEvaluator<ILabeledPath<T, A>, V> solutionEvaluator,
			final int timeoutForSingleCompletionEvaluationInMS, final int timeoutForNodeEvaluationInMS) {
		this(random, desiredNumberOfSuccessfulSamples, maxSamples, solutionEvaluator, timeoutForSingleCompletionEvaluationInMS, timeoutForNodeEvaluationInMS, null);
	}

	public RandomCompletionBasedNodeEvaluator(final Random random, final int desiredNumberOfSuccessfulSamples, final int maxSamples, final IObjectEvaluator<ILabeledPath<T, A>, V> solutionEvaluator,
			final int timeoutForSingleCompletionEvaluationInMS, final int timeoutForNodeEvaluationInMS, final Predicate<T> priorityPredicateForRDFS) {
		super(timeoutForNodeEvaluationInMS);
		if (random == null) {
			throw new IllegalArgumentException("Random source must not be null!");
		}
		if (desiredNumberOfSuccessfulSamples <= 0) {
			throw new IllegalArgumentException("Sample size must be greater than 0!");
		}
		if (solutionEvaluator == null) {
			throw new IllegalArgumentException("Solution evaluator must not be null!");
		}

		this.random = random;
		this.desiredNumberOfSuccesfulSamples = desiredNumberOfSuccessfulSamples;
		this.maxSamples = maxSamples;
		this.solutionEvaluator = solutionEvaluator;
		this.timeoutForSingleCompletionEvaluationInMS = timeoutForSingleCompletionEvaluationInMS;
		this.priorityPredicateForRDFS = priorityPredicateForRDFS;

		this.logger.info("Initialized RandomCompletionEvaluator with timeout {}ms for single evaluations and {}ms in total per node", timeoutForSingleCompletionEvaluationInMS, timeoutForNodeEvaluationInMS);

		/* check whether assertions are on */
		assert this.logAssertionActivation();
	}

	private boolean logAssertionActivation() {
		StringBuilder sb = new StringBuilder();
		sb.append("Assertion remark:\n--------------------------------------------------------\n");
		sb.append("Assertions are activated.\n");
		sb.append("This may cause significant performance loss using ");
		sb.append(RandomCompletionBasedNodeEvaluator.class.getName());
		sb.append(".\nIf you are not in debugging mode, we strongly suggest to disable assertions.\n");
		sb.append("--------------------------------------------------------");
		this.logger.info("{}", sb);
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected V fTimeouted(final ILabeledPath<T, A> path, final int timeout) throws InterruptedException, PathEvaluationException {
		assert this.generator != null : "Cannot compute f as no generator has been set!";
		if (!(path instanceof BackPointerPath)) {
			throw new IllegalArgumentException("Random Completer currently can only work with backpointer-based paths.");
		}
		BackPointerPath<T, A, V> n = (BackPointerPath<T, A, V>) path;
		this.eventBus.post(new NodeAnnotationEvent<>(ALGORITHM, n.getHead(), "f-computing thread", Thread.currentThread().getName()));
		this.logger.info("Received request for f-value of node with hashCode {}. Number of subsamples will be {}, timeout for node evaluation is {}ms and for a single candidate is {}ms. Enable DEBUG for node details.", n.hashCode(),
				this.desiredNumberOfSuccesfulSamples, this.getTimeoutForNodeEvaluationInMS(), this.timeoutForSingleCompletionEvaluationInMS);
		this.logger.debug("Node details: {}", n);

		long startOfComputation = System.currentTimeMillis();
		long deadline = timeout > 0 ? startOfComputation + timeout : -1;
		if (this.timestampOfFirstEvaluation == 0) {
			this.timestampOfFirstEvaluation = startOfComputation;
		}
		if (!this.fValues.containsKey(n)) {

			/* abort if not graph generator is set */
			if (this.generator == null) {
				throw new IllegalStateException("Cannot compute f-values before the generator is set!");
			}

			/* compute path and partial plan belonging to the node */
			List<T> nodeList = n.getNodes();

			/* annotate node with estimated relative distance to optimal solution */
			if (this.eventBus == null) { // this is necessary if the node evaluator was stored to the disc
				this.eventBus = new SolutionEventBus<>();
			}
			double uncertainty = 0.0;
			if (!n.isGoal()) {

				/* if the node has no sibling (parent has no other child than this node), apply parent's f. This only works if the parent is already part of the explored graph, which is not necessarily the case */
				if (n.getParent() != null && this.completer.getExploredGraph().hasItem(n.getParent().getHead())) {
					boolean parentHasFValue = this.fValues.containsKey(n.getParent());
					assert parentHasFValue || n.getParent().getParent() == null : "No f-value has been stored for the parent of node with hash code " + n.hashCode() + " (hash code of parent is " + n.getParent().hashCode()
							+ ") whose f-value we may want to reuse.\nThis is only allowed for top-level nodes, but the actual path is: "
							+ n.path().stream().map(k -> "\n\t" + k.hashCode() + "\t(f-value: " + k.getScore() + ")").collect(Collectors.joining());
					boolean nodeHasSibling = this.completer.getExploredGraph().getSuccessors(n.getParent().getHead()).size() > 1;
					if (!n.isPoint() && !nodeHasSibling && parentHasFValue) {
						V score = this.fValues.get(n.getParent());
						this.fValues.put(n, score);
						this.logger.debug("Score {} of parent can be used since the last action did not affect the performance.", score);
						if (score == null) {
							this.logger.warn("Returning score NULL inherited from parent, this should not happen.");
						}
						if (this.uncertaintySource != null) { // inherit uncertainty from parent
							n.setAnnotation(ENodeAnnotation.F_UNCERTAINTY.name(), n.getParent().getAnnotation(ENodeAnnotation.F_UNCERTAINTY.name()));
						}
						assert !this.annotatesUncertainty() || n.getAnnotation(ENodeAnnotation.F_UNCERTAINTY.name()) != null : "No uncertainty has been annotated to node " + n + " even though we claim to annotate!";
						return score;
					}
				}

				/* draw random completions and determine best solution */
				AtomicInteger drawnSamples = new AtomicInteger();
				AtomicInteger successfulSamples = new AtomicInteger();
				int countedExceptions = 0;
				List<V> evaluations = new ArrayList<>();
				List<ILabeledPath<T, A>> completedPaths = new ArrayList<>();
				this.logger.debug("Now drawing {} successful examples but no more than {}", this.desiredNumberOfSuccesfulSamples, this.maxSamples);
				while (successfulSamples.get() < this.desiredNumberOfSuccesfulSamples) {
					this.logger.debug("Drawing next sample. {} samples have been drawn already, {} have been successful. Thread interruption state is: {}", drawnSamples, successfulSamples, Thread.currentThread().isInterrupted());
					this.checkInterruption();
					if (deadline > 0 && deadline < System.currentTimeMillis()) {
						this.logger.info("Deadline for random completions hit! Finishing node evaluation.");
						break;
					}

					/* determine time that is available to conduct next computation */
					long remainingTimeForNodeEvaluation = deadline > 0 ? deadline - System.currentTimeMillis() : -1; // this value is positive or -1 due to the previous check
					long timeoutForJob;
					if (remainingTimeForNodeEvaluation >= 0 && this.timeoutForSingleCompletionEvaluationInMS >= 0) {
						timeoutForJob = Math.min(remainingTimeForNodeEvaluation, this.timeoutForSingleCompletionEvaluationInMS);
					} else if (remainingTimeForNodeEvaluation >= 0) {
						timeoutForJob = remainingTimeForNodeEvaluation;
					} else if (this.timeoutForSingleCompletionEvaluationInMS >= 0) {
						timeoutForJob = this.timeoutForSingleCompletionEvaluationInMS;
					} else {
						timeoutForJob = -1;
					}

					/* complete the current path by the dfs-solution; we assume that this goes quickly */
					ILabeledPath<T, A> tmpCompletedPath = null;
					try {
						this.logger.debug("Computed remaining time for evaluation: {}ms. Timeout for the job is hence {}ms. Now drawing new solution.", remainingTimeForNodeEvaluation, timeoutForJob);
						tmpCompletedPath = this.getNextRandomPathCompletionForNode(n);
					} catch (RCNEPathCompletionFailedException e1) {
						if (e1.getCause() instanceof InterruptedException) {
							throw (InterruptedException) e1.getCause();
						}
						this.logger.info("Stopping sampling.");
						break;
					}
					final ILabeledPath<T, A> completedPath = tmpCompletedPath;
					completedPaths.add(completedPath);
					final int evaluationId = this.random.nextInt(1000000);
					this.logger.debug("Identified complete path with {} nodes. Now evaluating the path; assigning evaluation id {}", completedPath.getNumberOfNodes(), evaluationId);

					/* evaluate the found solution and update internal value model */
					try {
						this.logger.debug("Enqueuing timed computation with timeout {} and evaluation id {}", timeoutForJob, evaluationId);
						TimedComputation.compute(() -> {
							this.logger.debug("Starting timed computation with timeout {} and evaluation id {}", timeoutForJob, evaluationId);
							drawnSamples.incrementAndGet();
							V val = this.getFValueOfSolutionPath(completedPath);
							this.logger.debug("Completed path evaluation with id {}. Score is {}", evaluationId, val);
							successfulSamples.incrementAndGet();
							this.eventBus.post(new RolloutEvent<>(ALGORITHM, n.path(), val));
							if (val != null) {
								evaluations.add(val);
								this.updateMapOfBestScoreFoundSoFar(completedPath, val);
							} else {
								this.logger.warn("Got NULL result as score for evaluation with id {}", evaluationId);
							}
							return true;
						}, timeoutForJob, "RCNE-timeout for evaluation with id " + evaluationId);
					} catch (InterruptedException e) { // Interrupts are directly re-thrown
						this.logger.debug("Path evaluation has been interrupted.");
						throw e;
					}
					catch (Exception ex) {
						if (countedExceptions == this.maxSamples) {
							this.logger.warn("Too many retry attempts, giving up. {} samples were drawn, {} were successful.", drawnSamples, successfulSamples);
							throw new PathEvaluationException("Error in the evaluation of a node!", ex);
						} else {
							countedExceptions++;
							if (ex instanceof AlgorithmTimeoutedException) {
								this.logger.debug("Candidate evaluation failed due to timeout (either for this candidate or for the whole node).");
							}
							else {
								if (LOG_FAILURES_AS_ERRORS) {
									this.logger.error("Could not evaluate solution candidate ... retry another completion. {}", LoggerUtil.getExceptionInfo(ex));
								} else {
									this.logger.debug("Could not evaluate solution candidate ... retry another completion. {}", LoggerUtil.getExceptionInfo(ex));
								}
							}
						}
					} finally { // make sure that the abortion task is definitely killed
						this.logger.debug("Finished process for sample {}.", drawnSamples);
					}
				}

				/* the only reason why we have no score at this point is that all evaluations have failed with exception or were interrupted */
				V best = this.bestKnownScoreUnderNodeInCompleterGraph.get(n.getNodes());
				this.logger.debug("Finished sampling. {} samples were drawn, {} were successful. Best seen score is {}", drawnSamples, successfulSamples, best);
				if (best == null) {
					if (n.getHead().equals(this.root)) {
						this.logger.error("No completion could be drawn for the root. Apparently there is no solution in this graph!");
					}
					this.checkInterruption();
					if (countedExceptions > 0) {
						throw new NoSuchElementException("Among " + drawnSamples + " evaluated candidates, we could not identify any candidate that did not throw an exception.");
					} else {
						return null;
					}
				}

				/* if we are still interrupted, throw an exception */
				this.logger.debug("Checking interruption.");
				this.checkInterruption();
				this.logger.debug("Not interrupted.");

				/* add number of samples to node */
				n.setAnnotation("fRPSamples", successfulSamples);
				if (this.uncertaintySource != null) {
					uncertainty = this.uncertaintySource.calculateUncertainty(n, completedPaths, evaluations);
					this.logger.debug("Setting uncertainty to {}", uncertainty);
				} else {
					this.logger.debug("Not setting uncertainty, because no uncertainty source has been defined.");
				}
				this.fValues.put(n, best);
			}

			/* the node is a goal node */
			else {
				V score = this.getFValueOfSolutionPath(path);
				if (score == null) {
					this.logger.warn("No score was computed");
					return null;
				}
				this.fValues.put(n, score);
				if (!this.postedSolutions.contains(n)) {
					this.logger.error("Found a goal node whose solution has not been posted before!");
				}
				uncertainty = 0.0;
			}

			/* set uncertainty if an uncertainty source has been set */
			if (this.uncertaintySource != null) {
				n.setAnnotation(ENodeAnnotation.F_UNCERTAINTY.name(), uncertainty);
			}
		}
		assert this.fValues.containsKey(n);

		V f = this.fValues.get(n);
		this.logger.info("Returning f-value: {}. Annotated uncertainty is {}", f, n.getAnnotation(ENodeAnnotation.F_UNCERTAINTY.name()));
		return f;
	}

	public ILabeledPath<T, A> getNextRandomPathCompletionForNode(final BackPointerPath<T, A, ?> n) throws InterruptedException, RCNEPathCompletionFailedException {

		/* make sure that the completer has the path from the root to the node in question and that the f-values of the nodes above are added to the map */
		if (!this.completer.knowsNode(n.getHead())) {
			synchronized (this.completer) {
				this.completer.appendPathToNode(n);
			}
			BackPointerPath<T, A, ?> current = n.getParent();
			while (current != null && !this.fValues.containsKey(current)) {
				this.fValues.put(current, (V) current.getScore());
				this.logger.debug("Filling up the f-value of {} with {}", current.hashCode(), current.getScore());
				current = current.getParent();
			}
		}

		/* now draw random completion */
		ILabeledPath<T, A> pathCompletion = null;
		ILabeledPath<T, A> completedPath = null;
		synchronized (this.completer) {
			long startCompletion = System.currentTimeMillis();
			if (this.completer.isCanceled()) {
				this.logger.info("Completer has been canceled (perhaps due a cancel on the evaluator). Canceling sampling.");
				throw new RCNEPathCompletionFailedException("Completer has been canceled.");
			}
			this.logger.debug("Starting search for next solution ...");
			SearchGraphPath<T, A> solutionPathFromN = null;
			try {
				if (!this.completer.getExploredGraph().hasItem(n.getHead())) {
					throw new IllegalStateException("The completer does not know hte head.");
				}
				solutionPathFromN = this.completer.nextSolutionUnderSubPath(n);
			} catch (AlgorithmExecutionCanceledException | TimeoutException e) {
				this.logger.info("Completer has been canceled or timeouted. Returning control.");
				throw new RCNEPathCompletionFailedException(e);
			}
			if (solutionPathFromN == null) {
				this.logger.info("No completion was found for path {}.", n.getNodes());
				throw new RCNEPathCompletionFailedException("No completion found for path " + n.getNodes());
			}
			assert solutionPathFromN.getArcs() != null : "The RandomSearch has returned a solution path with a null pointer for the edges.";
			assert solutionPathFromN.getNumberOfNodes() == solutionPathFromN.getArcs().size() + 1;
			long finishedCompletion = System.currentTimeMillis();
			this.logger.debug("Found solution of length {} in {}ms. Enable TRACE for details.", solutionPathFromN.getNodes().size(), finishedCompletion - startCompletion);
			this.logger.trace("Solution path is {}", solutionPathFromN);
			pathCompletion = solutionPathFromN.getPathFromChildOfRoot();
			completedPath = new SearchGraphPath<>(n, pathCompletion, solutionPathFromN.getArcs() != null ? solutionPathFromN.getArcs().get(0) : null);
		}
		return completedPath;
	}

	private void updateMapOfBestScoreFoundSoFar(final ILabeledPath<T, A> nodeInCompleterGraph, final V scoreOnOriginalBenchmark) {
		V bestKnownScore = this.bestKnownScoreUnderNodeInCompleterGraph.get(nodeInCompleterGraph.getNodes());
		if (bestKnownScore == null || scoreOnOriginalBenchmark.compareTo(bestKnownScore) < 0) {
			this.logger.debug("Updating best score of path, because score {} is better than previously observed best score {}", scoreOnOriginalBenchmark, bestKnownScore);
			this.bestKnownScoreUnderNodeInCompleterGraph.put(nodeInCompleterGraph.getNodes(), scoreOnOriginalBenchmark);
			if (!nodeInCompleterGraph.isPoint()) {
				this.updateMapOfBestScoreFoundSoFar(nodeInCompleterGraph.getPathToParentOfHead(), scoreOnOriginalBenchmark);
			}
		}
	}

	protected V getFValueOfSolutionPath(final ILabeledPath<T, A> path) throws InterruptedException, PathEvaluationException {
		boolean knownPath = this.scoresOfSolutionPaths.containsKey(path.getNodes());
		if (!knownPath) {
			if (this.unsuccessfulPaths.contains(path.getNodes())) {
				this.logger.warn("Asking again for the reevaluation of a path that was evaluated unsuccessfully in a previous run; returning NULL: {}", path);
				return null;
			}
			this.logger.debug("Associated plan is new. Calling solution evaluator {} to compute f-value for path of length {}. Enable TRACE for exact plan.", this.solutionEvaluator.getClass().getName(), path.getNumberOfNodes());
			if (this.logger.isTraceEnabled()) {
				this.logger.trace("The hash code of the path is {}", path.hashCode());
			}

			/* compute value of solution */
			long start = System.currentTimeMillis();
			V val = null;
			try {
				val = this.solutionEvaluator.evaluate(new SearchGraphPath<>(path));
			} catch (InterruptedException e) {
				this.logger.info("Received interrupt during computation of f-value.");
				throw e;
			} catch (Exception e) {
				this.unsuccessfulPaths.add(path.getNodes());
				throw new PathEvaluationException("Error in evaluating node!", e);
			}
			long duration = System.currentTimeMillis() - start;
			if (duration >= this.timeoutForSingleCompletionEvaluationInMS) {
				this.logger.warn("Evaluation took {}ms, but timeout is {}", duration, this.timeoutForSingleCompletionEvaluationInMS);
				assert duration < this.timeoutForSingleCompletionEvaluationInMS + 10000 : "Evaluation took " + duration + "ms, but timeout is " + this.timeoutForSingleCompletionEvaluationInMS;
			}

			/* at this point, the value should not be NULL */
			this.logger.info("Result: {}, Size: {}", val, this.scoresOfSolutionPaths.size());
			if (val == null) {
				this.logger.warn("The solution evaluator has returned NULL, which should not happen.");
				this.unsuccessfulPaths.add(path.getNodes());
				return null;
			}

			this.scoresOfSolutionPaths.put(path.getNodes(), val);
			this.timesToComputeEvaluations.put(path.getNodes(), (int) duration);
			this.postSolution(path);
		} else {
			this.logger.info("Associated plan is known. Reading score from cache.");
			if (this.logger.isTraceEnabled()) {
				for (List<T> existingPath : this.scoresOfSolutionPaths.keySet()) {
					if (existingPath.equals(path)) {
						this.logger.trace("The following plans appear equal:\n\t{}\n\t{}", existingPath, path);
					}
				}
			}
			if (!this.postedSolutions.contains(path)) {
				throw new IllegalStateException("Reading cached score of a plan whose path has not been posted as a solution! Are there several paths to a plan?");
			}
		}
		V score = this.scoresOfSolutionPaths.get(path.getNodes());
		assert score != null : "Stored scores must never be null";
		this.logger.debug("Determined value {} for path of length {}.", score, path.getNumberOfNodes());
		this.logger.trace("Full path is {}", path);
		return score;
	}

	protected void postSolution(final ILabeledPath<T, A> solution) {
		assert !this.postedSolutions.contains(solution) : "Solution " + solution.toString() + " already posted!";
		assert this.goalTester.isGoal(solution) : "Last node is not a goal node!";
		this.postedSolutions.add(solution);
		try {

			/* now post the solution to the event bus */
			int numberOfComputedFValues = this.scoresOfSolutionPaths.size();

			/* post solution and then the annotations */
			if (this.eventBus == null) {
				this.eventBus = new SolutionEventBus<>();
			}
			EvaluatedSearchGraphPath<T, ?, V> solutionObject = new EvaluatedSearchGraphPath<>(solution, this.scoresOfSolutionPaths.get(solution.getNodes()));
			solutionObject.setAnnotation("fTime", this.timesToComputeEvaluations.get(solution.getNodes()));
			solutionObject.setAnnotation("timeToSolution", (int) (System.currentTimeMillis() - this.timestampOfFirstEvaluation));
			solutionObject.setAnnotation("nodesEvaluatedToSolution", numberOfComputedFValues);
			this.logger.debug("Posting solution {}", solutionObject);
			this.eventBus.post(new EvaluatedSearchSolutionCandidateFoundEvent<>(ALGORITHM, solutionObject));
		} catch (Exception e) {
			List<Pair<String, Object>> explanations = new ArrayList<>();
			if (this.logger.isDebugEnabled()) {
				StringBuilder sb = new StringBuilder();
				solution.getNodes().forEach(n -> sb.append(n.toString() + "\n"));
				explanations.add(new Pair<>("The path that has been tried to convert is as follows:", sb.toString()));
			}
			this.logger.error("Cannot post solution, because no valid MLPipeline object could be derived from it:\n{}", LoggerUtil.getExceptionInfo(e, explanations));
		}
	}

	@Override
	public void setGenerator(final IGraphGenerator<T, A> generator, final IPathGoalTester<T, A> goalTester) {
		this.generator = generator;
		this.root = generator.getRootGenerator().getRoots().iterator().next();
		this.goalTester = goalTester;

		/* create the completion algorithm and initialize it */
		IPathEvaluator<T, A, Double> nodeEvaluator = new RandomizedDepthFirstNodeEvaluator<>(this.random);
		GraphSearchWithSubpathEvaluationsInput<T, A, Double> completionProblem = new GraphSearchWithSubpathEvaluationsInput<>(this.generator, this.goalTester, nodeEvaluator);
		this.completer = new RandomSearch<>(completionProblem, this.priorityPredicateForRDFS, this.random);
		if (this.getTotalDeadline() >= 0) {
			this.completer.setTimeout(new Timeout(this.getTotalDeadline() - System.currentTimeMillis(), TimeUnit.MILLISECONDS));
		}
		if (this.loggerName != null) {
			this.completer.setLoggerName(this.loggerName + ".completer");
		}
		IAlgorithmEvent e = this.completer.next();
		assert e instanceof AlgorithmInitializedEvent : "First event of completer is not the initialization event!";
		this.logger.info("Generator has been set, and completer has been initialized");
	}

	@Subscribe
	public void receiveCompleterEvent(final NodeExpansionCompletedEvent<BackPointerPath<T, A, Double>> event) {
		this.completerInsertionSemaphore.release();
	}

	@Override
	public void registerSolutionListener(final Object listener) {
		this.eventBus.register(listener);
	}

	@Override
	public void cancelActiveTasks() {
		this.logger.info("Receive cancel signal. Canceling the completer.");
		this.completer.cancel();
	}

	@Override
	public void setUncertaintySource(final IUncertaintySource<T, A, V> uncertaintySource) {
		this.uncertaintySource = uncertaintySource;
	}

	public IObjectEvaluator<ILabeledPath<T, A>, V> getSolutionEvaluator() {
		return this.solutionEvaluator;
	}

	public boolean isVisualizeSubSearch() {
		return this.visualizeSubSearch;
	}

	public void setVisualizeSubSearch(final boolean visualizeSubSearch) {
		this.visualizeSubSearch = visualizeSubSearch;
	}

	@Override
	public void setLoggerName(final String name) {
		this.loggerName = name;
		this.logger.info("Switching logger (name) of object of class {} to {}", this.getClass().getName(), name);
		this.logger = LoggerFactory.getLogger(name);
		super.setLoggerName(name + "._parent");
		if (this.completer != null) {
			this.completer.setLoggerName(name + ".randomsearch");
		}
		if (this.solutionEvaluator instanceof ILoggingCustomizable) {
			this.logger.info("Setting logger of evaluator {} to {}.evaluator", this.solutionEvaluator.getClass().getName(), name);
			((ILoggingCustomizable) this.solutionEvaluator).setLoggerName(name + ".evaluator");
		}
		else {
			this.logger.info("Evaluator {} is not customizable for logger, so not configuring it.", this.solutionEvaluator.getClass().getName());
		}
		this.logger.info("Switched logger (name) of {} to {}", this, name);
		this.logger.info("Reprinting RandomCompletionEvaluator configuration after logger switch: timeout {}ms for single evaluations and {}ms in total per node", this.timeoutForSingleCompletionEvaluationInMS,
				this.getTimeoutForNodeEvaluationInMS());
	}

	@Override
	public String getLoggerName() {
		return this.loggerName;
	}

	@Override
	public String toString() {
		Map<String, Object> fields = new HashMap<>();
		fields.put("solutionEvaluator", this.solutionEvaluator);
		fields.put("visualizeSubSearch", this.visualizeSubSearch);
		return ToJSONStringUtil.toJSONString(this.getClass().getSimpleName(), fields);
	}

	@Override
	public boolean requiresGraphGenerator() {
		return true;
	}

	@Override
	public boolean reportsSolutions() {
		return true;
	}

	@Override
	public boolean annotatesUncertainty() {
		return this.uncertaintySource != null;
	}
}