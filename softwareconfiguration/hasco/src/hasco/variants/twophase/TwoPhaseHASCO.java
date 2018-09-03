package hasco.variants.twophase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
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
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

import hasco.core.HASCORunReport;
import hasco.core.HASCOSolutionCandidate;
import hasco.core.RefinementConfiguredSoftwareConfigurationProblem;
import hasco.model.ComponentInstance;
import hasco.optimizingfactory.SoftwareConfigurationAlgorithm;
import hasco.variants.HASCOViaFDAndBestFirstWithRandomCompletions;
import jaicore.basic.ILoggingCustomizable;
import jaicore.basic.IObjectEvaluator;
import jaicore.basic.algorithm.AlgorithmEvent;
import jaicore.basic.algorithm.IAlgorithmConfig;
import jaicore.basic.algorithm.IOptimizerResult;
import jaicore.basic.algorithm.SolutionCandidateFoundEvent;
import jaicore.basic.sets.SetUtil;
import jaicore.concurrent.TimeoutTimer;
import jaicore.concurrent.TimeoutTimer.TimeoutSubmitter;
import jaicore.logging.LoggerUtil;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;

public class TwoPhaseHASCO implements SoftwareConfigurationAlgorithm<TwoPhaseSoftwareConfigurationProblem, TwoPhaseHASCOReport, Double>, ILoggingCustomizable {

	/** Logger for controlled outputs. */
	private Logger logger = LoggerFactory.getLogger(TwoPhaseHASCO.class);

	/** Name for configuring the output of this class' logger in a more convenient way. */
	private String loggerName;

	/** Caching factor to estimate runtimes for second phase. */
	private static final double CACHE_FACTOR = 0.8;

	/** Conservativeness factor to estimate runtimes for second phase. */
	private static final double CONSERVATIVENESS_FACTOR = 1.0;

	/* algorithm inputs */
	private final TwoPhaseSoftwareConfigurationProblem problem;
	private final TwoPhaseHASCOConfig config;
	private INodeEvaluator<TFDNode, Double> preferredNodeEvaluator;
	
	/** The classifier selected during selection phase. */
	private HASCOSolutionCandidate<Double> selectedHASCOSolution;

	/**  evaluator for the selection phase. */
	private HASCOViaFDAndBestFirstWithRandomCompletions<Double> hasco;
	private HASCORunReport<Double> hascoReport;
	
	/* state variables during the run */
	private HASCOSolutionCandidate<Double> currentlyBestKnownSolution;
	private final Queue<HASCOSolutionCandidate<Double>> phase1ResultQueue = new LinkedBlockingQueue<>();

	/** Timestamp when the search started. */
	private long timeOfStart = -1;

	private Thread timeoutControl = null;
	
	public TwoPhaseHASCO(TwoPhaseSoftwareConfigurationProblem problem, TwoPhaseHASCOConfig config) {
		if (problem == null)
			throw new IllegalArgumentException("Cannot work with NULL problem");
		this.problem = problem;
		this.config = config != null ? config : ConfigFactory.create(TwoPhaseHASCOConfig.class);
	}

