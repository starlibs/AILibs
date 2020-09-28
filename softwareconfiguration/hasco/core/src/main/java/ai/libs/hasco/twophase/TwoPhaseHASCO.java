package ai.libs.hasco.twophase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.aeonbits.owner.ConfigFactory;
import org.api4.java.ai.graphsearch.problem.IPathSearchInput;
import org.api4.java.algorithm.events.IAlgorithmEvent;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.api4.java.common.attributedobjects.IObjectEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

import ai.libs.hasco.core.HASCO;
import ai.libs.hasco.core.HASCOSolutionCandidate;
import ai.libs.hasco.core.events.HASCOSolutionEvent;
import ai.libs.hasco.core.events.TwoPhaseHASCOPhaseSwitchEvent;
import ai.libs.jaicore.basic.MathExt;
import ai.libs.jaicore.basic.algorithm.AlgorithmFinishedEvent;
import ai.libs.jaicore.basic.algorithm.AlgorithmInitializedEvent;
import ai.libs.jaicore.basic.sets.SetUtil;
import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.components.optimizingfactory.SoftwareConfigurationAlgorithm;
import ai.libs.jaicore.components.serialization.ComponentSerialization;
import ai.libs.jaicore.concurrent.GlobalTimer;
import ai.libs.jaicore.concurrent.NamedTimerTask;
import ai.libs.jaicore.logging.ToJSONStringUtil;

public class TwoPhaseHASCO<N, A> extends SoftwareConfigurationAlgorithm<TwoPhaseSoftwareConfigurationProblem, HASCOSolutionCandidate<Double>, Double> {

	private static final String SUFFIX_HASCO = ".hasco";

	/* logging */
	private Logger logger = LoggerFactory.getLogger(TwoPhaseHASCO.class);
	private String loggerName;

	/* HASCO configuration */
	private HASCO<N, A, Double> hasco;
	private NamedTimerTask phase1CancellationTask;

	/** The solution selected during selection phase. */
	private final Queue<HASCOSolutionCandidate<Double>> phase1ResultQueue = new LinkedBlockingQueue<>();
	private final Map<HASCOSolutionCandidate<Double>, TwoPhaseCandidateEvaluator> selectionRuns = new HashMap<>();
	private HASCOSolutionCandidate<Double> selectedHASCOSolution;

	private final double blowupInSelection;
	private final double blowupInPostProcessing;

	private final ComponentSerialization serializer = new ComponentSerialization();

	/* statistics */
	private long timeOfStart = -1;
	private int secondsSpentInPhase1;

	@Override
	public String toString() {
		Map<String, Object> fields = new HashMap<>();
		fields.put("hasco", this.hasco);
		fields.put("phase1ResultQueue", this.phase1ResultQueue);
		fields.put("selectedHASCOSolution", this.selectedHASCOSolution);
		fields.put("timeOfStart", this.timeOfStart);
		fields.put("secondsSpentInPhase1", this.secondsSpentInPhase1);
		return ToJSONStringUtil.toJSONString(fields);
	}

	public TwoPhaseHASCO(final TwoPhaseSoftwareConfigurationProblem problem, final TwoPhaseHASCOConfig config) {
		super(config != null ? config : ConfigFactory.create(TwoPhaseHASCOConfig.class), problem);
		this.logger.info("Created TwoPhaseHASCO object.");
		this.blowupInSelection = this.getConfig().expectedBlowupInSelection();
		this.blowupInPostProcessing = this.getConfig().expectedBlowupInPostprocessing();
		if (Double.isNaN(this.blowupInSelection)) {
			throw new IllegalArgumentException("Blow-Up for selection phase not configured properly.");
		}
		if (Double.isNaN(this.blowupInPostProcessing)) {
			throw new IllegalArgumentException("Blow-Up for post-processing phase not configured properly.");
		}
	}

	public TwoPhaseHASCO(final TwoPhaseSoftwareConfigurationProblem problem, final TwoPhaseHASCOConfig config, final HASCO<N, A, Double> hasco) {
		this(problem, config);
		this.setHasco(hasco);
	}

