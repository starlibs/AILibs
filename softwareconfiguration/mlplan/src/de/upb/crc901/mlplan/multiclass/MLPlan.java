package de.upb.crc901.mlplan.multiclass;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.crc901.automl.hascowekaml.HASCOForWekaML;
import de.upb.crc901.automl.hascowekaml.HASCOForWekaML.HASCOForWekaMLSolution;
import hasco.model.Component;
import jaicore.basic.ILoggingCustomizable;
import jaicore.basic.sets.SetUtil;
import jaicore.concurrent.TimeoutTimer;
import jaicore.concurrent.TimeoutTimer.TimeoutSubmitter;
import jaicore.graph.IObservableGraphAlgorithm;
import jaicore.graphvisualizer.SimpleGraphVisualizationWindow;
import jaicore.logging.LoggerUtil;
import jaicore.ml.WekaUtil;
import jaicore.ml.evaluation.ClassifierEvaluator;
import jaicore.ml.evaluation.MonteCarloCrossValidationEvaluator;
import jaicore.ml.evaluation.MulticlassEvaluator;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.planning.graphgenerators.task.tfd.TFDTooltipGenerator;
import jaicore.search.algorithms.interfaces.ISolutionEvaluator;
import jaicore.search.algorithms.standard.core.INodeEvaluator;
import jaicore.search.algorithms.standard.uncertainty.OversearchAvoidanceConfig;
import jaicore.search.structure.core.GraphGenerator;
import jaicore.search.structure.core.Node;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;

@SuppressWarnings("serial")
public class MLPlan extends AbstractClassifier implements Classifier, OptionHandler, IObservableGraphAlgorithm<TFDNode, String>, ILoggingCustomizable {

	/* logging */
	private Logger logger = LoggerFactory.getLogger(MLPlan.class);
	private String loggerName;

	/* configuration variables */
	private int timeoutInS = -1;
	private int timeoutPerNodeFComputation;
	private int randomSeed;
	private int numberOfCPUs = 4;
	private int memory = 256;
	private float portionOfDataForPhase2 = 0;
	private int numberOfConsideredSolutions = 100;
	private int numberOfMCIterationsPerSolutionInSelectionPhase = 3;

	/* variable relevant for and during a single run */
	private long timeOfStart = -1;
	private final HASCOForWekaML hasco;

	/* output variables */
	private Classifier selectedClassifier;

	public MLPlan(File configurationFile) {
		super();
		if (configurationFile == null || !configurationFile.exists())
			throw new IllegalArgumentException("The file " + configurationFile + " is null or does not exist and cannot be used by ML-Plan");
		hasco = new HASCOForWekaML(configurationFile);
	}

