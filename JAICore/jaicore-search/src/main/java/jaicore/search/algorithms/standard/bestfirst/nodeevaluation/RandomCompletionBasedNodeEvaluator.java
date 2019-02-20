package jaicore.search.algorithms.standard.bestfirst.nodeevaluation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

import jaicore.basic.ILoggingCustomizable;
import jaicore.basic.TimeOut;
import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.events.AlgorithmInitializedEvent;
import jaicore.basic.sets.SetUtil.Pair;
import jaicore.logging.LoggerUtil;
import jaicore.logging.ToJSONStringUtil;
import jaicore.search.algorithms.parallel.parallelexploration.distributed.interfaces.SerializableNodeEvaluator;
import jaicore.search.algorithms.standard.bestfirst.events.EvaluatedSearchSolutionCandidateFoundEvent;
import jaicore.search.algorithms.standard.bestfirst.events.NodeAnnotationEvent;
import jaicore.search.algorithms.standard.bestfirst.events.NodeExpansionCompletedEvent;
import jaicore.search.algorithms.standard.bestfirst.events.RolloutEvent;
import jaicore.search.algorithms.standard.bestfirst.exceptions.NodeEvaluationException;
import jaicore.search.algorithms.standard.gbf.SolutionEventBus;
import jaicore.search.algorithms.standard.random.RandomSearch;
import jaicore.search.algorithms.standard.uncertainty.IUncertaintySource;
import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.core.interfaces.ISolutionEvaluator;
import jaicore.search.model.other.EvaluatedSearchGraphPath;
import jaicore.search.model.other.SearchGraphPath;
import jaicore.search.model.travesaltree.Node;
import jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;