	public void setHasco(final HASCO<N, A, Double> hasco) {
		this.hasco = hasco;
		this.setHASCOLoggerNameIfPossible();
		this.hasco.setConfig(this.getConfig());
		this.hasco.registerListener(new Object() {

			@Subscribe
			public void receiveHASCOEvent(final IAlgorithmEvent event) {

				/* forward the HASCO events and register solutions to update best seen solutions and fill up the queue */
				if (!(event instanceof AlgorithmInitializedEvent || event instanceof AlgorithmFinishedEvent)) {
					TwoPhaseHASCO.this.post(event);
				}
				if (event instanceof HASCOSolutionEvent) {
					@SuppressWarnings("unchecked")
					HASCOSolutionCandidate<Double> solution = ((HASCOSolutionEvent<Double>) event).getSolutionCandidate();
					TwoPhaseHASCO.this.updateBestSeenSolution(solution);
					TwoPhaseHASCO.this.logger.info("Received new solution {} with score {} and evaluation time {}ms", solution.getComponentInstance(), solution.getScore(), solution.getTimeToEvaluateCandidate());
					TwoPhaseHASCO.this.phase1ResultQueue.add(solution);
				}

			}
		}); // this is to register solutions during runtime
	}

	@Override
	public IAlgorithmEvent nextWithException() throws InterruptedException, AlgorithmTimeoutedException, AlgorithmException, AlgorithmExecutionCanceledException {
		this.logger.info("Stepping 2phase HASCO. Current state: {}", this.getState());
		switch (this.getState()) {
		case CREATED:
			if (this.hasco == null) {
				throw new IllegalStateException("Cannot start algorithm before HASCO has been set. Please set HASCO either in constructor or via the setter.");
			}
			this.timeOfStart = System.currentTimeMillis();
			AlgorithmInitializedEvent event = this.activate();
			this.logger.info(
					"Starting 2-Phase HASCO with the following setup:\n\tCPUs:{},\n\tTimeout: {}s\n\tTimeout per node evaluation: {}ms\n\tTimeout per candidate: {}ms\n\tNumber of Random Completions: {}\n\tExpected blow-ups are {} (selection) and {} (post-processing).\nThe search factory is: {}",
					this.getNumCPUs(), this.getTimeout().seconds(), this.getConfig().timeoutForNodeEvaluation(), this.getConfig().timeoutForCandidateEvaluation(), this.getConfig().numberOfRandomCompletions(),
					this.blowupInSelection, this.blowupInPostProcessing, this.hasco.getSearchFactory());
			this.setHASCOLoggerNameIfPossible();
			this.logger.info("Initialized HASCO with start time {}.", this.timeOfStart);
			return event;

			/* active is only one step in this model; this could be refined */
		case ACTIVE:

			/* phase 1: gather solutions */
			if (this.hasco.getTimeout().milliseconds() >= 0) {
				GlobalTimer timer = GlobalTimer.getInstance();
				this.phase1CancellationTask = new NamedTimerTask() {

					@Override
					public void exec() {

						try {
							/* check whether the algorithm has been shutdown, then also cancel this task */
							if (TwoPhaseHASCO.this.isShutdownInitialized()) {
								this.cancel();
								return;
							}

							/* check termination of phase 1 */
							int timeElapsed = (int) (System.currentTimeMillis() - TwoPhaseHASCO.this.timeOfStart);
							int timeRemaining = (int) TwoPhaseHASCO.this.hasco.getTimeout().milliseconds() - timeElapsed;
							if (timeRemaining < 2000 || TwoPhaseHASCO.this.shouldSearchTerminate(timeRemaining)) {
								TwoPhaseHASCO.this.logger.info("Canceling HASCO (first phase). {}ms remaining.", timeRemaining);
								TwoPhaseHASCO.this.hasco.cancel();
								TwoPhaseHASCO.this.logger.info("HASCO canceled successfully after {}ms", (System.currentTimeMillis() - TwoPhaseHASCO.this.timeOfStart) - timeElapsed);
								this.cancel();
							}
						} catch (Exception e) {
							TwoPhaseHASCO.this.logger.error("Observed {} while checking termination of phase 1. Stack trace is: {}", e.getClass().getName(),
									Arrays.stream(e.getStackTrace()).map(se -> "\n\t" + se.toString()).collect(Collectors.joining()));
						}
					}
				};
				this.phase1CancellationTask.setDescriptor("TwoPhaseHASCO task to check termination of phase 1");
				timer.scheduleAtFixedRate(this.phase1CancellationTask, 1000, 1000);
			}
			this.logger.info("Entering phase 1. Calling HASCO with timeout {}.", this.hasco.getTimeout());
			try {
				this.hasco.call();
			} catch (AlgorithmExecutionCanceledException e) {
				this.logger.info("HASCO has terminated due to a cancel.");
				if (this.isCanceled()) {
					throw new AlgorithmExecutionCanceledException(e.getDelay());
				}
			} catch (AlgorithmTimeoutedException e) {
				this.logger.warn("HASCO has timeouted. In fact, time to deadline is {}ms", this.getTimeout().milliseconds() - (System.currentTimeMillis() - this.timeOfStart));
			} finally {
				if (this.phase1CancellationTask != null) {
					this.phase1CancellationTask.cancel();
				}
			}
			this.secondsSpentInPhase1 = (int) Math.round((System.currentTimeMillis() - this.timeOfStart) / 1000.0);

			/* if there is no candidate, and the remaining time is very small, throw an AlgorithmTimeoutedException */
			this.logger.info("HASCO has finished. {} solutions were found.", this.phase1ResultQueue.size());
			if (this.phase1ResultQueue.isEmpty() && this.getRemainingTimeToDeadline().seconds() < 10) {
				this.logger.info("No solution found within phase 1. Throwing an AlgorithmTimeoutedException (This is conventional behavior for when an algorithm has not identified its solution when the timeout bound is hit.)");
				this.terminate(); // this sends the AlgorithmFinishedEvent
				throw new AlgorithmTimeoutedException(this.getRemainingTimeToDeadline().milliseconds() * -1);
			}

			/* phase 2: enter phase and set respective logs/events */
			IObjectEvaluator<?, Double> selectionBenchmark = this.getInput().getSelectionBenchmark();
			if (selectionBenchmark != null) {
				if (this.logger.isInfoEnabled()) {
					this.logger.info("Entering phase 2.");
					this.logger.debug("Solutions seen so far had the following (internal) errors and evaluation times (one per line): {}",
							this.phase1ResultQueue.stream()
							.map(e -> "\n\t" + MathExt.round(e.getScore(), 4) + " in " + e.getTimeToEvaluateCandidate() + "ms (" + this.serializer.serialize(e.getComponentInstance()) + ")")
							.collect(Collectors.joining()));
				}
				this.post(new TwoPhaseHASCOPhaseSwitchEvent(this));

				// Robustness check whether precondition of phase 2 is actually fulfilled.
				if (this.phase1ResultQueue.isEmpty()) {
					this.logger.error("Not a single solution found in the first phase. Thus, exit with exception.");
					throw new AlgorithmException("Not a single solution candidate could be found in the first phase. Please check your search space configuration and search phase benchmark carefully.");
				}
				this.checkAndConductTermination();

				/* phase 2: conduct it (select model) */
				this.selectedHASCOSolution = this.selectModel();
			} else {
				this.logger.info("Selection phase is disabled. Returning best result of phase 1.");
				final Optional<HASCOSolutionCandidate<Double>> bestSolutionOptional = this.phase1ResultQueue.stream().min((s1, s2) -> s1.getScore().compareTo(s2.getScore()));
				if (!bestSolutionOptional.isPresent()) {
					throw new IllegalStateException("Cannot select a model since phase 1 has not returned any result.");
				}
				this.selectedHASCOSolution = bestSolutionOptional.get();
			}
			this.setBestSeenSolution(this.selectedHASCOSolution);
			assert this.getBestSeenSolution().equals(this.selectedHASCOSolution);
			return this.terminate();

		default:
			throw new IllegalStateException("Cannot do anything in state " + this.getState());
		}
	}