	@Override
	public void buildClassifier(final Instances data) throws Exception {

		this.timeOfStart = System.currentTimeMillis();
		logger.info("Starting ML-Plan with timeout {}s, and a portion of {} for the second phase.", this.timeoutInS, this.portionOfDataForPhase2);

		/* split data set */
		Instances dataForSearch;
		Instances dataPreservedForSelection;
		if (this.portionOfDataForPhase2 > 0) {
			List<Instances> split = WekaUtil.realizeSplit(data, WekaUtil.getArbitrarySplit(data, new Random(this.randomSeed), this.portionOfDataForPhase2));
			dataForSearch = split.get(1);
			dataPreservedForSelection = split.get(0);
			if (dataForSearch.isEmpty()) {
				throw new IllegalStateException("Cannot search on no data and select on " + dataPreservedForSelection.size() + " data points.");
			}
		} else {
			dataForSearch = data;
			dataPreservedForSelection = null;
		}

		logger.info("Creating search with a data split {}/{} for search/selection, which yields effectively a split of size:  {}/{}", 1 - this.portionOfDataForPhase2,
				this.portionOfDataForPhase2, dataForSearch.size(), dataPreservedForSelection != null ? dataPreservedForSelection.size() : 0);

		/*
		 * we allow CPUs-1 threads for node evaluation. Setting the timeout evaluator to null means to really prune all those
		 */
		if (this.numberOfCPUs < 1) {
			throw new IllegalStateException("Cannot generate search where number of CPUs is " + this.numberOfCPUs);
		}

		/* phase 1: run HASCO to gather solutions */
		// search.setTimeoutForComputationOfF(timeoutPerNodeFComputation, n -> null);
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					while (!Thread.interrupted()) {
						Thread.sleep(100);
						int timeElapsed = (int) (System.currentTimeMillis() - MLPlan.this.timeOfStart);
						int timeRemaining = MLPlan.this.timeoutInS * 1000 - timeElapsed;
						if (MLPlan.this.shouldSearchTerminate(timeRemaining)) {
							MLPlan.this.hasco.cancel();
							return;
						}
					}
				} catch (InterruptedException e) {
				}

			}
		}, "Phase 1 time bound observer").start();
		logger.info("Now invoking HASCO to gather solutions for {}s", this.timeoutInS);
		this.hasco.gatherSolutions(dataForSearch, this.timeoutInS * 1000);
		logger.info("HASCO has finished. {} solutions were found.", hasco.getFoundClassifiers().size());
		if (this.hasco.getFoundClassifiers().isEmpty()) {
			System.err.println("No model built by HASCO");
			return;
		}

		/* phase 2: select model */
		MonteCarloCrossValidationEvaluator solutionEvaluatorInPhase2 = new MonteCarloCrossValidationEvaluator(new MulticlassEvaluator(new Random(3)), 10, data, .7f);
		this.selectedClassifier = this.selectModel(solutionEvaluatorInPhase2);
		this.timeoutPerNodeFComputation = 1000 * (this.timeoutInS == 60 ? 15 : 300);

		/* train selected model on all data */
		this.selectedClassifier.buildClassifier(data);
	}

	@Override
	public double classifyInstance(final Instance instance) throws Exception {
		return this.selectedClassifier.classifyInstance(instance);
	}

	@Override
	public double[] distributionForInstance(final Instance instance) throws Exception {
		return this.selectedClassifier.distributionForInstance(instance);
	}

	@Override
	public Capabilities getCapabilities() {
		Capabilities result = super.getCapabilities();
		result.disableAll();

		// attributes
		result.enable(Capability.NOMINAL_ATTRIBUTES);
		result.enable(Capability.NUMERIC_ATTRIBUTES);
		result.enable(Capability.DATE_ATTRIBUTES);
		result.enable(Capability.STRING_ATTRIBUTES);
		result.enable(Capability.RELATIONAL_ATTRIBUTES);
		result.enable(Capability.MISSING_VALUES);

		// class
		result.enable(Capability.NOMINAL_CLASS);
		result.enable(Capability.NUMERIC_CLASS);
		result.enable(Capability.DATE_CLASS);
		result.enable(Capability.MISSING_CLASS_VALUES);

		// instances
		result.setMinimumNumberInstances(1);
		return result;
	}

	@Override
	public Enumeration<Option> listOptions() {
		return null;
	}

	@Override
	public void setOptions(final String[] options) throws Exception {
		for (int i = 0; i < options.length; i++) {
			switch (options[i].toLowerCase()) {
			case "-t": {
				this.setTimeout(Integer.parseInt(options[++i]));
				break;
			}
			case "-r": {
				this.setRandom(Integer.parseInt(options[++i]));
				break;
			}
			default: {
				throw new IllegalArgumentException("Unknown option " + options[i] + ".");
			}
			}
		}
	}

	@Override
	public String[] getOptions() {
		return null;
	}

	public void setTimeout(final int timeoutInS) {
		this.timeoutInS = timeoutInS;
	}
	
	public void setOversearchAvoidanceConfig (OversearchAvoidanceConfig config) {
		hasco.setOversearchAvoidanceMode(config);
	}

	public void setRandom(final int randomSeed) {
		this.randomSeed = randomSeed;
	}

	public boolean isSelectionActivated() {
		return this.portionOfDataForPhase2 > 0;
	}

	protected boolean shouldSearchTerminate(final long timeRemaining) {
		Collection<HASCOForWekaMLSolution> currentSelection = this.getSelectionForPhase2();
		int estimateForPhase2 = this.isSelectionActivated() ? this.getExpectedRuntimeForPhase2ForAGivenPool(currentSelection) : 0;
		HASCOForWekaMLSolution internallyOptimalSolution = this.hasco.getCurrentlyBestSolution();
		int timeToTrainBestSolutionOnEntireSet = internallyOptimalSolution != null
				? (int) Math.round((double) internallyOptimalSolution.getTimeToComputeScore() / (1 - this.portionOfDataForPhase2))
				: 0;
		boolean terminatePhase1 = estimateForPhase2 + timeToTrainBestSolutionOnEntireSet > timeRemaining;
		logger.info("{}ms remaining in total, and we estimate {}ms for phase 2. Terminate phase 1: {}", timeRemaining, estimateForPhase2, terminatePhase1);
		return terminatePhase1;
	}

	private synchronized List<HASCOForWekaMLSolution> getSelectionForPhase2() {
		return this.getSelectionForPhase2(Integer.MAX_VALUE);
	}

	private synchronized List<HASCOForWekaMLSolution> getSelectionForPhase2(final int remainingTime) {

		if (this.numberOfConsideredSolutions < 1) {
			throw new UnsupportedOperationException(
					"Cannot determine candidates for phase 2 if their number is set to a value less than 1. Here, it has been set to " + this.numberOfConsideredSolutions);
		}

		/* some initial checks for cases where we do not really have to do anything */
		if (remainingTime < 0) {
			throw new IllegalArgumentException("Cannot do anything in negative time (" + remainingTime + "ms)");
		}
		HASCOForWekaMLSolution internallyOptimalSolution = this.hasco.getCurrentlyBestSolution();
		if (internallyOptimalSolution == null) {
			return new ArrayList<>();
		}
		if (!this.isSelectionActivated()) {
			List<HASCOForWekaMLSolution> best = new ArrayList<>();
			best.add(internallyOptimalSolution);
			return best;
		}

		/*
		 * compute k pipeline candidates (the k/2 best, and k/2 random ones that do not deviate too much from the best one)
		 */
		double optimalInternalScore = internallyOptimalSolution.getScore();
		int maxMarginFrombest = 300;
		int bestK = (int) Math.ceil(this.numberOfConsideredSolutions / 2);
		int randomK = this.numberOfConsideredSolutions - bestK;
		Collection<HASCOForWekaMLSolution> potentialCandidates = new ArrayList<>(this.hasco.getFoundClassifiers()).stream().filter(solution -> {
			return solution.getScore() <= optimalInternalScore + maxMarginFrombest;
		}).collect(Collectors.toList());
		logger.debug("Computing {} best and {} random solutions for a max runtime of {}. Number of candidates that are at most {} worse than optimum {} is: {}/{}", bestK, randomK,
				remainingTime, maxMarginFrombest, optimalInternalScore, potentialCandidates.size(), this.hasco.getFoundClassifiers().size());
		List<HASCOForWekaMLSolution> selectionCandidates = potentialCandidates.stream().limit(bestK).collect(Collectors.toList());
		List<HASCOForWekaMLSolution> remainingCandidates = new ArrayList<>(SetUtil.difference(potentialCandidates, selectionCandidates));
		Collections.shuffle(remainingCandidates, new Random(this.randomSeed));
		selectionCandidates.addAll(remainingCandidates.stream().limit(randomK).collect(Collectors.toList()));

		/* if the candidates can be evaluated in the remaining time, return all of them */
		int budget = this.getExpectedRuntimeForPhase2ForAGivenPool(selectionCandidates);
		if (budget < remainingTime) {
			return selectionCandidates;
		}

		/* otherwise return as much as can be expectedly done in the time */
		List<HASCOForWekaMLSolution> actuallySelectedSolutions = new ArrayList<>();
		int expectedRuntime;
		for (HASCOForWekaMLSolution pl : selectionCandidates) {
			actuallySelectedSolutions.add(pl);
			expectedRuntime = this.getExpectedRuntimeForPhase2ForAGivenPool(actuallySelectedSolutions);
			if (expectedRuntime > remainingTime && actuallySelectedSolutions.size() > 1) {
				logger.debug("Not considering solution {} for phase 2, because the expected runtime of the whole thing would be {}/{}", pl, expectedRuntime, remainingTime);
				actuallySelectedSolutions.remove(pl);
			}
		}
		assert this.getExpectedRuntimeForPhase2ForAGivenPool(
				actuallySelectedSolutions) > remainingTime : "Invalid result. Expected runtime is higher than it should be based on the computation.";
		return actuallySelectedSolutions;
	}

	private int getInSearchEvaluationTimeOfSolutionSet(final Collection<HASCOForWekaMLSolution> solutions) {
		return this.hasco.getFoundClassifiers().stream().map(x -> x.getTimeToComputeScore()).reduce(0, (a, b) -> a + b).intValue();
	}

	public int getExpectedRuntimeForPhase2ForAGivenPool(final Collection<HASCOForWekaMLSolution> solutions) {
		long estimateForPhase2IfSequential = (int) (this.getInSearchEvaluationTimeOfSolutionSet(solutions) / (1 - this.portionOfDataForPhase2)); // consider the fact the inner search
																																					// train time was on only 70% of the
																																					// data
		long estimateForAvailableCores = this.numberOfCPUs;
		// double cacheFactor = Math.pow(getNumberOfCPUs(), -.6);
		double cacheFactor = .8;
		double conservativenessFactor = 1;
		int runtime = (int) (conservativenessFactor * estimateForPhase2IfSequential * this.numberOfMCIterationsPerSolutionInSelectionPhase * cacheFactor
				/ estimateForAvailableCores);
		logger.debug("Expected runtime is {} = {} * {} * {} * {} / {}", runtime, conservativenessFactor, estimateForPhase2IfSequential,
				this.numberOfMCIterationsPerSolutionInSelectionPhase, cacheFactor, estimateForAvailableCores);
		return runtime;
	}

	protected Classifier selectModel(final ClassifierEvaluator evaluator) {

		Queue<HASCOForWekaMLSolution> solutions = this.hasco.getFoundClassifiers();
		HASCOForWekaMLSolution bestSolution = solutions.peek();
		double scoreOfBestSolution = bestSolution.getScore();

		/* Check whether selection phase is activated otherwise simply return solution with best F-Value */
		if (!this.isSelectionActivated()) {
			logger.info("Selection disabled, just returning first element.");
			return bestSolution.getSolution();
		}

		/* determine the models from which we want to select */
		logger.info("Starting with phase 2: Selection of final model among the {} solutions that were identified.", solutions.size());
		long startOfPhase2 = System.currentTimeMillis();
		List<HASCOForWekaMLSolution> ensembleToSelectFrom;
		if (this.timeoutInS > 0) {
			int remainingTime = (int) (this.timeoutInS * 1000 - (System.currentTimeMillis() - this.timeOfStart));
			/*
			 * check remaining time, otherwise just return the solution with best F-Value.
			 */
			if (remainingTime < 0) {
				logger.info("Timelimit is already exhausted, just returning a greedy solution that had internal error {}.", scoreOfBestSolution);
				return bestSolution.getSolution();
			}

			/* Get a queue of solutions to perform selection evaluation for. */
			ensembleToSelectFrom = this.getSelectionForPhase2(remainingTime); // should be ordered by f-value already (at least the first k)
			int expectedTimeForSolution;
			expectedTimeForSolution = this.getExpectedRuntimeForPhase2ForAGivenPool(ensembleToSelectFrom);
			remainingTime = (int) (this.timeoutInS * 1000 - (System.currentTimeMillis() - this.timeOfStart));

			if (expectedTimeForSolution > remainingTime) {
				logger.warn("Only {}ms remaining. We probably cannot make it in time.", remainingTime);
			}
			logger.info("We expect phase 2 to consume {}ms for {} candidates. {}ms are permitted by timeout. The following pipelines are considered: ", expectedTimeForSolution,
					ensembleToSelectFrom.size(), remainingTime);
			// ensembleToSelectFrom.stream().forEach(pl -> logger.info("\t{}: {} (f) / {} (t)", pl, hasco,
			// getAnnotation(pl).getFTime()));
		} else {
			ensembleToSelectFrom = this.getSelectionForPhase2();
		}

		AtomicInteger evaluatorCounter = new AtomicInteger(0);
		ExecutorService pool = Executors.newFixedThreadPool(this.numberOfCPUs, r -> {
			Thread t = new Thread(r);
			t.setName("final-evaluator-" + evaluatorCounter.incrementAndGet());
			return t;
		});
		HASCOForWekaMLSolution selectedModel = bestSolution; // backup solution
		final Semaphore sem = new Semaphore(0);
		long timestampOfDeadline = this.timeOfStart + this.timeoutInS * 1000;

		/* evaluate each candiate */
		List<DescriptiveStatistics> stats = new ArrayList<>();
		final TimeoutSubmitter ts = TimeoutTimer.getInstance().getSubmitter();
		ensembleToSelectFrom.forEach(c -> stats.add(new DescriptiveStatistics()));
		for (int i = 0; i < ensembleToSelectFrom.size(); i++) {
			HASCOForWekaMLSolution c = ensembleToSelectFrom.get(i);
			final DescriptiveStatistics statsForThisCandidate = stats.get(i);
			for (int j = 0; j < this.numberOfMCIterationsPerSolutionInSelectionPhase; j++) {
				pool.submit(new Runnable() {
					@Override
					public void run() {
						int taskId = -1;
						try {
							int indexOfCurrentlyChosenModel = MLPlan.this.getClassifierThatWouldCurrentlyBeSelectedWithinPhase2(ensembleToSelectFrom, stats, false);
							HASCOForWekaMLSolution currentlyChosenSolution = ensembleToSelectFrom.get(indexOfCurrentlyChosenModel);
							int trainingTimeForChosenModelInsideSearch = currentlyChosenSolution.getTimeToComputeScore();
							int estimatedOverallTrainingTimeForChosenModel = (int) Math
									.round(trainingTimeForChosenModelInsideSearch / (1 - MLPlan.this.portionOfDataForPhase2) / .7); // we
																																	// assume a
																																	// linear
																																	// growth
							int expectedTrainingTimeOfThisModel = (int) Math.round(c.getTimeToComputeScore() / .7);
							if (MLPlan.this.timeoutPerNodeFComputation > 0) {
								taskId = ts.interruptMeAfterMS(MLPlan.this.timeoutPerNodeFComputation);
							}
							if (MLPlan.this.timeoutInS > 0) {
								int remainingTime = (int) (timestampOfDeadline - System.currentTimeMillis());
								if (estimatedOverallTrainingTimeForChosenModel + expectedTrainingTimeOfThisModel >= remainingTime) {
									logger.info(
											"Not evaluating solutiom {} anymore, because expected time is {}, overall training time of currently selected solution is {}. This adds up to {}, which exceeds the remaining time of {}!",
											c, expectedTrainingTimeOfThisModel, estimatedOverallTrainingTimeForChosenModel,
											expectedTrainingTimeOfThisModel + estimatedOverallTrainingTimeForChosenModel, remainingTime);
									return;
								}
							}
							Classifier clone = WekaUtil.cloneClassifier(c.getSolution());
							double selectionScore = evaluator.evaluate(clone);
							synchronized (statsForThisCandidate) {
								statsForThisCandidate.addValue(selectionScore);
							}
						} catch (Throwable e) {
							logger.error("Observed an exeption when trying to evaluate a candidate in the selection phase.\n{}", LoggerUtil.getExceptionInfo(e));
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
			logger.info("Waiting for termination of threads that compute the selection scores.");
			sem.acquire(ensembleToSelectFrom.size() * this.numberOfMCIterationsPerSolutionInSelectionPhase);
			long endOfPhase2 = System.currentTimeMillis();
			logger.info("Finished phase 2 within {}ms net. Total runtime was {}ms. ", endOfPhase2 - startOfPhase2, endOfPhase2 - this.timeOfStart);
			logger.debug("Shutting down thread pool");
			pool.shutdownNow();
			pool.awaitTermination(5, TimeUnit.SECONDS);
			if (!pool.isShutdown()) {
				logger.warn("Thread pool is not shut down yet!");
			}

			/* set chosen model */
			if (ensembleToSelectFrom.isEmpty()) {
				logger.warn("No solution contained in ensemble.");
			} else {
				int selectedModelIndex = this.getClassifierThatWouldCurrentlyBeSelectedWithinPhase2(ensembleToSelectFrom, stats, true);
				selectedModel = ensembleToSelectFrom.get(selectedModelIndex);
				DescriptiveStatistics statsOfBest = stats.get(selectedModelIndex);
				logger.info("Selected a model. The model is: {}. Its internal error was {}. Validation error was {}", selectedModel, selectedModel.getScore(),
						statsOfBest.getMean());
			}

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// else
		// logger.warn("Thread pool still active!");
		return selectedModel.getSolution();
	}

	private synchronized int getClassifierThatWouldCurrentlyBeSelectedWithinPhase2(final List<HASCOForWekaMLSolution> ensembleToSelectFrom, final List<DescriptiveStatistics> stats,
			final boolean logComputations) {
		int selectedModel = 0;
		double best = Double.MAX_VALUE;
		for (int i = 0; i < ensembleToSelectFrom.size(); i++) {
			HASCOForWekaMLSolution candidate = ensembleToSelectFrom.get(i);
			DescriptiveStatistics statsOfCandidate = stats.get(i);
			if (statsOfCandidate.getN() == 0) {
				if (logComputations) {
					logger.info("Ignoring candidate {} because no results were obtained in selection phase.", candidate);
				}
				continue;
			}
			double avgError = statsOfCandidate.getMean() / 100f;
			double quartileScore = statsOfCandidate.getPercentile(75) / 100;
			double score = (avgError + quartileScore) / 2f;
			if (logComputations) {
				logger.info("Score of candidate {} is {} based on {} (avg) and {} (.75-pct) with {} samples", candidate, score, avgError, quartileScore, statsOfCandidate.getN());
			}
			if (score < best) {
				best = score;
				selectedModel = i;
			}
		}
		return selectedModel;
	}

	public int getTimeoutPerNodeFComputation() {
		return this.timeoutPerNodeFComputation;
	}

	public void setTimeoutPerNodeFComputation(final int timeoutPerNodeFComputation) {
		this.timeoutPerNodeFComputation = timeoutPerNodeFComputation;
	}

	public int getRandomSeed() {
		return this.randomSeed;
	}

	public void setRandomSeed(final int randomSeed) {
		this.randomSeed = randomSeed;
	}

	public int getNumberOfCPUs() {
		return this.numberOfCPUs;
	}

	public void setNumberOfCPUs(final int numberOfCPUs) {
		this.numberOfCPUs = numberOfCPUs;
	}

	public int getMemory() {
		return this.memory;
	}

	public void setMemory(final int memory) {
		this.memory = memory;
	}

	public float getPortionOfDataForPhase2() {
		return this.portionOfDataForPhase2;
	}

	public void setPortionOfDataForPhase2(final float portionOfDataForPhase2) {
		this.portionOfDataForPhase2 = portionOfDataForPhase2;
	}

	public int getNumberOfConsideredSolutions() {
		return this.numberOfConsideredSolutions;
	}

	public void setNumberOfConsideredSolutions(final int numberOfConsideredSolutions) {
		this.numberOfConsideredSolutions = numberOfConsideredSolutions;
	}

	public int getNumberOfMCIterationsPerSolutionInSelectionPhase() {
		return this.numberOfMCIterationsPerSolutionInSelectionPhase;
	}

	public void setNumberOfMCIterationsPerSolutionInSelectionPhase(final int numberOfMCIterationsPerSolutionInSelectionPhase) {
		this.numberOfMCIterationsPerSolutionInSelectionPhase = numberOfMCIterationsPerSolutionInSelectionPhase;
	}

	public int getTimeout() {
		return this.timeoutInS;
	}

	public Classifier getSelectedClassifier() {
		return this.selectedClassifier;
	}

	@Override
	public void registerListener(final Object listener) {
		this.hasco.registerListener(listener);
	}

	/**
	 * Set the preferred node evaluator. If this returns NULL, the random completion is conducted
	 */
	public void setNodeEvaluator(final INodeEvaluator<TFDNode, Double> nodeEvaluator) {
		this.hasco.setPreferredNodeEvaluator(nodeEvaluator);
	}

	@Override
	public void setLoggerName(String name) {
		logger.info("Switching logger from {} to {}", logger.getName(), name);
		this.loggerName = name;
		logger = LoggerFactory.getLogger(name);
		logger.info("Activated logger {} with name {}", name, logger.getName());
		this.hasco.setLoggerName(loggerName + ".hascoml");
	}

	@Override
	public String getLoggerName() {
		return loggerName;
	}
	
	public void enableVisualization() {
		if (this.timeOfStart >= 0)
			throw new IllegalStateException("Cannot enable visualization after buildClassifier has been invoked. Please enable it previously.");
		
		new SimpleGraphVisualizationWindow<Node<TFDNode, Double>>(this).getPanel()
		.setTooltipGenerator(new TFDTooltipGenerator<>());
	}
	
	public Collection<Component> getComponents() {
		return hasco.getComponents();
	}
	
	public ISolutionEvaluator<TFDNode,Double> getSolutionEvaluator() {
		return hasco.getSolutionEvaluator();
	}
}
