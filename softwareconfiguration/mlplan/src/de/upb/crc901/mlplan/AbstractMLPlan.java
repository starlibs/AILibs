package de.upb.crc901.mlplan;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.aeonbits.owner.ConfigCache;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.crc901.automl.hascoml.supervised.HASCOSupervisedML;
import hasco.serialization.ComponentLoader;
import jaicore.basic.ILoggingCustomizable;
import jaicore.basic.IObjectEvaluator;
import jaicore.basic.sets.SetUtil;
import jaicore.concurrent.TimeoutTimer;
import jaicore.concurrent.TimeoutTimer.TimeoutSubmitter;
import jaicore.graph.IObservableGraphAlgorithm;
import jaicore.logging.LoggerUtil;
import jaicore.ml.WekaUtil;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import weka.classifiers.Classifier;

public abstract class AbstractMLPlan extends HASCOSupervisedML implements IObservableGraphAlgorithm<TFDNode, String>, ILoggingCustomizable {

	/** Logger for controlled outputs. */
	private Logger logger = LoggerFactory.getLogger(AbstractMLPlan.class);

	/** Name for configuring the output of this class' logger in a more convenient way. */
	private String loggerName;

	/** Properties object holding all the parameter configurations. */
	private static final AbstractMLPlanConfig CONFIG = ConfigCache.getOrCreate(AbstractMLPlanConfig.class);

	/** Caching factor to estimate runtimes for second phase. */
	private static final double CACHE_FACTOR = 0.8;

	/** Conservativeness factor to estimate runtimes for second phase. */
	private static final double CONSERVATIVENESS_FACTOR = 1.0;

	/** The classifier selected during selection phase. */
	private Classifier selectedClassifier;

	/** Classifier evaluator for the selection phase. */
	private IObjectEvaluator<Classifier, Double> selectionPhaseEvaluator = null;

	/* state variables during the run */

	/** Timestamp when the search started. */
	private long timeOfStart = -1;

	private Thread timeoutControl = null;

	protected AbstractMLPlan(final ComponentLoader componentLoader) throws IOException {
		super(componentLoader);
	}