	protected boolean shouldSearchTerminate(final long timeRemaining) {
		Collection<HASCOSolutionCandidate<Double>> currentSelection = this.getSelectionForPhase2();
		int estimateForRemainingRuntime = this.getExpectedTotalRemainingRuntimeForAGivenPool(currentSelection, true);
		boolean terminatePhase1 = estimateForRemainingRuntime + 5000 > timeRemaining;
		this.logger.debug("{}ms of the available time remaining in total, and we estimate a remaining runtime of {}ms. Terminate phase 1: {}", timeRemaining, estimateForRemainingRuntime, terminatePhase1);
		return terminatePhase1;
	}

	public synchronized List<HASCOSolutionCandidate<Double>> getSelectionForPhase2() {
		return this.getSelectionForPhase2(Integer.MAX_VALUE);
	}

	private static final double MAX_MARGIN_FROM_BEST = 0.03;

	private synchronized List<HASCOSolutionCandidate<Double>> getSelectionForPhase2(final int remainingTime) {
		if (this.getNumberOfConsideredSolutions() < 1) {
			throw new UnsupportedOperationException("Cannot determine candidates for phase 2 if their number is set to a value less than 1. Here, it has been set to " + this.getNumberOfConsideredSolutions());
		}

		/* some initial checks for cases where we do not really have to do anything */
		if (remainingTime < 0) {
			throw new IllegalArgumentException("Cannot do anything in negative time (" + remainingTime + "ms)");
		}
		HASCOSolutionCandidate<Double> internallyOptimalSolution = this.getBestSeenSolution();
		if (internallyOptimalSolution == null) {
			return new ArrayList<>();
		}

		/* compute k pipeline candidates (the k/2 best, and k/2 random ones that do not deviate too much from the best one) */
		double optimalInternalScore = internallyOptimalSolution.getScore();
		int bestK = (int) Math.ceil((double) this.getNumberOfConsideredSolutions() / 2);
		int randomK = this.getNumberOfConsideredSolutions() - bestK;
		Collection<HASCOSolutionCandidate<Double>> potentialCandidates = new ArrayList<>(this.phase1ResultQueue).stream().filter(solution -> solution.getScore() <= optimalInternalScore + MAX_MARGIN_FROM_BEST).collect(Collectors.toList());
		this.logger.debug("Computing {} best and {} random solutions for a max runtime of {}. Number of candidates that are at most {} worse than optimum {} is: {}/{}", bestK, randomK, remainingTime, MAX_MARGIN_FROM_BEST,
				optimalInternalScore, potentialCandidates.size(), this.phase1ResultQueue.size());
		List<HASCOSolutionCandidate<Double>> selectionCandidates = potentialCandidates.stream().limit(bestK).collect(Collectors.toList());
		List<HASCOSolutionCandidate<Double>> remainingCandidates = new ArrayList<>(SetUtil.difference(potentialCandidates, selectionCandidates));
		Collections.shuffle(remainingCandidates, new Random(this.getConfig().randomSeed()));
		selectionCandidates.addAll(remainingCandidates.stream().limit(randomK).collect(Collectors.toList()));
		if (this.logger.isTraceEnabled()) {
			this.logger.trace("Determined the following candidates for selection phase (in this order): {}", selectionCandidates.stream().map(c -> "\n\t" + c.getScore() + ": " + c.getComponentInstance()).collect(Collectors.joining()));
		}

		/* if the candidates can be evaluated in the remaining time, return all of them */
		int budget = this.getExpectedTotalRemainingRuntimeForAGivenPool(selectionCandidates, true);
		if (budget < remainingTime) {
			return selectionCandidates;
		}

		/* otherwise return as much as can be expectedly done in the time */
		List<HASCOSolutionCandidate<Double>> actuallySelectedSolutions = new ArrayList<>();
		int expectedRuntime;
		for (HASCOSolutionCandidate<Double> pl : selectionCandidates) {
			actuallySelectedSolutions.add(pl);
			expectedRuntime = this.getExpectedTotalRemainingRuntimeForAGivenPool(actuallySelectedSolutions, true);
			if (expectedRuntime > remainingTime && actuallySelectedSolutions.size() > 1) {
				this.logger.info("Not considering solution {} for phase 2, because the expected runtime of the whole thing would be {}/{}", pl, expectedRuntime, remainingTime);
				actuallySelectedSolutions.remove(pl);
			}
		}
		return actuallySelectedSolutions;
	}

