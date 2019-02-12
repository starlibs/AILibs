package hasco.variants.forwarddecomposition.twophase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.aeonbits.owner.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

import hasco.core.HASCO;
import hasco.core.HASCOSolutionCandidate;
import hasco.events.HASCOSolutionEvent;
import hasco.model.ComponentInstance;
import hasco.optimizingfactory.SoftwareConfigurationAlgorithm;
import hasco.variants.forwarddecomposition.DefaultPathPriorizingPredicate;
import jaicore.basic.IObjectEvaluator;
import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.events.AlgorithmFinishedEvent;
import jaicore.basic.algorithm.events.AlgorithmInitializedEvent;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.basic.sets.SetUtil;
import jaicore.concurrent.TimeoutTimer;
import jaicore.concurrent.TimeoutTimer.TimeoutSubmitter;
import jaicore.logging.LoggerUtil;
import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.probleminputs.GraphSearchInput;

public class TwoPhaseHASCO<ISearch extends GraphSearchInput<N, A>, N, A> extends SoftwareConfigurationAlgorithm<TwoPhaseSoftwareConfigurationProblem, HASCOSolutionCandidate<Double>, Double> {

	/* logging */
	private Logger logger = LoggerFactory.getLogger(TwoPhaseHASCO.class);
	private String loggerName;

	/* HASCO configuration */
	private HASCO<ISearch, N, A, Double> hasco;

	/** The solution selected during selection phase. */
	private final Queue<HASCOSolutionCandidate<Double>> phase1ResultQueue = new LinkedBlockingQueue<>();
	private HASCOSolutionCandidate<Double> selectedHASCOSolution;

	/* statistics */
	private long timeOfStart = -1;
	private int secondsSpentInPhase1;

	private Thread timeoutControl = null;

	public TwoPhaseHASCO(final TwoPhaseSoftwareConfigurationProblem problem, final TwoPhaseHASCOConfig config) {
		super(config != null ? config : ConfigFactory.create(TwoPhaseHASCOConfig.class), problem);
		logger.info("Created TwoPhaseHASCO object.");
	}
	
	public TwoPhaseHASCO(final TwoPhaseSoftwareConfigurationProblem problem, final TwoPhaseHASCOConfig config, HASCO<ISearch, N, A, Double> hasco) {
		this(problem, config);
		this.setHasco(hasco);
	}
	
	public void setHasco(HASCO<ISearch, N, A, Double> hasco) {
		this.hasco = hasco;
		if (this.getLoggerName() != null)
			this.hasco.setLoggerName(this.getLoggerName() + ".hasco");
		this.hasco.setConfig(this.getConfig());
		this.hasco.registerListener(new Object() {

			@Subscribe
			public void receiveHASCOEvent(AlgorithmEvent event) {
				
				/* forward the HASCO events and register solutions to update best seen solutions and fill up the queue */
				if (!(event instanceof AlgorithmInitializedEvent || event instanceof AlgorithmFinishedEvent))
					post(event);
				if (event instanceof HASCOSolutionEvent) {
					HASCOSolutionCandidate<Double> solution = ((HASCOSolutionEvent<Double>)event).getSolutionCandidate();
					updateBestSeenSolution(solution);
					logger.info("Received new solution {} with score {} and evaluation time {}ms", solution.getComponentInstance(), solution.getScore(), solution.getTimeToEvaluateCandidate());
					phase1ResultQueue.add(solution);
				}

			}
		}); // this is to register solutions during runtime		
	}