	public void gatherSolutions() throws IOException {
		this.timeOfStart = System.currentTimeMillis();
		this.logger.info("Starting ML-Plan with timeout {}s, and a portion of {} for the second phase.", this.getConfig().timeout(), this.getConfig().selectionDataPortion());

		if (this.selectionPhaseEvaluator == null) {
			throw new IllegalArgumentException("The solution evaluator for the selection phase has not been set.");
		}

		/* phase 1: run HASCO to gather solutions */
		this.timeoutControl = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					while (!Thread.interrupted()) {
						Thread.sleep(100);
						int timeElapsed = (int) (System.currentTimeMillis() - AbstractMLPlan.this.timeOfStart);
						int timeRemaining = AbstractMLPlan.this.getConfig().timeout() * 1000 - timeElapsed;
						if (AbstractMLPlan.this.shouldSearchTerminate(timeRemaining)) {
							AbstractMLPlan.this.cancel();
							return;
						}
					}
				} catch (InterruptedException e) {
				}

			}
		}, "Phase 1 time bound observer");
		this.timeoutControl.start();
		this.logger.info("Now invoking HASCO to gather solutions for {}s", this.getConfig().timeout());

		super.gatherSolutions(this.getConfig().timeout() * 1000);

		this.logger.info("HASCO has finished. {} solutions were found.", super.getFoundClassifiers().size());
		if (super.getFoundClassifiers().isEmpty()) {
			System.err.println("No model built by HASCO");
			return;
		}

		System.out.println("Enter phase 2");
		/* phase 2: select model */
		this.selectedClassifier = this.selectModel(this.selectionPhaseEvaluator);
	}

	protected boolean shouldSearchTerminate(final long timeRemaining) {
		Collection<HASCOClassificationMLSolution> currentSelection = this.getSelectionForPhase2();

		int estimateForPhase2 = this.isSelectionActivated() ? this.getExpectedRuntimeForPhase2ForAGivenPool(currentSelection) : 0;

		HASCOClassificationMLSolution internallyOptimalSolution = super.getCurrentlyBestSolution();

		int timeToTrainBestSolutionOnEntireSet = internallyOptimalSolution != null ? (int) Math.round(internallyOptimalSolution.getTimeToComputeScore() / (1 - this.getPortionOfDataForPhase2())) : 0;

		boolean terminatePhase1 = estimateForPhase2 + timeToTrainBestSolutionOnEntireSet > timeRemaining;

		this.logger.debug("{}ms remaining in total, and we estimate {}ms for phase 2. Terminate phase 1: {}", timeRemaining, estimateForPhase2, terminatePhase1);

		return terminatePhase1;
	}

	private synchronized List<HASCOClassificationMLSolution> getSelectionForPhase2() {
		return this.getSelectionForPhase2(Integer.MAX_VALUE);
	}

	private static final double MAX_MARGIN_FROM_BEST = 0.03;

	private synchronized List<HASCOClassificationMLSolution> getSelectionForPhase2(final int remainingTime) {
		if (this.getNumberOfConsideredSolutions() < 1) {
			throw new UnsupportedOperationException("Cannot determine candidates for phase 2 if their number is set to a value less than 1. Here, it has been set to " + this.getNumberOfConsideredSolutions());
		}

		/* some initial checks for cases where we do not really have to do anything */
		if (remainingTime < 0) {
			throw new IllegalArgumentException("Cannot do anything in negative time (" + remainingTime + "ms)");
		}
		HASCOClassificationMLSolution internallyOptimalSolution = super.getCurrentlyBestSolution();
		if (internallyOptimalSolution == null) {
			return new ArrayList<>();
		}
		if (!this.isSelectionActivated()) {
			List<HASCOClassificationMLSolution> best = new ArrayList<>();
			best.add(internallyOptimalSolution);
			return best;
		}

		/*
		 * compute k pipeline candidates (the k/2 best, and k/2 random ones that do not deviate too much from the best one)
		 */
		double optimalInternalScore = internallyOptimalSolution.getScore();
		int bestK = (int) Math.ceil(this.getNumberOfConsideredSolutions() / 2);
		int randomK = this.getNumberOfConsideredSolutions() - bestK;
		Collection<HASCOClassificationMLSolution> potentialCandidates = new ArrayList<>(super.getFoundClassifiers()).stream().filter(solution -> {
			return solution.getScore() <= optimalInternalScore + MAX_MARGIN_FROM_BEST;
		}).collect(Collectors.toList());
		this.logger.debug("Computing {} best and {} random solutions for a max runtime of {}. Number of candidates that are at most {} worse than optimum {} is: {}/{}", bestK, randomK, remainingTime, MAX_MARGIN_FROM_BEST,
				optimalInternalScore, potentialCandidates.size(), super.getFoundClassifiers().size());
		List<HASCOClassificationMLSolution> selectionCandidates = potentialCandidates.stream().limit(bestK).collect(Collectors.toList());
		List<HASCOClassificationMLSolution> remainingCandidates = new ArrayList<>(SetUtil.difference(potentialCandidates, selectionCandidates));
		Collections.shuffle(remainingCandidates, new Random(this.getConfig().randomSeed()));
		selectionCandidates.addAll(remainingCandidates.stream().limit(randomK).collect(Collectors.toList()));

		/*
		 * if the candidates can be evaluated in the remaining time, return all of them
		 */
		int budget = this.getExpectedRuntimeForPhase2ForAGivenPool(selectionCandidates);
		if (budget < remainingTime) {
			return selectionCandidates;
		}

		/* otherwise return as much as can be expectedly done in the time */
		List<HASCOClassificationMLSolution> actuallySelectedSolutions = new ArrayList<>();
		int expectedRuntime;
		for (HASCOClassificationMLSolution pl : selectionCandidates) {
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

	private int getInSearchEvaluationTimeOfSolutionSet(final Collection<HASCOClassificationMLSolution> solutions) {
		return solutions.stream().map(x -> x.getTimeToComputeScore()).mapToInt(x -> x).sum();
	}

	public int getExpectedRuntimeForPhase2ForAGivenPool(final Collection<HASCOClassificationMLSolution> solutions) {
		int inSearchMCEvalTime = this.getInSearchEvaluationTimeOfSolutionSet(solutions);
		int inSearchSingleIterationEvalTime = (int) Math.round((double) inSearchMCEvalTime / this.getConfig().searchMCIterations());
		int estimateSelectionSingleIterationEvalTime = (int) (inSearchSingleIterationEvalTime * (1 - this.getConfig().selectionDataPortion()));

		// train time was on only 70% of the data
		// double cacheFactor = Math.pow(getNumberOfCPUs(), -.6);
		int runtime = (int) (CONSERVATIVENESS_FACTOR * estimateSelectionSingleIterationEvalTime * this.getConfig().selectionMCIterations() * CACHE_FACTOR / this.getConfig().cpus());
		this.logger.debug("Expected runtime is {} = {} * {} * {} * {} / {} for a pool of size {}", runtime, CONSERVATIVENESS_FACTOR, estimateSelectionSingleIterationEvalTime, this.getConfig().selectionMCIterations(), CACHE_FACTOR,
				this.getConfig().cpus(), solutions.size());
		return runtime;
	}

	protected Classifier selectModel(final IObjectEvaluator<Classifier, Double> evaluator) {
		Queue<HASCOClassificationMLSolution> solutions = super.getFoundClassifiers();
		HASCOClassificationMLSolution bestSolution = solutions.peek();
		double scoreOfBestSolution = bestSolution.getScore();

		/*
		 * Check whether selection phase is activated otherwise simply return solution with best F-Value
		 */
		if (!this.isSelectionActivated()) {
			this.logger.info("Selection disabled, just returning first element.");
			return bestSolution.getSolution();
		}

		/* determine the models from which we want to select */
		this.logger.info("Starting with phase 2: Selection of final model among the {} solutions that were identified.", solutions.size());
		long startOfPhase2 = System.currentTimeMillis();
		List<HASCOClassificationMLSolution> ensembleToSelectFrom;
		if (this.getConfig().timeout() > 0) {
			int remainingTime = (int) (this.getConfig().timeout() * 1000 - (System.currentTimeMillis() - this.timeOfStart));
			/*
			 * check remaining time, otherwise just return the solution with best F-Value.
			 */
			if (remainingTime < 0) {
				this.logger.info("Timelimit is already exhausted, just returning a greedy solution that had internal error {}.", scoreOfBestSolution);
				return bestSolution.getSolution();
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
		HASCOClassificationMLSolution selectedModel = bestSolution; // backup solution
		final Semaphore sem = new Semaphore(0);
		long timestampOfDeadline = this.timeOfStart + this.getTimeout() * 1000;

		/* evaluate each candiate */
		List<DescriptiveStatistics> stats = new ArrayList<>();
		final TimeoutSubmitter ts = TimeoutTimer.getInstance().getSubmitter();
		ensembleToSelectFrom.forEach(c -> stats.add(new DescriptiveStatistics()));
		System.out.println(ensembleToSelectFrom);

		for (int i = 0; i < ensembleToSelectFrom.size(); i++) {
			HASCOClassificationMLSolution c = ensembleToSelectFrom.get(i);
			final DescriptiveStatistics statsForThisCandidate = stats.get(i);

			for (int j = 0; j < this.getNumberOfMCIterationsPerSolutionInSelectionPhase(); j++) {
				pool.submit(new Runnable() {
					@Override
					public void run() {
						long timeStampStart = System.currentTimeMillis();

						int taskId = -1;
						HASCOClassificationMLSolution currentlyChosenSolution = null;
						try {
							/* Get the HASCOClassificationMLSolution instance for the considered model. */
							int indexOfCurrentlyChosenModel = AbstractMLPlan.this.getClassifierThatWouldCurrentlyBeSelectedWithinPhase2(ensembleToSelectFrom, stats, false);
							currentlyChosenSolution = ensembleToSelectFrom.get(indexOfCurrentlyChosenModel);

							/* Time needed to compute the entire score. */
							int inSearchMCEvaluationTime = currentlyChosenSolution.getTimeToComputeScore();
							int inSearchSingleIterationEvaluationTime = (int) Math.round((double) currentlyChosenSolution.getTimeToComputeScore() / AbstractMLPlan.this.getConfig().searchMCIterations());

							/* We assume linear growth of the classifier's evaluation time here to estimate
							 * (A) time for selection data,
							 * (B) time for building on the entire data provided for building. */
							int estimatedInSelectionSingleIterationEvaluationTime = (int) Math.round(inSearchSingleIterationEvaluationTime / AbstractMLPlan.this.getConfig().searchDataPortion());
							int estimatedInSelectionMCEvaluationTime = estimatedInSelectionSingleIterationEvaluationTime * AbstractMLPlan.this.getConfig().selectionMCIterations();
							int estimatedFinalBuildTime = (int) Math.round(estimatedInSelectionSingleIterationEvaluationTime / (1 - AbstractMLPlan.this.getConfig().selectionDataPortion()));
							int sumOfSelectionAndFinalBuild = estimatedFinalBuildTime + estimatedInSelectionMCEvaluationTime;

							AbstractMLPlan.this.logger.debug(
									"During search, the currently chosen model {} had a total evaluation time of {}ms ({}ms per iteration). "
											+ "We estimate an iteration in the selection phase to take {}ms ({} ms for all iterations), and the final build to take {}. " + "This yields a total time of {}ms.",
									currentlyChosenSolution.getSolution(), inSearchMCEvaluationTime, inSearchSingleIterationEvaluationTime, estimatedInSelectionSingleIterationEvaluationTime, estimatedInSelectionMCEvaluationTime,
									estimatedFinalBuildTime, sumOfSelectionAndFinalBuild);

							/* Old computation as coded by fmohr: we assume a linear growth */
							int trainingTimeForChosenModelInsideSearch = currentlyChosenSolution.getTimeToComputeScore();
							int estimatedOverallTrainingTimeForChosenModel = (int) Math.round(trainingTimeForChosenModelInsideSearch / (1 - AbstractMLPlan.this.getPortionOfDataForPhase2()) / .7);
							int expectedTrainingTimeOfThisModel = (int) Math.round(c.getTimeToComputeScore() / .7);

							trainingTimeForChosenModelInsideSearch = inSearchMCEvaluationTime;
							estimatedOverallTrainingTimeForChosenModel = estimatedFinalBuildTime;
							expectedTrainingTimeOfThisModel = estimatedInSelectionSingleIterationEvaluationTime;

							/* Schedule a timeout for single f evaluation if a timeout is given. */
							// TODO: Schedule timeout according to estimates?
							if (AbstractMLPlan.this.getTimeoutForSingleFEvaluation() > 0) {
								taskId = ts.interruptMeAfterMS(AbstractMLPlan.this.getTimeoutForSingleFEvaluation());
							}

							/* If we have a global timeout, check whether considering this model is feasible. */
							if (AbstractMLPlan.this.getConfig().timeout() > 0) {
								int remainingTime = (int) (timestampOfDeadline - System.currentTimeMillis());
								if (estimatedOverallTrainingTimeForChosenModel + expectedTrainingTimeOfThisModel >= remainingTime) {
									AbstractMLPlan.this.logger.info(
											"Not evaluating solutiom {} anymore, because its insearch training time was {} expected time is {}, overall training time of currently selected solution is {}. This adds up to {}, which exceeds the remaining time of {}!",
											c, c.getTimeToComputeScore(), expectedTrainingTimeOfThisModel, estimatedOverallTrainingTimeForChosenModel, expectedTrainingTimeOfThisModel + estimatedOverallTrainingTimeForChosenModel,
											remainingTime);
									return;
								}
							}
							Classifier clone = WekaUtil.cloneClassifier(c.getSolution());
							double selectionScore = AbstractMLPlan.this.selectionPhaseEvaluator.evaluate(clone);
							synchronized (statsForThisCandidate) {
								statsForThisCandidate.addValue(selectionScore);
							}
						} catch (InterruptedException e) {
							System.out.println("Selection eval of " + ((currentlyChosenSolution == null) ? "unkown" : currentlyChosenSolution.getSolution().toString()) + "got interrupted after "
									+ (System.currentTimeMillis() - timeStampStart) + "ms. Defined timeout was: " + AbstractMLPlan.this.getTimeoutForSingleFEvaluation() + "ms");
							// intentionally do not print anything as a timeout occurred.
						} catch (Throwable e) {
							/* Print only an exception if it is not expected. */
							if (!e.getMessage().contains("Killed WEKA!")) {
								AbstractMLPlan.this.logger.error("Observed an exeption when trying to evaluate a candidate in the selection phase.\n{}", LoggerUtil.getExceptionInfo(e));
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
		}
		try {
			/* now wait for results */
			this.logger.info("Waiting for termination of threads that compute the selection scores.");
			sem.acquire(ensembleToSelectFrom.size() * this.getNumberOfMCIterationsPerSolutionInSelectionPhase());
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
				int selectedModelIndex = this.getClassifierThatWouldCurrentlyBeSelectedWithinPhase2(ensembleToSelectFrom, stats, true);
				selectedModel = ensembleToSelectFrom.get(selectedModelIndex);
				DescriptiveStatistics statsOfBest = stats.get(selectedModelIndex);
				this.logger.info("Selected a model. The model is: {}. Its internal error was {}. Validation error was {}", selectedModel, selectedModel.getScore(), statsOfBest.getMean());
			}

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return selectedModel.getSolution();
	}

	private synchronized int getClassifierThatWouldCurrentlyBeSelectedWithinPhase2(final List<HASCOClassificationMLSolution> ensembleToSelectFrom, final List<DescriptiveStatistics> stats, final boolean logComputations) {
		int selectedModel = 0;
		double best = Double.MAX_VALUE;
		for (int i = 0; i < ensembleToSelectFrom.size(); i++) {
			HASCOClassificationMLSolution candidate = ensembleToSelectFrom.get(i);
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
		super.cancel();
	}

	/* Getter and setter for parameters. */

	/**
	 * @return Returns whether the selection phase is activated.
	 */
	public boolean isSelectionActivated() {
		return this.getConfig().selectionDataPortion() > 0;
	}

	/**
	 * @return The classifier selected by ML-Plan
	 */
	public Classifier getSelectedClassifier() {
		return this.selectedClassifier;
	}

	@Override
	public AbstractMLPlanConfig getConfig() {
		return CONFIG;
	}

	/**
	 * @return The classifier evaluator that is used in the selection phase.
	 */
	public IObjectEvaluator<Classifier, Double> getSelectionPhaseEvaluator() {
		return this.selectionPhaseEvaluator;
	}

	/**
	 * @param selectionBenchmark
	 *            The classifier evaluator that is used in the selection phase.
	 */
	public void setSelectionPhaseEvaluator(final IObjectEvaluator<Classifier, Double> selectionBenchmark) {
		this.selectionPhaseEvaluator = selectionBenchmark;
	}

	/**
	 * @return The portion of data that is reserved for the selection phase.
	 */
	public double getPortionOfDataForPhase2() {
		return this.getConfig().selectionDataPortion();
	}

	/**
	 * @param portionOfDataForPhase2
	 *            THe portion of data that is reserved for the selection phase.
	 */
	public void setPortionOfDataForPhase2(final double portionOfDataForPhase2) {
		this.getConfig().setProperty(AbstractMLPlanConfig.K_SELECTION_DATA_PORTION, portionOfDataForPhase2 + "");
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
		this.getConfig().setProperty(AbstractMLPlanConfig.K_SELECTION_NUM_CONSIDERED_SOLUTIONS, numberOfConsideredSolutions + "");
	}

	/**
	 * @return The number of MC iterations per solution evaluation in the selection phase.
	 */
	public int getNumberOfMCIterationsPerSolutionInSelectionPhase() {
		return this.getConfig().selectionMCIterations();
	}

	/**
	 * @param numberOfMCIterationsPerSolutionInSelectionPhase
	 *            The number of MC iterations per solution evaluation in the selection phase.
	 */
	public void setNumberOfMCIterationsPerSolutionInSelectionPhase(final int numberOfMCIterationsPerSolutionInSelectionPhase) {
		this.getConfig().setProperty(AbstractMLPlanConfig.K_SELECTION_MC_ITERATIONS, numberOfMCIterationsPerSolutionInSelectionPhase + "");
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
}