	private int getInSearchEvaluationTimeOfSolutionSet(final Collection<HASCOSolutionCandidate<Double>> solutions) {
		return solutions.stream().map(HASCOSolutionCandidate::getTimeToEvaluateCandidate).mapToInt(x -> x).sum();
	}

	public int getExpectedTotalRemainingRuntimeForAGivenPool(final Collection<HASCOSolutionCandidate<Double>> solutions, final boolean assumeCurrentlyBestCandidateToBeSelected) {
		int timeForPhase2 = this.getExpectedRuntimeForPhase2ForAGivenPool(solutions);
		int timeForPostprocessing = 0;
		if (assumeCurrentlyBestCandidateToBeSelected && this.getBestSeenSolution() != null) {
			timeForPostprocessing = this.getPostprocessingTimeOfCurrentlyBest();
		} else {
			timeForPostprocessing = this.getMaximumPostprocessingTimeOfAnyPoolMember(solutions);
		}
		return timeForPhase2 + timeForPostprocessing;
	}

	public int getPostprocessingTimeOfCurrentlyBest() {
		return (int) Math.round(this.getBestSeenSolution().getTimeToEvaluateCandidate() * this.blowupInSelection * this.blowupInPostProcessing);
	}

	public int getMaximumPostprocessingTimeOfAnyPoolMember(final Collection<HASCOSolutionCandidate<Double>> solutions) {
		int max = 0;
		for (HASCOSolutionCandidate<Double> candidate : solutions) {
			int expectedPostProcessingTime = (int) Math.ceil(candidate.getTimeToEvaluateCandidate() * this.blowupInSelection * this.blowupInPostProcessing);
			max = Math.max(max, expectedPostProcessingTime);
		}
		return max;
	}