@SuppressWarnings("serial")
public class RandomCompletionBasedNodeEvaluator<T, V extends Comparable<V>> extends TimeAwareNodeEvaluator<T, V>
		implements IPotentiallyGraphDependentNodeEvaluator<T, V>, SerializableNodeEvaluator<T, V>,
		IPotentiallySolutionReportingNodeEvaluator<T, V>, ICancelableNodeEvaluator, IUncertaintyAnnotatingNodeEvaluator<T, V>,
		ILoggingCustomizable {

	private String loggerName;
	private Logger logger = LoggerFactory.getLogger(RandomCompletionBasedNodeEvaluator.class);

	private final int timeoutForSingleCompletionEvaluationInMS;

	protected Set<List<T>> unsuccessfulPaths = Collections.synchronizedSet(new HashSet<>());
	protected Set<List<T>> postedSolutions = new HashSet<>();
	protected Map<List<T>, Integer> timesToComputeEvaluations = new HashMap<>();

	protected Map<List<T>, V> scoresOfSolutionPaths = new ConcurrentHashMap<>();
	protected Map<Node<T, ?>, V> fValues = new ConcurrentHashMap<>();
	protected Map<String, Integer> ppFails = new ConcurrentHashMap<>();
	protected Map<String, Integer> plFails = new ConcurrentHashMap<>();
	protected Map<String, Integer> plSuccesses = new ConcurrentHashMap<>();

	protected GraphGenerator<T, String> generator;
	// private boolean generatorProvidesPathUnification;
	protected long timestampOfFirstEvaluation;

	/* algorithm parameters */
	protected final Random random;
	protected int samples;
	private final Predicate<T> priorityPredicateForRDFS;

	/* sub-tools for conducting and analyzing random completions */
	private Timer timeoutTimer;
	private RandomSearch<T, String> completer;
	private final Semaphore completerInsertionSemaphore = new Semaphore(0); // this is required since the step-method of
																			// the completer is asynchronous
	protected final ISolutionEvaluator<T, V> solutionEvaluator;
	protected IUncertaintySource<T, V> uncertaintySource;
	protected transient SolutionEventBus<T> eventBus = new SolutionEventBus<>();
	private final Map<List<T>, V> bestKnownScoreUnderNodeInCompleterGraph = new HashMap<>();
	private boolean visualizeSubSearch;

	public RandomCompletionBasedNodeEvaluator(final Random random, final int samples,
			final ISolutionEvaluator<T, V> solutionEvaluator) {
		this(random, samples, solutionEvaluator, -1, -1);
	}

	public RandomCompletionBasedNodeEvaluator(final Random random, final int samples,
			final ISolutionEvaluator<T, V> solutionEvaluator, final int timeoutForSingleCompletionEvaluationInMS,
			final int timeoutForNodeEvaluationInMS) {
		this(random, samples, solutionEvaluator, timeoutForSingleCompletionEvaluationInMS, timeoutForNodeEvaluationInMS,
				null);
	}

	public RandomCompletionBasedNodeEvaluator(final Random random, final int samples,
			final ISolutionEvaluator<T, V> solutionEvaluator, final int timeoutForSingleCompletionEvaluationInMS,
			final int timeoutForNodeEvaluationInMS, final Predicate<T> priorityPredicateForRDFS) {
		super(timeoutForNodeEvaluationInMS);
		if (random == null) {
			throw new IllegalArgumentException("Random source must not be null!");
		}
		if (samples <= 0) {
			throw new IllegalArgumentException("Sample size must be greater than 0!");
		}
		if (solutionEvaluator == null) {
			throw new IllegalArgumentException("Solution evaluator must not be null!");
		}

		this.random = random;
		this.samples = samples;
		this.solutionEvaluator = solutionEvaluator;
		this.timeoutForSingleCompletionEvaluationInMS = timeoutForSingleCompletionEvaluationInMS;
		this.priorityPredicateForRDFS = priorityPredicateForRDFS;

		this.logger.info(
				"Initialized RandomCompletionEvaluator with timeout {}ms for single evaluations and {}ms in total per node",
				timeoutForSingleCompletionEvaluationInMS, timeoutForNodeEvaluationInMS);

		/* check whether assertions are on */
		boolean assertOn = false;
		assert assertOn = true;
		if (assertOn) {
			System.out.println("--------------------------------------------------------");
			System.out.println("Attention: assertions are activated.");
			System.out.println("This causes significant performance loss using RandomCompleter.");
			System.out.println("If you are not in debugging mode, we strongly suggest to deactive assertions.");
			System.out.println("--------------------------------------------------------");
		}
	}

	public ISolutionEvaluator<T, V> getSolutionEvaluator() {
		return this.solutionEvaluator;
	}

	protected double getExpectedUpperBoundForRelativeDistanceToOptimalSolution(final Node<T, ?> n, final List<T> path) {
		return 0.0;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected V fTimeouted(final Node<T, ?> n, int timeout) throws InterruptedException, NodeEvaluationException {
		assert this.generator != null : "Cannot compute f as no generator has been set!";
		eventBus.post(new NodeAnnotationEvent<>("RandomCompletion", n.getPoint(), "f-computing thread",
				Thread.currentThread().getName()));
		this.logger.info(
				"Received request for f-value of node {}. Number of subsamples will be {}, timeout for node evaluation is {}ms and for a single candidate is {}ms.",
				n, this.samples, getTimeoutForNodeEvaluationInMS(), this.timeoutForSingleCompletionEvaluationInMS);
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
			List<T> path = n.externalPath();

			/* annotate node with estimated relative distance to optimal solution */
			if (this.eventBus == null) { // this is necessary if the node evaluator was stored to the disc
				this.eventBus = new SolutionEventBus<>();
			}
			this.eventBus.post(new NodeAnnotationEvent<>("RandomCompletion", n.getPoint(), "EUBRD2OS",
					this.getExpectedUpperBoundForRelativeDistanceToOptimalSolution(n, path)));
			double uncertainty = 0.0;
			if (!n.isGoal()) {

				/* if there was no relevant change in comparison to parent, apply parent's f */
				if (path.size() > 1 && !this.solutionEvaluator.doesLastActionAffectScoreOfAnySubsequentSolution(path)) {
					assert this.fValues.containsKey(n
							.getParent()) : "The solution evaluator tells that the solution on the path has not significantly changed, but no f-value has been stored before for the parent. The path is: "
									+ path;
					V score = this.fValues.get(n.getParent());
					this.fValues.put(n, score);
					this.logger.debug(
							"Score {} of parent can be used since the last action did not affect the performance.",
							score);
					if (score == null) {
						this.logger.warn("Returning score NULL inherited from parent, this should not happen.");
					}
					return score;
				}

				/*
				 * make sure that the completer has the path from the root to the node in
				 * question
				 */
				if (!this.completer.knowsNode(n.getPoint())) {
					synchronized (this.completer) {
						this.completer.appendPathToNode(n.externalPath());
					}
				}

				/* draw random completions and determine best solution */
				int successfulSamples = 0;
				int drawnSamples = 0;
				int countedExceptions = 0;
				final int maxSamples = this.samples * 2;
				List<V> evaluations = new ArrayList<>();
				List<List<T>> completedPaths = new ArrayList<>();
				this.logger.debug("Now drawing {} successful examples but no more than {}", this.samples, maxSamples);
				while (successfulSamples < this.samples) {
					logger.debug("Drawing next sample. {} samples have been drawn already, {} have been successful.", drawnSamples, successfulSamples);
					checkInterruption();
					if (deadline > 0 && deadline < System.currentTimeMillis()) {
						this.logger.info("Deadline for random completions hit! Finishing node evaluation.");
						break;
					}

					/* determine time that is available to conduct next computation */
					long remainingTimeForNodeEvaluation = deadline > 0 ? deadline - System.currentTimeMillis() : -1; // this
																														//value
																														// is
																														// positive
																														// or
																														// -1
																														// due
																														// to
																														// the
																														// previous
																														// check
					long timeoutForJob;
					if (remainingTimeForNodeEvaluation >= 0 && this.timeoutForSingleCompletionEvaluationInMS >= 0) {
						timeoutForJob = Math.min(remainingTimeForNodeEvaluation,
								this.timeoutForSingleCompletionEvaluationInMS);
					} else if (remainingTimeForNodeEvaluation >= 0) {
						timeoutForJob = remainingTimeForNodeEvaluation;
					} else if (this.timeoutForSingleCompletionEvaluationInMS >= 0) {
						timeoutForJob = this.timeoutForSingleCompletionEvaluationInMS;
					} else
						timeoutForJob = -1;

					/*
					 * complete the current path by the dfs-solution; we assume that this goes
					 * quickly
					 */
					List<T> pathCompletion = null;
					List<T> completedPath = null;
					synchronized (this.completer) {
						long startCompletion = System.currentTimeMillis();
						if (this.completer.isCanceled()) {
							this.logger.info("Completer has been canceled (perhaps due a cancel on the evaluator). Canceling sampling.");
							break;
						}
						completedPath = new ArrayList<>(n.externalPath());
						this.logger.debug("Starting search for next solution ...");
						SearchGraphPath<T, String> solutionPathFromN = null;
						try {
							solutionPathFromN = this.completer.nextSolutionUnderNode(n.getPoint());
						} catch (AlgorithmExecutionCanceledException | TimeoutException e) {
							this.logger.info("Completer has been canceled or timeouted. Returning control.");
							break;
						}
						if (solutionPathFromN == null) {
							this.logger.info("No completion was found for path {}.", path);
							break;
						}
						long finishedCompletion = System.currentTimeMillis();
						this.logger.debug("Found solution of length {} in {}ms. Enable TRACE for details.", solutionPathFromN.getNodes().size(), finishedCompletion - startCompletion);
						this.logger.trace("Solution path is {}", solutionPathFromN);
						pathCompletion = new ArrayList<>(solutionPathFromN.getNodes());
						pathCompletion.remove(0);
						completedPath.addAll(pathCompletion);
					}
					completedPaths.add(completedPath);

					/* setup timeout timer to interrupt evaluation */
					TimerTask abortionTask = null;
					AtomicBoolean nodeEvaluationTimedOut = new AtomicBoolean(false);
					if (timeoutForJob >= 0) {
						Thread executingThread = Thread.currentThread();
						abortionTask = new TimerTask() {
							@Override
							public void run() {

								/* if the executing thread has not been interrupted from outside */
								if (!executingThread.isInterrupted()) {
									RandomCompletionBasedNodeEvaluator.this.logger.info(
											"Sending an controlled interrupt to the evaluating thread to get it back here.");
									nodeEvaluationTimedOut.set(true);
									executingThread.interrupt();
								}
							}
						};
						if (this.timeoutTimer == null) {
							this.timeoutTimer = new Timer("RandomCompletion-Timeouter", true);
						}
						this.timeoutTimer.schedule(abortionTask, timeoutForJob);
						logger.debug("Activated timeout of {}ms for evaluation of found solution.", timeoutForJob);
					} else
						logger.debug("No timeout active for candidate evaluation.");

					/* evaluate the found solution */
					drawnSamples++;
					try {
						V val = this.getFValueOfSolutionPath(completedPath);
						successfulSamples++;
						this.eventBus.post(new RolloutEvent<>("RandomCompletion", n.path(), val));
						if (val != null) {
							evaluations.add(val);
							this.updateMapOfBestScoreFoundSoFar(completedPath, val);
						} else {
							this.logger.warn("Got NULL result as score for path {}", completedPath);
						}
					} catch (InterruptedException e) {
						boolean intentionalInterrupt = nodeEvaluationTimedOut.get();
						this.logger.info("Recognized {} interrupt", intentionalInterrupt ? "intentional" : "external");
						if (!intentionalInterrupt) {
							if (abortionTask != null)
								abortionTask.cancel();
							throw e;
						} else {
							Thread.interrupted(); // set interrupted to false
						}
					} catch (Exception ex) {
						if (countedExceptions == maxSamples) {
							this.logger.warn("Too many retry attempts, giving up. {} samples were drawn, {} were successful.", drawnSamples, successfulSamples);
							throw new NodeEvaluationException(ex, "Error in the evaluation of a node!");
						} else {
							countedExceptions++;
							this.logger.error("Could not evaluate solution candidate ... retry another completion. {}",
									LoggerUtil.getExceptionInfo(ex));
						}
					}
					abortionTask.cancel();
				}

				/*
				 * the only reason why we have no score at this point is that all evaluations
				 * have failed with exception or were interrupted
				 */
				V best = this.bestKnownScoreUnderNodeInCompleterGraph.get(n.externalPath());
				logger.debug("Finished sampling. {} samples were drawn, {} were successful. Best seen score is {}", drawnSamples, successfulSamples, best);
				if (best == null) {
					checkInterruption();
					if (countedExceptions > 0) {
						throw new NoSuchElementException("Among " + drawnSamples
								+ " evaluated candidates, we could not identify any candidate that did not throw an exception.");
					} else {
						return null;
					}
				}

				/* if we are still interrupted, throw an exception */
				checkInterruption();

				/* add number of samples to node */
				n.setAnnotation("fRPSamples", successfulSamples);
				if (this.uncertaintySource != null) {
					uncertainty = this.uncertaintySource.calculateUncertainty((Node<T, V>) n, completedPaths,
							evaluations);
				}

				/*
				 * cache this result and possible determine whether we already had a solution
				 * path that goes over this node before
				 */
				// if (rememberBestScores) {
				// if (generatorProvidesPathUnification) {
				// Optional<List<T>> bestPreviouslyKnownPathOverThisNode = Optional.empty();
				// AtomicBoolean interruptedInCheck = new AtomicBoolean();
				// PathUnifyingGraphGenerator<T, ?> castedGenerator =
				// (PathUnifyingGraphGenerator<T, ?>) generator;
				// bestPreviouslyKnownPathOverThisNode =
				// this.completions.keySet().stream().filter(p -> {
				// try {
				// if (interruptedInCheck.get())
				// return false;
				// return castedGenerator.isPathSemanticallySubsumed(path, p);
				// } catch (InterruptedException e) {
				// interruptedInCheck.set(true);
				// return false;
				// }
				// }).min((p1, p2) ->
				// this.scoresOfSolutionPaths.get(p1).compareTo(this.scoresOfSolutionPaths.get(p2)));
				// if (interruptedInCheck.get())
				// throw new InterruptedException("Node evaluation interrupted");
				// if (bestPreviouslyKnownPathOverThisNode.isPresent() &&
				// this.scoresOfSolutionPaths.get(bestPreviouslyKnownPathOverThisNode.get()).compareTo(best)
				// < 0) {
				// bestCompletion = bestPreviouslyKnownPathOverThisNode.get();
				// best = this.scoresOfSolutionPaths.get(bestCompletion);
				// }
				// }
				// }
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
				if (!this.postedSolutions.contains(path)) {
					this.logger.error("Found a goal node whose solution has not been posted before!");
				}
				uncertainty = 0.0;
			}

			/* set uncertainty if an uncertainty source has been set */
			if (this.uncertaintySource != null) {
				n.setAnnotation("uncertainty", uncertainty);
			}
		}
		V f = this.fValues.get(n);
		this.logger.info("Returning f-value: {}", f);
		return f;
	}

	private void updateMapOfBestScoreFoundSoFar(final List<T> nodeInCompleterGraph, final V scoreOnOriginalBenchmark) {
		V bestKnownScore = this.bestKnownScoreUnderNodeInCompleterGraph.get(nodeInCompleterGraph);
		if (bestKnownScore == null || scoreOnOriginalBenchmark.compareTo(bestKnownScore) < 0) {
			this.bestKnownScoreUnderNodeInCompleterGraph.put(nodeInCompleterGraph, scoreOnOriginalBenchmark);
			if (nodeInCompleterGraph.size() > 1) {
				this.updateMapOfBestScoreFoundSoFar(nodeInCompleterGraph.subList(0, nodeInCompleterGraph.size() - 1),
						scoreOnOriginalBenchmark);
			}
		}
	}

	protected V getFValueOfSolutionPath(final List<T> path) throws InterruptedException, NodeEvaluationException {
		boolean knownPath = this.scoresOfSolutionPaths.containsKey(path);
		if (!knownPath) {
			if (this.unsuccessfulPaths.contains(path)) {
				this.logger.warn(
						"Asking again for the reevaluation of a path that was evaluated unsuccessfully in a previous run; returning NULL: {}",
						path);
				return null;
			}
			this.logger.debug(
					"Associated plan is new. Calling solution evaluator {} to compute f-value for path of length {}. Enable TRACE for exact plan.",
					this.solutionEvaluator.getClass().getName(), path.size());
			this.logger.trace("The path is {}", path);

			/* compute value of solution */
			long start = System.currentTimeMillis();
			V val = null;
			try {
				val = this.solutionEvaluator.evaluateSolution(path);
			} catch (InterruptedException e) {
				this.logger.info("Received interrupt during computation of f-value of {}.", path);
				throw e;
			} catch (Throwable e) {
				this.logger.error(
						"Computing the solution quality of {} failed due to an exception. Here is the trace:\n\t{}\n\t{}\n\t{}",
						path, e.getClass().getName(), e.getMessage(), Arrays.asList(e.getStackTrace()).stream()
								.map(n -> "\n\t" + n.toString()).collect(Collectors.toList()));
				this.unsuccessfulPaths.add(path);
				throw new NodeEvaluationException(e, "Error in evaluating node!");
			}
			long duration = System.currentTimeMillis() - start;
			assert duration < timeoutForSingleCompletionEvaluationInMS + 10000 : "Evaluation took " + duration
					+ "ms, but timeout is " + timeoutForSingleCompletionEvaluationInMS;

			/* at this point, the value should not be NULL */
			this.logger.info("Result: {}, Size: {}", val, this.scoresOfSolutionPaths.size());
			if (val == null) {
				this.logger.warn("The solution evaluator has returned NULL, which should not happen.");
				this.unsuccessfulPaths.add(path);
				return null;
			}

			this.scoresOfSolutionPaths.put(path, val);
			this.timesToComputeEvaluations.put(path, (int) duration);
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
				throw new IllegalStateException(
						"Reading cached score of a plan whose path has not been posted as a solution! Are there several paths to a plan?");
			}
		}
		V score = this.scoresOfSolutionPaths.get(path);
		assert score != null : "Stored scores must never be null";
		this.logger.debug("Determined value {} for path of length {}.", score, path.size());
		this.logger.trace("Full path is {}", path);
		return score;
	}

	protected void postSolution(final List<T> solution) {
		assert !this.postedSolutions.contains(solution) : "Solution " + solution.toString() + " already posted!";
		this.postedSolutions.add(solution);
		try {

			/* now post the solution to the event bus */
			int numberOfComputedFValues = this.scoresOfSolutionPaths.size();

			/* post solution and then the annotations */
			if (this.eventBus == null) {
				this.eventBus = new SolutionEventBus<>();
			}
			EvaluatedSearchGraphPath<T, ?, V> solutionObject = new EvaluatedSearchGraphPath<>(solution, null,
					this.scoresOfSolutionPaths.get(solution));
			solutionObject.setAnnotation("fTime", this.timesToComputeEvaluations.get(solution));
			solutionObject.setAnnotation("timeToSolution",
					(int) (System.currentTimeMillis() - this.timestampOfFirstEvaluation));
			solutionObject.setAnnotation("nodesEvaluatedToSolution", numberOfComputedFValues);
			this.logger.debug("Posting solution {}", solutionObject);
			this.eventBus.post(new EvaluatedSearchSolutionCandidateFoundEvent<>("RandomCompletion", solutionObject));
		} catch (Throwable e) {
			List<Pair<String, Object>> explanations = new ArrayList<>();
			if (this.logger.isDebugEnabled()) {
				StringBuilder sb = new StringBuilder();
				solution.forEach(n -> sb.append(n.toString() + "\n"));
				explanations.add(new Pair<>("The path that has been tried to convert is as follows:", sb.toString()));
			}
			this.logger.error("Cannot post solution, because no valid MLPipeline object could be derived from it:\n{}",
					LoggerUtil.getExceptionInfo(e, explanations));
		}
	}

	@Override
	public void setGenerator(final GraphGenerator<T, ?> generator) {
		this.generator = (GraphGenerator<T, String>)generator;
		// this.generatorProvidesPathUnification = (this.generator instanceof
		// PathUnifyingGraphGenerator);
		// if (!this.generatorProvidesPathUnification)
		// logger.warn("The graph generator passed to the RandomCompletion algorithm
		// does not offer path subsumption checks, which may cause inefficiencies in
		// some domains.");

		/* create the completion algorithm and initialize it */
		INodeEvaluator<T, Double> nodeEvaluator = new RandomizedDepthFirstNodeEvaluator<>(this.random);
		GraphSearchWithSubpathEvaluationsInput<T, String, Double> completionProblem = new GraphSearchWithSubpathEvaluationsInput<>(
				this.generator, nodeEvaluator);
		this.completer = new RandomSearch<>(completionProblem, this.priorityPredicateForRDFS, this.random);
		if (this.getTotalDeadline() >= 0)
			this.completer.setTimeout(
					new TimeOut(this.getTotalDeadline() - System.currentTimeMillis(), TimeUnit.MILLISECONDS));
		if (loggerName != null)
			completer.setLoggerName(loggerName + ".completer");
		if (this.visualizeSubSearch) {
//			new VisualizationWindow<>(this.completer).setTooltipGenerator(n -> n.toString() + "<br />f: " + String.valueOf(this.bestKnownScoreUnderNodeInCompleterGraph.get(n)));
		}
		while (!(this.completer.next() instanceof AlgorithmInitializedEvent)) {
			;
		}
		this.logger.info("Generator has been set, and completer has been initialized");
	}

	@Subscribe
	public void receiveCompleterEvent(final NodeExpansionCompletedEvent<Node<T, Double>> event) {
		this.completerInsertionSemaphore.release();
	}

	@Override
	public void registerSolutionListener(final Object listener) {
		this.eventBus.register(listener);
	}

	@Override
	public void cancel() {
		this.logger.info("Receive cancel signal.");
		this.completer.cancel();
		if (this.timeoutTimer != null) {
			this.timeoutTimer.cancel();
		}
	}

	public void setNumberOfRandomCompletions(final int randomCompletions) {
		this.samples = randomCompletions;
	}

	@Override
	public void setUncertaintySource(final IUncertaintySource<T, V> uncertaintySource) {
		this.uncertaintySource = uncertaintySource;
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
		if (completer != null)
			this.completer.setLoggerName(name + ".randomsearch");
		this.logger.info("Switched logger (name) of {} to {}", this, name);
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
}