	@Override
	public TwoPhaseHASCOReport call() throws Exception {
		this.timeOfStart = System.currentTimeMillis();
		this.logger.info("Starting 2-Phase HASCO with {} CPUs and a timeout of {}s. Preferred node evaluator is {}", getNumCPUs(), getTimeout(), preferredNodeEvaluator);

		/* phase 1: run HASCO to gather solutions */
		RefinementConfiguredSoftwareConfigurationProblem<Double> hascoProblem = new RefinementConfiguredSoftwareConfigurationProblem<>(problem, problem.getParamRefinementConfig());
		hasco = new HASCOViaFDAndBestFirstWithRandomCompletions<>(hascoProblem, config.randomCompletions(), config.randomSeed(), preferredNodeEvaluator);
		hasco.setLoggerName(loggerName + ".hasco");
		hasco.setConfig(config);
		hasco.registerListener(this); // this is to register solutions during runtime
		this.timeoutControl = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					while (!Thread.currentThread().isInterrupted()) {
						Thread.sleep(100);
						int timeElapsed = (int) (System.currentTimeMillis() - TwoPhaseHASCO.this.timeOfStart);
						int timeRemaining = config.timeout() * 1000 - timeElapsed;
						if (timeRemaining < 2000 || TwoPhaseHASCO.this.shouldSearchTerminate(timeRemaining)) {
							logger.info("Canceling HASCO (first phase). {}ms remaining.", timeRemaining);
							hasco.cancel();
							return;
						}
					}
				} catch (Exception e) {
					System.err.println("Timeouter died away. This must not happen; killing the whole application. The exception responsible for this is:");
					e.printStackTrace();
					System.exit(1);
				}

			}
		}, "Phase 1 time bound observer");
		this.timeoutControl.start();
		hascoReport = hasco.call();
		int secondsSpentInPhase1 = (int)Math.round(System.currentTimeMillis() - timeOfStart / 1000.0);

		this.logger.info("HASCO has finished. {} solutions were found.", hascoReport.getSolutionCandidates().size());
		if (phase1ResultQueue.isEmpty()) {
			throw new NoSuchElementException("No classifier could be built within the given timeout.");
		}
		
		/* phase 2: select model */
		logger.info("Entering phase 2");
		this.selectedHASCOSolution = this.selectModel();
		return new TwoPhaseHASCOReport(hascoReport.getSolutionCandidates().size(), secondsSpentInPhase1, selectedHASCOSolution);
	}

	protected boolean shouldSearchTerminate(final long timeRemaining) {
		Collection<HASCOSolutionCandidate<Double>> currentSelection = this.getSelectionForPhase2();
		int estimateForPhase2 = this.getExpectedRuntimeForPhase2ForAGivenPool(currentSelection);
		HASCOSolutionCandidate<Double> internallyOptimalSolution = currentlyBestKnownSolution;
		int timeToTrainBestSolutionOnEntireSet = internallyOptimalSolution != null ? (int) Math.round(internallyOptimalSolution.getTimeToComputeScore() * config.expectedBlowupInSelection()) : 0;
		boolean terminatePhase1 = estimateForPhase2 + timeToTrainBestSolutionOnEntireSet > timeRemaining;
		this.logger.debug("{}ms remaining in total, and we estimate {}ms for phase 2. Terminate phase 1: {}", timeRemaining, estimateForPhase2, terminatePhase1);
		return terminatePhase1;
	}

	private synchronized List<HASCOSolutionCandidate<Double>> getSelectionForPhase2() {
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
		HASCOSolutionCandidate<Double> internallyOptimalSolution = currentlyBestKnownSolution;
		if (internallyOptimalSolution == null) {
			return new ArrayList<>();
		}

		/*
		 * compute k pipeline candidates (the k/2 best, and k/2 random ones that do not deviate too much from the best one)
		 */
		double optimalInternalScore = internallyOptimalSolution.getScore();
		int bestK = (int) Math.ceil(this.getNumberOfConsideredSolutions() / 2);
		int randomK = this.getNumberOfConsideredSolutions() - bestK;
		Collection<HASCOSolutionCandidate<Double>> potentialCandidates = new ArrayList<>(phase1ResultQueue).stream().filter(solution -> {
			return solution.getScore() <= optimalInternalScore + MAX_MARGIN_FROM_BEST;
		}).collect(Collectors.toList());
		this.logger.debug("Computing {} best and {} random solutions for a max runtime of {}. Number of candidates that are at most {} worse than optimum {} is: {}/{}", bestK, randomK, remainingTime, MAX_MARGIN_FROM_BEST,
				optimalInternalScore, potentialCandidates.size(), phase1ResultQueue.size());
		List<HASCOSolutionCandidate<Double>> selectionCandidates = potentialCandidates.stream().limit(bestK).collect(Collectors.toList());
		List<HASCOSolutionCandidate<Double>> remainingCandidates = new ArrayList<>(SetUtil.difference(potentialCandidates, selectionCandidates));
		Collections.shuffle(remainingCandidates, new Random(this.getConfig().randomSeed()));
		selectionCandidates.addAll(remainingCandidates.stream().limit(randomK).collect(Collectors.toList()));

		/* if the candidates can be evaluated in the remaining time, return all of them */
		int budget = this.getExpectedRuntimeForPhase2ForAGivenPool(selectionCandidates);
		if (budget < remainingTime) {
			return selectionCandidates;
		}

		/* otherwise return as much as can be expectedly done in the time */
		List<HASCOSolutionCandidate<Double>> actuallySelectedSolutions = new ArrayList<>();
		int expectedRuntime;
		for (HASCOSolutionCandidate<Double> pl : selectionCandidates) {
			actuallySelectedSolutions.add(pl);
			expectedRuntime = this.getExpectedRuntimeForPhase2ForAGivenPool(actuallySelectedSolutions);
			if (expectedRuntime > remainingTime && actuallySelectedSolutions.size() > 1) {
				this.logger.info("Not considering solution {} for phase 2, because the expected runtime of the whole thing would be {}/{}", pl, expectedRuntime, remainingTime);
				actuallySelectedSolutions.remove(pl);
			}
		}
		assert this.getExpectedRuntimeForPhase2ForAGivenPool(actuallySelectedSolutions) > remainingTime : "Invalid result. Expected runtime is higher than it should be based on the computation.";
		return actuallySelectedSolutions;
	}

	private int getInSearchEvaluationTimeOfSolutionSet(final Collection<HASCOSolutionCandidate<Double>> solutions) {
		return solutions.stream().map(x -> x.getTimeToComputeScore()).mapToInt(x -> x).sum();
	}

	public int getExpectedRuntimeForPhase2ForAGivenPool(final Collection<HASCOSolutionCandidate<Double>> solutions) {
		int inSearchMCEvalTime = this.getInSearchEvaluationTimeOfSolutionSet(solutions);
		int estimateSelectionSingleIterationEvalTime = (int) (inSearchMCEvalTime * config.expectedBlowupInSelection());

		// train time was on only 70% of the data
		// double cacheFactor = Math.pow(getNumberOfCPUs(), -.6);
		int runtime = (int) (CONSERVATIVENESS_FACTOR * estimateSelectionSingleIterationEvalTime * config.expectedBlowupInSelection() * CACHE_FACTOR / this.getConfig().cpus());
		this.logger.debug("Expected runtime is {} = {} * {} * {} * {} / {} for a pool of size {}", runtime, CONSERVATIVENESS_FACTOR, estimateSelectionSingleIterationEvalTime, this.getConfig().expectedBlowupInSelection(), CACHE_FACTOR,
				this.getConfig().cpus(), solutions.size());
		return runtime;
	}

	protected HASCOSolutionCandidate<Double> selectModel() {
		final IObjectEvaluator<ComponentInstance, Double> evaluator = problem.getSelectionBenchmark();
		final HASCOSolutionCandidate<Double> bestSolution = phase1ResultQueue.stream().min((s1,s2) -> s1.getScore().compareTo(s2.getScore())).get();
		double scoreOfBestSolution = bestSolution.getScore();

		/* determine the models from which we want to select */
		this.logger.info("Starting with phase 2: Selection of final model among the {} solutions that were identified.", phase1ResultQueue.size());
		long startOfPhase2 = System.currentTimeMillis();
		List<HASCOSolutionCandidate<Double>> ensembleToSelectFrom;
		if (this.getConfig().timeout() > 0) {
			int remainingTime = (int) (this.getConfig().timeout() * 1000 - (System.currentTimeMillis() - this.timeOfStart));
			/*
			 * check remaining time, otherwise just return the solution with best F-Value.
			 */
			if (remainingTime < 0) {
				this.logger.info("Timelimit is already exhausted, just returning a greedy solution that had internal error {}.", scoreOfBestSolution);
				return bestSolution;
			}

			/* Get a queue of solutions to perform selection evaluation for. */
			ensembleToSelectFrom = this.getSelectionForPhase2(remainingTime); // should be ordered by f-value already
																				// (at least the first k)
			int expectedTimeForSolution;
			expectedTimeForSolution = this.getExpectedRuntimeForPhase2ForAGivenPool(ensembleToSelectFrom);
			remainingTime = (int) (this.getConfig().timeout() * 1000 - (System.currentTimeMillis() - this.timeOfStart));

			if (expectedTimeForSolution > remainingTime) {
				this.logger.warn("Only {}ms remaining. We probably cannot make it in time.", remainingTime);
			}
			this.logger.info("We expect phase 2 to consume {}ms for {} candidates. {}ms are permitted by timeout. The following pipelines are considered: ", expectedTimeForSolution, ensembleToSelectFrom.size(), remainingTime);
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
		long timestampOfDeadline = this.timeOfStart + this.getTimeout() * 1000;

		/* evaluate each candiate */
		List<DescriptiveStatistics> stats = new ArrayList<>();
		final TimeoutSubmitter ts = TimeoutTimer.getInstance().getSubmitter();
		ensembleToSelectFrom.forEach(c -> stats.add(new DescriptiveStatistics()));
		System.out.println(ensembleToSelectFrom);

		for (int i = 0; i < ensembleToSelectFrom.size(); i++) {
			HASCOSolutionCandidate<Double> c = ensembleToSelectFrom.get(i);
			final DescriptiveStatistics statsForThisCandidate = stats.get(i);

				pool.submit(new Runnable() {
					@Override
					public void run() {
						long timeStampStart = System.currentTimeMillis();

						int taskId = -1;
						HASCOSolutionCandidate<Double> currentlyChosenSolution = null;
						try {
							/* Get the HASCOClassificationMLSolution instance for the considered model. */
							int indexOfCurrentlyChosenModel = TwoPhaseHASCO.this.getCandidateThatWouldCurrentlyBeSelectedWithinPhase2(ensembleToSelectFrom, stats, false);
							currentlyChosenSolution = ensembleToSelectFrom.get(indexOfCurrentlyChosenModel);

							/* Time needed to compute the entire score. */
							int inSearchSolutionEvaluationTime = currentlyChosenSolution.getTimeToComputeScore();

							/* We assume linear growth of the classifier's evaluation time here to estimate
							 * (A) time for selection data,
							 * (B) time for building on the entire data provided for building. */
							int estimatedInSelectionSingleIterationEvaluationTime = (int) Math.round(inSearchSolutionEvaluationTime * config.expectedBlowupInSelection());
							int estimatedFinalBuildTime = (int)Math.round(estimatedInSelectionSingleIterationEvaluationTime * config.expectedBlowupInPostprocessing());
							int sumOfSelectionAndFinalBuild = estimatedFinalBuildTime + estimatedInSelectionSingleIterationEvaluationTime;

							TwoPhaseHASCO.this.logger.debug(
									"During search, the currently chosen model {} had a total evaluation time of {}ms ({}ms per iteration). "
											+ "We estimate an evaluation in the selection phase to take {}ms, and the final build to take {}. " + "This yields a total time of {}ms.",
									currentlyChosenSolution, inSearchSolutionEvaluationTime, inSearchSolutionEvaluationTime, estimatedInSelectionSingleIterationEvaluationTime,
									estimatedFinalBuildTime, sumOfSelectionAndFinalBuild);

							/* Old computation as coded by fmohr: we assume a linear growth */
							int trainingTimeForChosenModelInsideSearch = currentlyChosenSolution.getTimeToComputeScore();
							int estimatedOverallTrainingTimeForChosenModel = (int) Math.round(trainingTimeForChosenModelInsideSearch * config.expectedBlowupInSelection());
							int expectedTrainingTimeOfThisModel = (int) Math.round(c.getTimeToComputeScore() / .7);

							trainingTimeForChosenModelInsideSearch = inSearchSolutionEvaluationTime;
							estimatedOverallTrainingTimeForChosenModel = estimatedFinalBuildTime;
							expectedTrainingTimeOfThisModel = estimatedInSelectionSingleIterationEvaluationTime;

							/* Schedule a timeout for single f evaluation if a timeout is given. */
							// TODO: Schedule timeout according to estimates?
							if (config.timeoutForCandidateEvaluation() > 0) {
								taskId = ts.interruptMeAfterMS(config.timeoutForCandidateEvaluation());
							}

							/* If we have a global timeout, check whether considering this model is feasible. */
							if (TwoPhaseHASCO.this.getConfig().timeout() > 0) {
								int remainingTime = (int) (timestampOfDeadline - System.currentTimeMillis());
								if (estimatedOverallTrainingTimeForChosenModel + expectedTrainingTimeOfThisModel >= remainingTime) {
									TwoPhaseHASCO.this.logger.info(
											"Not evaluating solutiom {} anymore, because its insearch training time was {} expected time is {}, overall training time of currently selected solution is {}. This adds up to {}, which exceeds the remaining time of {}!",
											c, c.getTimeToComputeScore(), expectedTrainingTimeOfThisModel, estimatedOverallTrainingTimeForChosenModel, expectedTrainingTimeOfThisModel + estimatedOverallTrainingTimeForChosenModel,
											remainingTime);
									return;
								}
							}
							double selectionScore = evaluator.evaluate(c.getComponentInstance());
							synchronized (statsForThisCandidate) {
								statsForThisCandidate.addValue(selectionScore);
							}
						} catch (InterruptedException e) {
							System.out.println("Selection eval of " + ((currentlyChosenSolution == null) ? "unkown" : currentlyChosenSolution.toString()) + "got interrupted after "
									+ (System.currentTimeMillis() - timeStampStart) + "ms. Defined timeout was: " + config.timeoutForCandidateEvaluation() + "ms");
							// intentionally do not print anything as a timeout occurred.
						} catch (Throwable e) {
							/* Print only an exception if it is not expected. */
							if (!e.getMessage().contains("Killed WEKA!")) {
								TwoPhaseHASCO.this.logger.error("Observed an exeption when trying to evaluate a candidate in the selection phase.\n{}", LoggerUtil.getExceptionInfo(e));
							}
						} finally {
							sem.release();
							if (taskId >= 0) {
								ts.cancelTimeout(taskId);
							}
						}
					}
				});
		}
		try {
			/* now wait for results */
			this.logger.info("Waiting for termination of threads that compute the selection scores.");
			sem.acquire(ensembleToSelectFrom.size());
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
				selectedModel = ensembleToSelectFrom.get(selectedModelIndex);
				DescriptiveStatistics statsOfBest = stats.get(selectedModelIndex);
				this.logger.info("Selected a model. The model is: {}. Its internal error was {}. Validation error was {}", selectedModel, selectedModel.getScore(), statsOfBest.getMean());
			}

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return selectedModel;
	}

	private synchronized int getCandidateThatWouldCurrentlyBeSelectedWithinPhase2(final List<HASCOSolutionCandidate<Double>> ensembleToSelectFrom, final List<DescriptiveStatistics> stats, final boolean logComputations) {
		int selectedModel = 0;
		double best = Double.MAX_VALUE;
		for (int i = 0; i < ensembleToSelectFrom.size(); i++) {
			HASCOSolutionCandidate<Double> candidate = ensembleToSelectFrom.get(i);
			DescriptiveStatistics statsOfCandidate = stats.get(i);
			if (statsOfCandidate.getN() == 0) {
				if (logComputations) {
					this.logger.info("Ignoring candidate {} because no results were obtained in selection phase.", candidate);
				}
				continue;
			}
			double avgError = statsOfCandidate.getMean() / 100f;
			double quartileScore = statsOfCandidate.getPercentile(75) / 100;
			double score = (avgError + quartileScore) / 2f;
			if (logComputations) {
				this.logger.info("Score of candidate {} is {} based on {} (avg) and {} (.75-pct) with {} samples", candidate, score, avgError, quartileScore, statsOfCandidate.getN());
			}
			if (score < best) {
				best = score;
				selectedModel = i;
			}
		}
		return selectedModel;
	}

	@Override
	public void cancel() {
		this.timeoutControl.interrupt();
	}

	/**
	 * @return The solution candidate selected by TwoPhase HASCO
	 */
	public HASCOSolutionCandidate<Double> getSelectedSolutionCandidate() {
		return this.selectedHASCOSolution;
	}

	public TwoPhaseHASCOConfig getConfig() {
		return config;
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

	@Override
	public void setLoggerName(final String name) {
		this.logger.info("Switching logger from {} to {}", this.logger.getName(), name);
		this.loggerName = name;
		this.logger = LoggerFactory.getLogger(name);
		this.logger.info("Activated logger {} with name {}", name, this.logger.getName());
	}

	@Override
	public String getLoggerName() {
		return this.loggerName;
	}

	@Override
	public Iterator<AlgorithmEvent> iterator() {
		return this;
	}

	@Override
	public boolean hasNext() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public AlgorithmEvent next() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TwoPhaseSoftwareConfigurationProblem getInput() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void registerListener(Object listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setNumCPUs(int numberOfCPUs) {
		config.setProperty(IAlgorithmConfig.K_CPUS, String.valueOf(numberOfCPUs));
	}

	@Override
	public int getNumCPUs() {
		return config.cpus();
	}

	@Override
	public void setTimeout(int timeout, TimeUnit timeUnit) {
		if (timeUnit != TimeUnit.SECONDS)
			throw new IllegalArgumentException("only seconds supported");
		this.config.setProperty(IAlgorithmConfig.K_TIMEOUT, String.valueOf(timeout));
	}

	@Override
	public int getTimeout() {
		return config.timeout();
	}

	@Override
	public TimeUnit getTimeoutUnit() {
		return TimeUnit.SECONDS;
	}

	@Override
	public IOptimizerResult<ComponentInstance, Double> getOptimizationResult() {
		return new IOptimizerResult<ComponentInstance, Double>(selectedHASCOSolution.getComponentInstance(), selectedHASCOSolution.getScore());
	}
	
	@Subscribe
	public void receiveSolutionEvent(SolutionCandidateFoundEvent<HASCOSolutionCandidate<Double>> solutionEvent) {
		HASCOSolutionCandidate<Double> solution = solutionEvent.getSolutionCandidate();
		if (currentlyBestKnownSolution == null || solution.getScore().compareTo(currentlyBestKnownSolution.getScore()) < 0)
			currentlyBestKnownSolution = solution;
		logger.info("Received new solution with score {}", solution.getScore());
		phase1ResultQueue.add(solution);
	}

	public INodeEvaluator<TFDNode, Double> getPreferredNodeEvaluator() {
		return preferredNodeEvaluator;
	}

	public void setPreferredNodeEvaluator(INodeEvaluator<TFDNode, Double> preferredNodeEvaluator) {
		this.preferredNodeEvaluator = preferredNodeEvaluator;
	}
}