	public int getExpectedRuntimeForPhase2ForAGivenPool(final Collection<HASCOSolutionCandidate<Double>> solutions) {
		int inSearchMCEvalTime = this.getInSearchEvaluationTimeOfSolutionSet(solutions);
		int estimateEvaluationTimeForSelectionPhase = (int) (inSearchMCEvalTime * this.blowupInSelection);
		int usableCPUs = Math.min(this.getConfig().cpus(), solutions.size());
		int runtime = estimateEvaluationTimeForSelectionPhase / Math.max(1, usableCPUs);
		this.logger.debug("Expected runtime is {} = {} * {} / {} for a pool of size {}", runtime, inSearchMCEvalTime, this.blowupInSelection, usableCPUs, solutions.size());
		return runtime;
	}

	public HASCOSolutionCandidate<Double> getBestSolutionOfPhase1() {
		final Optional<HASCOSolutionCandidate<Double>> bestSolutionOptional = this.phase1ResultQueue.stream().min((s1, s2) -> s1.getScore().compareTo(s2.getScore()));
		if (!bestSolutionOptional.isPresent()) {
			throw new IllegalStateException("Cannot select a model since phase 1 has not returned any result.");
		}
		return bestSolutionOptional.get();
	}

	public List<HASCOSolutionCandidate<Double>> getEnsembleToSelectFromInPhase2() {
		if (this.getTimeout().seconds() <= 0) {
			return this.getSelectionForPhase2().stream().sorted((c1,c2) -> Double.compare(c1.getScore(), c2.getScore())).collect(Collectors.toList());
		}
		int remainingTime = (int) (this.getTimeout().milliseconds() - (System.currentTimeMillis() - this.timeOfStart));

		/* check remaining time, otherwise just return the solution with best F-Value. */
		if (remainingTime < 0) {
			HASCOSolutionCandidate<Double> bestSolution = this.getBestSolutionOfPhase1();
			double scoreOfBestSolution = bestSolution.getScore();
			this.logger.info("Timelimit is already exhausted, just returning a greedy solution that had internal error {}.", scoreOfBestSolution);
			return Arrays.asList(bestSolution);
		}

		/* Get a queue of solutions to perform selection evaluation for. */
		List<HASCOSolutionCandidate<Double>> ensembleToSelectFrom = this.getSelectionForPhase2(remainingTime); // should be ordered by scores already (at least the first k)
		int expectedTimeForPhase2 = this.getExpectedRuntimeForPhase2ForAGivenPool(ensembleToSelectFrom);
		int expectedPostprocessingTime = this.getPostprocessingTimeOfCurrentlyBest();
		int expectedMaximumRemainingRuntime = expectedTimeForPhase2 + expectedPostprocessingTime;
		remainingTime = (int) (this.getTimeout().milliseconds() - (System.currentTimeMillis() - this.timeOfStart));

		if (expectedMaximumRemainingRuntime > remainingTime) {
			this.logger.warn("Only {}ms remaining. We probably cannot make it in time.", remainingTime);
		}
		if (this.logger.isInfoEnabled()) {
			this.logger.info(
					"We expect phase 2 to consume {}ms for {} candidates, and post-processing is assumed to take at most {}ms, which is a total remaining runtime of {}ms. {}ms are permitted by timeout. The following candidates are considered (one per line with the internal error of phase 1): {}",
					expectedTimeForPhase2, ensembleToSelectFrom.size(), expectedPostprocessingTime, expectedMaximumRemainingRuntime, remainingTime,
					ensembleToSelectFrom.stream().map(e -> "\n\t" + MathExt.round(e.getScore(), 4) + " in " + e.getTimeToEvaluateCandidate() + "ms (" + this.serializer.serialize(e.getComponentInstance()) + ")")
					.collect(Collectors.joining()));
		}
		return ensembleToSelectFrom.stream().sorted((c1,c2) -> Double.compare(c1.getScore(), c2.getScore())).collect(Collectors.toList());
	}