	@Override
	public AlgorithmEvent nextWithException() throws InterruptedException, TimeoutException, AlgorithmException {
		switch (this.getState()) {
		case created: {
			if (hasco == null)
				throw new IllegalStateException("Cannot start algorithm before HASCO has been set. Please set HASCO either in constructor or via the setter.");
			this.timeOfStart = System.currentTimeMillis();
			AlgorithmInitializedEvent event = this.activate();
			this.logger.info(
					"Starting 2-Phase HASCO with the following setup:\n\tCPUs:{},\n\tTimeout: {}s\n\tTimeout per node evaluation: {}ms\n\tTimeout per candidate: {}ms\n\tNumber of Random Completions: {}\n\tExpected blow-ups are {} (selection) and {} (post-processing).",
					this.getNumCPUs(), this.getTimeout(), this.getConfig().timeoutForNodeEvaluation(), this.getConfig().timeoutForCandidateEvaluation(), this.getConfig().randomCompletions(),
					this.getConfig().expectedBlowupInSelection(), this.getConfig().expectedBlowupInPostprocessing());

			/* create HASCO object */
			DefaultPathPriorizingPredicate<N, A> prioritizingPredicate = new DefaultPathPriorizingPredicate<>();

			/* set HASCO objects within the default path prioritizing node evaluator */
			prioritizingPredicate.setHasco(this.hasco);
			logger.info("Initialized HASCO with start time {}.", timeOfStart);
			return event;
		}

		/* active is only one step in this model; this could be refined */
		case active: {

			/* phase 1: gather solutions */
			this.timeoutControl = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						while (!Thread.currentThread().isInterrupted()) {
							Thread.sleep(1000);
							int timeElapsed = (int) (System.currentTimeMillis() - TwoPhaseHASCO.this.timeOfStart);
							int timeRemaining = (int) TwoPhaseHASCO.this.getTimeout().milliseconds() - timeElapsed;
							if (timeRemaining < 2000 || TwoPhaseHASCO.this.shouldSearchTerminate(timeRemaining)) {
								TwoPhaseHASCO.this.logger.info("Canceling HASCO (first phase). {}ms remaining.", timeRemaining);
								TwoPhaseHASCO.this.hasco.cancel();
								return;
							}
						}
					} catch (Exception e) {
						logger.error("Timeouter died away. This must not happen; killing the whole application. The exception responsible for this is:");
						e.printStackTrace();
						System.exit(1);
					}

				}
			}, "Phase 1 time bound observer");
			this.timeoutControl.start();
			logger.info("Entering phase 1. Calling HASCO with timeout {}.", hasco.getTimeout());
			try {
				this.hasco.call();
			} catch (AlgorithmExecutionCanceledException e) {
				this.logger.info("HASCO has terminated due to a cancel.");
			}
			this.secondsSpentInPhase1 = (int) Math.round(System.currentTimeMillis() - this.timeOfStart / 1000.0);

			this.logger.info("HASCO has finished. {} solutions were found.", this.phase1ResultQueue.size());
			if (this.phase1ResultQueue.isEmpty()) {
				throw new NoSuchElementException("No classifier could be built within the given timeout.");
			}

			/* phase 2: select model */
			this.logger.info("Entering phase 2");
			this.selectedHASCOSolution = this.selectModel();
			this.updateBestSeenSolution(this.selectedHASCOSolution);
			return this.terminate();
		}
		default:
			throw new IllegalStateException("Cannot do anything in state " + this.getState());
		}
	}

	protected boolean shouldSearchTerminate(final long timeRemaining) {
		Collection<HASCOSolutionCandidate<Double>> currentSelection = this.getSelectionForPhase2();
		int estimateForRemainingRuntime = this.getExpectedTotalRemainingRuntimeForAGivenPool(currentSelection, true);
		boolean terminatePhase1 = estimateForRemainingRuntime + 5000 > timeRemaining;
		this.logger.debug("{}ms of the available time remaining in total, and we estimate a remaining runtime of {}ms. Terminate phase 1: {}", timeRemaining, estimateForRemainingRuntime,
				terminatePhase1);
		return terminatePhase1;
	}

	private synchronized List<HASCOSolutionCandidate<Double>> getSelectionForPhase2() {
		return this.getSelectionForPhase2(Integer.MAX_VALUE);
	}

	private static final double MAX_MARGIN_FROM_BEST = 0.03;

	private synchronized List<HASCOSolutionCandidate<Double>> getSelectionForPhase2(final int remainingTime) {
		if (this.getNumberOfConsideredSolutions() < 1) {
			throw new UnsupportedOperationException(
					"Cannot determine candidates for phase 2 if their number is set to a value less than 1. Here, it has been set to " + this.getNumberOfConsideredSolutions());
		}

		/* some initial checks for cases where we do not really have to do anything */
		if (remainingTime < 0) {
			throw new IllegalArgumentException("Cannot do anything in negative time (" + remainingTime + "ms)");
		}
		HASCOSolutionCandidate<Double> internallyOptimalSolution = this.getBestSeenSolution();
		if (internallyOptimalSolution == null) {
			return new ArrayList<>();
		}

		/*
		 * compute k pipeline candidates (the k/2 best, and k/2 random ones that do not deviate too much from the best one)
		 */
		double optimalInternalScore = internallyOptimalSolution.getScore();
		int bestK = (int) Math.ceil(this.getNumberOfConsideredSolutions() / 2);
		int randomK = this.getNumberOfConsideredSolutions() - bestK;
		Collection<HASCOSolutionCandidate<Double>> potentialCandidates = new ArrayList<>(this.phase1ResultQueue).stream().filter(solution -> {
			return solution.getScore() <= optimalInternalScore + MAX_MARGIN_FROM_BEST;
		}).collect(Collectors.toList());
		this.logger.debug("Computing {} best and {} random solutions for a max runtime of {}. Number of candidates that are at most {} worse than optimum {} is: {}/{}", bestK, randomK, remainingTime,
				MAX_MARGIN_FROM_BEST, optimalInternalScore, potentialCandidates.size(), this.phase1ResultQueue.size());
		List<HASCOSolutionCandidate<Double>> selectionCandidates = potentialCandidates.stream().limit(bestK).collect(Collectors.toList());
		List<HASCOSolutionCandidate<Double>> remainingCandidates = new ArrayList<>(SetUtil.difference(potentialCandidates, selectionCandidates));
		Collections.shuffle(remainingCandidates, new Random(this.getConfig().randomSeed()));
		selectionCandidates.addAll(remainingCandidates.stream().limit(randomK).collect(Collectors.toList()));

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
		// assert this.getExpectedRuntimeForPhase2ForAGivenPool(actuallySelectedSolutions) > remainingTime : "Invalid result. Expected runtime is higher than it should be based on the computation.";
		return actuallySelectedSolutions;
	}

	private int getInSearchEvaluationTimeOfSolutionSet(final Collection<HASCOSolutionCandidate<Double>> solutions) {
		return solutions.stream().map(x -> x.getTimeToEvaluateCandidate()).mapToInt(x -> x).sum();
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
		return (int) Math.round(this.getBestSeenSolution().getTimeToEvaluateCandidate() * this.getConfig().expectedBlowupInSelection() * this.getConfig().expectedBlowupInPostprocessing());
	}

	public int getMaximumPostprocessingTimeOfAnyPoolMember(final Collection<HASCOSolutionCandidate<Double>> solutions) {
		int max = 0;
		for (HASCOSolutionCandidate<Double> candidate : solutions) {
			int expectedPostProcessingTime = (int) Math.ceil(candidate.getTimeToEvaluateCandidate() * this.getConfig().expectedBlowupInSelection() * this.getConfig().expectedBlowupInPostprocessing());
			max = Math.max(max, expectedPostProcessingTime);
		}
		return max;
	}

	public int getExpectedRuntimeForPhase2ForAGivenPool(final Collection<HASCOSolutionCandidate<Double>> solutions) {
		int inSearchMCEvalTime = this.getInSearchEvaluationTimeOfSolutionSet(solutions);
		int estimateEvaluationTimeForSelectionPhase = (int) (inSearchMCEvalTime * this.getConfig().expectedBlowupInSelection());
		int usableCPUs = Math.min(this.getConfig().cpus(), solutions.size());
		int runtime = estimateEvaluationTimeForSelectionPhase / Math.max(1, usableCPUs);
		this.logger.debug("Expected runtime is {} = {} * {} / {} for a pool of size {}", runtime, inSearchMCEvalTime, this.getConfig().expectedBlowupInSelection(), usableCPUs, solutions.size());
		return runtime;
	}

	protected HASCOSolutionCandidate<Double> selectModel() {
		final IObjectEvaluator<ComponentInstance, Double> evaluator = this.getInput().getSelectionBenchmark();
		final HASCOSolutionCandidate<Double> bestSolution = this.phase1ResultQueue.stream().min((s1, s2) -> s1.getScore().compareTo(s2.getScore())).get();
		double scoreOfBestSolution = bestSolution.getScore();

		/* determine the models from which we want to select */
		this.logger.info("Starting with phase 2: Selection of final model among the {} solutions that were identified.", this.phase1ResultQueue.size());
		long startOfPhase2 = System.currentTimeMillis();
		List<HASCOSolutionCandidate<Double>> ensembleToSelectFrom;
		if (this.getTimeout().seconds() > 0) {
			int remainingTime = (int) (this.getTimeout().milliseconds() - (System.currentTimeMillis() - this.timeOfStart));
			/* check remaining time, otherwise just return the solution with best F-Value. */
			if (remainingTime < 0) {
				this.logger.info("Timelimit is already exhausted, just returning a greedy solution that had internal error {}.", scoreOfBestSolution);
				return bestSolution;
			}

			/* Get a queue of solutions to perform selection evaluation for. */
			ensembleToSelectFrom = this.getSelectionForPhase2(remainingTime); // should be ordered by scores already (at least the first k)
			int expectedTimeForPhase2 = this.getExpectedRuntimeForPhase2ForAGivenPool(ensembleToSelectFrom);
			int expectedPostprocessingTime = this.getPostprocessingTimeOfCurrentlyBest();
			int expectedMaximumRemainingRuntime = expectedTimeForPhase2 + expectedPostprocessingTime;
			remainingTime = (int) (this.getTimeout().milliseconds() - (System.currentTimeMillis() - this.timeOfStart));

			if (expectedMaximumRemainingRuntime > remainingTime) {
				this.logger.warn("Only {}ms remaining. We probably cannot make it in time.", remainingTime);
			}
			this.logger.info(
					"We expect phase 2 to consume {}ms for {} candidates, and post-processing is assumed to take at most {}ms, which is a total remaining runtime of {}ms. {}ms are permitted by timeout. The following pipelines are considered: ",
					expectedTimeForPhase2, ensembleToSelectFrom.size(), expectedPostprocessingTime, expectedMaximumRemainingRuntime, remainingTime);
		} else {
			ensembleToSelectFrom = this.getSelectionForPhase2();
		}

		AtomicInteger evaluatorCounter = new AtomicInteger(0);

		this.logger.info("Create a thread pool for phase 2 of size {}.", this.getConfig().cpus());
		ExecutorService pool = Executors.newFixedThreadPool(this.getConfig().cpus(), r -> {
			Thread t = new Thread(r);
			t.setName("final-evaluator-" + evaluatorCounter.incrementAndGet());
			return t;
		});
		HASCOSolutionCandidate<Double> selectedModel = bestSolution; // backup solution
		final Semaphore sem = new Semaphore(0);
		long timestampOfDeadline = this.timeOfStart + this.getTimeout().milliseconds() - 2000;

		/* evaluate each candiate */
		List<Double> stats = new ArrayList<>();
		final TimeoutSubmitter ts = TimeoutTimer.getInstance().getSubmitter();
		ensembleToSelectFrom.forEach(c -> stats.add(Double.MAX_VALUE));

		int n = ensembleToSelectFrom.size();
		for (int i = 0; i < n; i++) {
			HASCOSolutionCandidate<Double> c = ensembleToSelectFrom.get(i);

			final int run = i;

			pool.submit(new Runnable() {
				@Override
				public void run() {
					long timestampStart = System.currentTimeMillis();

					/* Time needed to compute the score of this solution in phase 1 */
					int inSearchSolutionEvaluationTime = c.getTimeToEvaluateCandidate();

					/* We assume linear growth of the evaluation time here to estimate
					 * (A) time for selection phase,
					 * (B) time for post-processing the solution in case it gets selected. */
					int estimatedInSelectionSingleIterationEvaluationTime = (int) Math.round(inSearchSolutionEvaluationTime * TwoPhaseHASCO.this.getConfig().expectedBlowupInSelection());
					int estimatedPostProcessingTime = (int) Math.round(estimatedInSelectionSingleIterationEvaluationTime * TwoPhaseHASCO.this.getConfig().expectedBlowupInPostprocessing());
					int estimatedTotalEffortInCaseOfSelection = estimatedInSelectionSingleIterationEvaluationTime
							+ Math.max(estimatedPostProcessingTime, TwoPhaseHASCO.this.getPostprocessingTimeOfCurrentlyBest());
					TwoPhaseHASCO.this.logger.info(
							"During search, the currently chosen model {} had a total evaluation time of {}ms ({}ms per iteration). "
									+ "We estimate an evaluation in the selection phase to take {}ms, and the final build to take {}. " + "This yields a total time of {}ms.",
							c.getComponentInstance(), inSearchSolutionEvaluationTime, inSearchSolutionEvaluationTime, estimatedInSelectionSingleIterationEvaluationTime, estimatedPostProcessingTime,
							estimatedTotalEffortInCaseOfSelection);

					// /* Old computation as coded by fmohr: we assume a linear growth */
					// int evaluationTimeOfCurrentlyChosenCandidateInsideSearch = currentlyChosenSolution.getTimeToComputeScore();
					// int estimatedEvaluationTimeOfCurrentlyChosenCandidateInSelection = (int) Math.round(evaluationTimeOfCurrentlyChosenCandidateInsideSearch * config.expectedBlowupInSelection());
					// int estimatedPostprocessingTimeOfCurrentlyChosenCandidate = (int) Math.round(estimatedEvaluationTimeOfCurrentlyChosenCandidateInSelection * config.expectedBlowupInPostprocessing());
					// trainingTimeForChosenModelInsideSearch = inSearchSolutionEvaluationTime;
					// estimatedOverallTrainingTimeForChosenModel = estimatedFinalBuildTime;
					// expectedTrainingTimeOfThisModel = estimatedInSelectionSingleIterationEvaluationTime;

					/* Schedule a timeout for this evaluation, which is 10% over the estimated time */
					int timeoutForEvaluation = (int) (estimatedInSelectionSingleIterationEvaluationTime * (1 + TwoPhaseHASCO.this.getConfig().selectionPhaseTimeoutTolerance()));
					int taskId = ts.interruptMeAfterMS(timeoutForEvaluation);

					/* If we have a global timeout, check whether considering this model is feasible. */
					if (TwoPhaseHASCO.this.getTimeout().seconds() > 0) {
						int remainingTime = (int) (timestampOfDeadline - System.currentTimeMillis());
						if (estimatedTotalEffortInCaseOfSelection >= remainingTime) {
							TwoPhaseHASCO.this.logger.info(
									"Not evaluating solution {} anymore, because its insearch evaluation time was {}, expected evaluation time for selection is {}, and expected post-processing time is {}. This adds up to {}, which exceeds the remaining time of {}!",
									c.getComponentInstance(), c.getTimeToEvaluateCandidate(), estimatedInSelectionSingleIterationEvaluationTime, estimatedPostProcessingTime,
									estimatedTotalEffortInCaseOfSelection, remainingTime);
							sem.release();
							return;
						}
					}
					try {
						double selectionScore = evaluator.evaluate(c.getComponentInstance());
						long trueEvaluationTime = (System.currentTimeMillis() - timestampStart);
						TwoPhaseHASCO.this.logger.info("Evaluated candidate {} with score {} (score assigned by HASCO was {}). Time to evaluate was {}ms", c.getComponentInstance(), selectionScore,
								c.getScore(), trueEvaluationTime);
						stats.set(run, selectionScore);
					} catch (InterruptedException e) {
						TwoPhaseHASCO.this.logger.info("Selection eval of {} got interrupted after {}ms. Defined timeout was: {}ms", c.getComponentInstance(),
								(System.currentTimeMillis() - timestampStart), timeoutForEvaluation);
					} catch (Throwable e) {
						/* Print only an exception if it is not expected. */
						if (!e.getMessage().contains("Killed WEKA!")) {
							TwoPhaseHASCO.this.logger.error("Observed an exeption when trying to evaluate a candidate in the selection phase.\n{}", LoggerUtil.getExceptionInfo(e));
						}
					} finally {
						sem.release();
						TwoPhaseHASCO.this.logger.debug("Released. Sem state: {}", sem.availablePermits());
						if (taskId >= 0) {
							ts.cancelTimeout(taskId);
						}
					}
				}
			});
		}
		try {

			/* now wait for results */
			this.logger.info("Waiting for termination of {} threads that compute the selection scores.", n);
			sem.acquire(n);
			long endOfPhase2 = System.currentTimeMillis();
			this.logger.info("Finished phase 2 within {}ms net. Total runtime was {}ms. ", endOfPhase2 - startOfPhase2, endOfPhase2 - this.timeOfStart);
			this.logger.debug("Shutting down thread pool");
			pool.shutdownNow();
			pool.awaitTermination(5, TimeUnit.SECONDS);

			if (!pool.isShutdown()) {
				this.logger.warn("Thread pool is not shut down yet!");
			}

			ts.close();

			/* set chosen model */
			if (ensembleToSelectFrom.isEmpty()) {
				this.logger.warn("No solution contained in ensemble.");
			} else {
				int selectedModelIndex = this.getCandidateThatWouldCurrentlyBeSelectedWithinPhase2(ensembleToSelectFrom, stats, true);
				if (selectedModelIndex < 0) {
					throw new NoSuchElementException("Could not identify any solution.");
				}
				selectedModel = ensembleToSelectFrom.get(selectedModelIndex);
				// DescriptiveStatistics statsOfBest = stats.get(selectedModelIndex);
				this.logger.info("Selected a configuration: {}. Its internal score was {}. Selection score was {}", selectedModel.getComponentInstance(), selectedModel.getScore(),
						stats.get(selectedModelIndex));
			}

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return selectedModel;
	}

	private synchronized int getCandidateThatWouldCurrentlyBeSelectedWithinPhase2(final List<HASCOSolutionCandidate<Double>> ensembleToSelectFrom, final List<Double> stats,
			final boolean logComputations) {
		int selectedModel = -1;
		double best = Double.MAX_VALUE;
		for (int i = 0; i < ensembleToSelectFrom.size(); i++) {
			// HASCOSolutionCandidate<Double> candidate = ensembleToSelectFrom.get(i);
			// DescriptiveStatistics statsOfCandidate = stats.get(i);
			// if (statsOfCandidate.getN() == 0) {
			// if (logComputations) {
			// logger.info("Ignoring candidate {} because no results were obtained in selection phase.", candidate);
			// }
			// continue;
			// }
			// double avgError = statsOfCandidate.getMean() / 100f;
			// double quartileScore = statsOfCandidate.getPercentile(75) / 100;
			// double score = (avgError + quartileScore) / 2f;
			// if (logComputations) {
			// logger.info("Score of candidate {} is {} based on {} (avg) and {} (.75-pct) with {} samples", candidate, score, avgError, quartileScore, statsOfCandidate.getN());
			// }
			double score = stats.get(i);
			if (score < best) {
				best = score;
				selectedModel = i;
			}
		}
		return selectedModel;
	}

	public HASCO<ISearch, N, A, Double> getHasco() {
		return hasco;
	}

	@Override
	public void cancel() {
		super.cancel();
		this.timeoutControl.interrupt();
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

	public GraphGenerator<N, A> getGraphGenerator() {
		if (this.hasco == null) {
			throw new IllegalStateException("Cannot retrieve GraphGenerator prior to algorithm initialization.");
		}
		return this.hasco.getGraphGenerator();
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
		this.logger.info("Switching logger from {} to {}", this.logger.getName(), name);
		this.logger = LoggerFactory.getLogger(name);
		this.logger.info("Activated logger {} with name {}", name, this.logger.getName());
		super.setLoggerName(this.loggerName + "._orgraphsearch");
	}
}