	protected HASCOSolutionCandidate<Double> selectModel() throws InterruptedException {

		/* determine the models from which we want to select */
		this.logger.info("Starting with phase 2: Selection of final model among the {} solutions that were identified.", this.phase1ResultQueue.size());
		long startOfPhase2 = System.currentTimeMillis();
		List<HASCOSolutionCandidate<Double>> ensembleToSelectFrom = this.getEnsembleToSelectFromInPhase2();
		if (ensembleToSelectFrom.isEmpty()) {
			this.logger.warn("No solution contained in ensemble.");
			return null;
		}
		else if (ensembleToSelectFrom.size() == 1) {
			this.logger.info("No selection to make since there is only one candidate to select from.");
			return ensembleToSelectFrom.get(0);
		}

		/* setup the thread pool for evaluation */
		AtomicInteger evaluatorCounter = new AtomicInteger(0);
		int threadsForPool = this.getConfig().threads() < 1 ? this.getConfig().cpus() : this.getConfig().threads() - 1; // subtract one thread for the one that is currently active
		this.logger.info("Create a thread pool for phase 2 of size {}.", threadsForPool);
		ExecutorService pool = Executors.newFixedThreadPool(threadsForPool, r -> {
			Thread t = new Thread(r);
			t.setName("final-evaluator-" + evaluatorCounter.incrementAndGet());
			return t;
		});

		/* evaluate each candidate */
		final Semaphore sem = new Semaphore(0);
		long timestampOfDeadline = this.timeOfStart + this.getTimeout().milliseconds() - 2000;
		final IObjectEvaluator<IComponentInstance, Double> evaluator = this.getInput().getSelectionBenchmark();
		final double timeoutTolerance = TwoPhaseHASCO.this.getConfig().selectionPhaseTimeoutTolerance();
		final String loggerNameForWorkers = this.getLoggerName() + ".worker";
		for (HASCOSolutionCandidate<Double> c : ensembleToSelectFrom) {
			TwoPhaseCandidateEvaluator run = new TwoPhaseCandidateEvaluator(c, timestampOfDeadline, timeoutTolerance, this.blowupInSelection, this.blowupInPostProcessing, evaluator, sem);
			run.setLoggerName(loggerNameForWorkers);
			this.selectionRuns.put(c,  run);
			pool.submit(run);
		}

		/* now wait for results */
		int n = ensembleToSelectFrom.size();
		this.logger.info("Waiting for termination of {} computations running on {} threads.", n, this.getConfig().cpus());
		sem.acquire(n);
		long endOfPhase2 = System.currentTimeMillis();
		this.logger.info("Finished phase 2 within {}ms net. Total runtime was {}ms. Evaluated solutions {}/{}", endOfPhase2 - startOfPhase2, endOfPhase2 - this.timeOfStart, this.selectionRuns.size(), n);
		this.logger.debug("Shutting down thread pool");
		pool.shutdownNow();
		pool.awaitTermination(5, TimeUnit.SECONDS);
		if (!pool.isShutdown()) {
			this.logger.warn("Thread pool is not shut down yet!");
		}

		/* set chosen model */
		Optional<TwoPhaseCandidateEvaluator> bestEvaluatedSolution = this.getCandidateThatWouldCurrentlyBeSelectedWithinPhase2(this.selectionRuns);
		if (bestEvaluatedSolution.isPresent()) {
			TwoPhaseCandidateEvaluator selectedModel = bestEvaluatedSolution.get();
			HASCOSolutionCandidate<Double> solution = selectedModel.getSolution();
			this.logger.info("Selected a configuration: {}. Its internal score was {}. Selection score was {}", this.serializer.serialize(solution.getComponentInstance()), solution.getScore(), selectedModel.getSelectionScore());
			return solution;
		} else {
			this.logger.warn("Could not select any real solution in selection phase, just returning the best we have seen in HASCO.");
			return this.getBestSolutionOfPhase1();
		}
	}

	private synchronized Optional<TwoPhaseCandidateEvaluator> getCandidateThatWouldCurrentlyBeSelectedWithinPhase2(final Map<HASCOSolutionCandidate<Double>, TwoPhaseCandidateEvaluator> stats) {
		return stats.entrySet().stream().map(Entry::getValue).min((e1,e2) -> Double.compare(e1.getSelectionScore(), e2.getSelectionScore()));
	}

	public HASCO<N, A, Double> getHasco() {
		return this.hasco;
	}

	public Queue<HASCOSolutionCandidate<Double>> getPhase1ResultQueue() {
		return this.phase1ResultQueue;
	}

	public int getSecondsSpentInPhase1() {
		return this.secondsSpentInPhase1;
	}

	public Map<HASCOSolutionCandidate<Double>, TwoPhaseCandidateEvaluator> getSelectionPhaseEvaluationRunners() {
		return this.selectionRuns;
	}

	@Override
	public void shutdown() {
		this.logger.info("Received shutdown signal. Cancelling phase 1 timer and invoking shutdown on parent.");
		if (this.phase1CancellationTask != null) {
			this.phase1CancellationTask.cancel();
		}
		super.shutdown();
	}

	@Override
	public void cancel() {
		this.logger.info("Received cancel signal.");
		super.cancel();
		this.logger.debug("Cancelling HASCO");
		if (this.hasco != null) {
			this.hasco.cancel();
		}
		assert this.isCanceled() : "Cancel-flag is not true at the end of the cancel procedure!";
	}

	/**
	 * @return The solution candidate selected by TwoPhase HASCO
	 */
	public HASCOSolutionCandidate<Double> getSelectedSolutionCandidate() {
		return this.selectedHASCOSolution;
	}

	@Override
	public TwoPhaseHASCOConfig getConfig() {
		return (TwoPhaseHASCOConfig) super.getConfig();
	}

	/**
	 * @return The number of considered solutions in the selection phase.
	 */
	public int getNumberOfConsideredSolutions() {
		return this.getConfig().selectionNumConsideredSolutions();
	}

	/**
	 * @param numberOfConsideredSolutions
	 *            The number of considered solutions in the selection phase.
	 */
	public void setNumberOfConsideredSolutions(final int numberOfConsideredSolutions) {
		this.getConfig().setProperty(TwoPhaseHASCOConfig.K_SELECTION_NUM_CONSIDERED_SOLUTIONS, numberOfConsideredSolutions + "");
	}

	public IPathSearchInput<N, A> getGraphSearchInput() {
		if (this.hasco == null) {
			throw new IllegalStateException("Cannot retrieve GraphGenerator prior to algorithm initialization.");
		}
		if (this.hasco.getSearch() == null) {
			throw new IllegalStateException("Cannot retrieve GraphGenerator prior to algorithm initialization.");
		}
		return this.hasco.getSearch().getInput();
	}

	public TwoPhaseHASCOReport getReort() {
		return new TwoPhaseHASCOReport(this.phase1ResultQueue.size(), this.secondsSpentInPhase1, this.selectedHASCOSolution);
	}

	@Override
	public String getLoggerName() {
		return this.loggerName;
	}

	@Override
	public void setLoggerName(final String name) {
		this.loggerName = name;
		this.logger.info("Switching logger from {} to {}", this.logger.getName(), name);
		this.logger = LoggerFactory.getLogger(name);
		this.logger.info("Activated logger {} with name {}", name, this.logger.getName());
		this.serializer.setLoggerName(name + ".serializer");
		this.setHASCOLoggerNameIfPossible();
		super.setLoggerName(this.loggerName + "._orgraphsearch");
	}

	private void setHASCOLoggerNameIfPossible() {
		if (this.getLoggerName() == null) {
			return;
		}
		if (this.hasco == null) {
			this.logger.info("HASCO object is null, so not setting a logger.");
			return;
		}
		if (this.hasco.getLoggerName() != null && this.hasco.getLoggerName().equals(this.loggerName + SUFFIX_HASCO)) {
			this.logger.info("HASCO logger has already been customized correctly, not customizing again.");
			return;
		}
		this.logger.info("Setting logger of {} to {}{}", this.hasco.getId(), this.getLoggerName(), SUFFIX_HASCO);
		this.hasco.setLoggerName(this.getLoggerName() + SUFFIX_HASCO);
	}
}
