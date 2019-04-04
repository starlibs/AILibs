//package de.upb.crc901.mlplan.multilabel;
//
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.Collections;
//import java.util.Enumeration;
//import java.util.List;
//import java.util.Queue;
//import java.util.Random;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.Semaphore;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.atomic.AtomicInteger;
//import java.util.stream.Collectors;
//
//import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import de.upb.crc901.automl.hascowekaml.HASCOForMEKA;
//import de.upb.crc901.automl.hascowekaml.HASCOForMEKA.HASCOForMEKASolution;
//import jaicore.basic.sets.SetUtil;
//import jaicore.concurrent.TimeoutTimer;
//import jaicore.concurrent.TimeoutTimer.TimeoutSubmitter;
//import jaicore.graph.IGraphAlgorithm;
//import jaicore.logging.LoggerUtil;
//import jaicore.ml.WekaUtil;
//import jaicore.ml.evaluation.ClassifierEvaluator;
//import jaicore.ml.evaluation.MonteCarloCrossValidationEvaluator;
//import jaicore.ml.multilabel.evaluators.F1AverageMultilabelEvaluator;
//import jaicore.planning.graphgenerators.task.tfd.TFDNode;
//import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
//import meka.classifiers.multilabel.MultiLabelClassifier;
//import weka.classifiers.AbstractClassifier;
//import weka.classifiers.Classifier;
//import weka.core.Capabilities;
//import weka.core.Capabilities.Capability;
//import weka.core.Instance;
//import weka.core.Instances;
//import weka.core.Option;
//import weka.core.OptionHandler;
//
//@SuppressWarnings("serial")
//public class ML2Plan extends AbstractClassifier implements Classifier, OptionHandler, IGraphAlgorithm<TFDNode, String> {
//
//	private static final Logger logger = LoggerFactory.getLogger(ML2Plan.class);
//
//	/* configuration variables */
//	private int timeoutInS = -1;
//	private int timeoutPerNodeFComputation;
//	private int randomSeed;
//	private int numberOfCPUs = 4;
//	private int memory = 256;
//	private float portionOfDataForPhase2 = 0;
//	private int numberOfConsideredSolutions = 100;
//	private int numberOfMCIterationsPerSolutionInSelectionPhase = 3;
//
//	/* variable relevant for and during a single run */
//	private long timeOfStart;
//	private HASCOForMEKA hasco = new HASCOForMEKA();
//
//	/* output variables */
//	private MultiLabelClassifier selectedClassifier;
//
//	public ML2Plan() {
//		super();
//	}
//
//	@Override
//	public void buildClassifier(final Instances data) throws Exception {
//
//		timeOfStart = System.currentTimeMillis();
//		logger.info("Starting ML-Plan with timeout {}s, and a portion of {} for the second phase.", timeoutInS, portionOfDataForPhase2);
//
//		/* split data set */
//		Instances dataForSearch;
//		Instances dataPreservedForSelection;
//		if (portionOfDataForPhase2 > 0) {
//			List<Instances> split = WekaUtil.realizeSplit(data, WekaUtil.getArbitrarySplit(data, new Random(randomSeed), portionOfDataForPhase2));
//			dataForSearch = split.get(1);
//			dataPreservedForSelection = split.get(0);
//			if (dataForSearch.isEmpty()) {
//				throw new IllegalStateException("Cannot search on no data and select on " + dataPreservedForSelection.size() + " data points.");
//			}
//		} else {
//			dataForSearch = data;
//			dataPreservedForSelection = null;
//		}
//
//		logger.info("Creating search with a data split {}/{} for search/selection, which yields effectively a split of size:  {}/{}", 1 - portionOfDataForPhase2,
//				portionOfDataForPhase2, dataForSearch.size(), dataPreservedForSelection != null ? dataPreservedForSelection.size() : 0);
//
//		/* we allow CPUs-1 threads for node evaluation. Setting the timeout evaluator to null means to really prune all those */
//		if (numberOfCPUs < 1)
//			throw new IllegalStateException("Cannot generate search where number of CPUs is " + numberOfCPUs);
//
//		/* phase 1: run HASCO to gather solutions */
//		// search.setTimeoutForComputationOfF(timeoutPerNodeFComputation, n -> null);
//		new Thread(new Runnable() {
//
//			@Override
//			public void run() {
//				try {
//					while (!Thread.interrupted()) {
//						Thread.sleep(100);
//						int timeElapsed = (int)(System.currentTimeMillis() - timeOfStart);
//						int timeRemaining = timeoutInS * 1000 - timeElapsed;
//						if (shouldSearchTerminate(timeRemaining)) {
//							hasco.cancel();
//							return;
//						}
//					}
//				} catch (InterruptedException e) {
//				}
//				
//			}
//		}, "Phase 1 time bound observer").start();
//		hasco.gatherSolutions(dataForSearch, timeoutInS * 1000);
//		if (hasco.getFoundClassifiers().isEmpty()) {
//			System.err.println("No model built by HASCO");
//			return;
//		}
//
//		/* phase 2: select model */
//		MonteCarloCrossValidationEvaluator solutionEvaluatorInPhase2 = new MonteCarloCrossValidationEvaluator(new F1AverageMultilabelEvaluator(new Random(3)), 3, data, .7f);
//		selectedClassifier = selectModel(solutionEvaluatorInPhase2);
//		timeoutPerNodeFComputation = 1000 * (timeoutInS == 60 ? 15 : 300);
//
//		/* train selected model on all data */
//		selectedClassifier.buildClassifier(data);
//	}
//
//	@Override
//	public double classifyInstance(final Instance instance) throws Exception {
//		return selectedClassifier.classifyInstance(instance);
//	}
//
//	@Override
//	public double[] distributionForInstance(final Instance instance) throws Exception {
//		return selectedClassifier.distributionForInstance(instance);
//	}
//
//	@Override
//	public Capabilities getCapabilities() {
//		Capabilities result = super.getCapabilities();
//		result.disableAll();
//
//		// attributes
//		result.enable(Capability.NOMINAL_ATTRIBUTES);
//		result.enable(Capability.NUMERIC_ATTRIBUTES);
//		result.enable(Capability.DATE_ATTRIBUTES);
//		result.enable(Capability.STRING_ATTRIBUTES);
//		result.enable(Capability.RELATIONAL_ATTRIBUTES);
//		result.enable(Capability.MISSING_VALUES);
//
//		// class
//		result.enable(Capability.NOMINAL_CLASS);
//		result.enable(Capability.NUMERIC_CLASS);
//		result.enable(Capability.DATE_CLASS);
//		result.enable(Capability.MISSING_CLASS_VALUES);
//
//		// instances
//		result.setMinimumNumberInstances(1);
//		return result;
//	}
//
//	@Override
//	public Enumeration<Option> listOptions() {
//		return null;
//	}
//
//	@Override
//	public void setOptions(final String[] options) throws Exception {
//		for (int i = 0; i < options.length; i++) {
//			switch (options[i].toLowerCase()) {
//			case "-t": {
//				this.setTimeout(Integer.parseInt(options[++i]));
//				break;
//			}
//			case "-r": {
//				this.setRandom(Integer.parseInt(options[++i]));
//				break;
//			}
//			default: {
//				throw new IllegalArgumentException("Unknown option " + options[i] + ".");
//			}
//			}
//		}
//	}
//
//	@Override
//	public String[] getOptions() {
//		return null;
//	}
//
//	public void setTimeout(final int timeoutInS) {
//		this.timeoutInS = timeoutInS;
//	}
//
//	public void setRandom(final int randomSeed) {
//		this.randomSeed = randomSeed;
//	}
//
//	public boolean isSelectionActivated() {
//		return portionOfDataForPhase2 > 0;
//	}
//
//	protected boolean shouldSearchTerminate(long timeRemaining) {
//		Collection<HASCOForMEKASolution> currentSelection = getSelectionForPhase2();
//		int estimateForPhase2 = isSelectionActivated() ? getExpectedRuntimeForPhase2ForAGivenPool(currentSelection) : 0;
//		HASCOForMEKASolution internallyOptimalSolution = hasco.getCurrentlyBestSolution();
//		int timeToTrainBestSolutionOnEntireSet = internallyOptimalSolution != null ? (int)Math.round(internallyOptimalSolution.getTimeForScoreComputation() / (1 - portionOfDataForPhase2)) : 0;
//		boolean terminatePhase1 = estimateForPhase2 + timeToTrainBestSolutionOnEntireSet > timeRemaining;
//		logger.info("{}ms remaining in total, and we estimate {}ms for phase 2. Terminate phase 1: {}", timeRemaining, estimateForPhase2, terminatePhase1);
//		return terminatePhase1;
//	}
//
//	private synchronized List<HASCOForMEKASolution> getSelectionForPhase2() {
//		return getSelectionForPhase2(Integer.MAX_VALUE);
//	}
//
//	private synchronized List<HASCOForMEKASolution> getSelectionForPhase2(int remainingTime) {
//
//		if (numberOfConsideredSolutions < 1)
//			throw new UnsupportedOperationException(
//					"Cannot determine candidates for phase 2 if their number is set to a value less than 1. Here, it has been set to " + numberOfConsideredSolutions);
//
//		/* some initial checks for cases where we do not really have to do anything */
//		if (remainingTime < 0)
//			throw new IllegalArgumentException("Cannot do anything in negative time (" + remainingTime + "ms)");
//		HASCOForMEKASolution internallyOptimalSolution = hasco.getCurrentlyBestSolution();
//		if (internallyOptimalSolution == null)
//			return new ArrayList<>();
//		if (!isSelectionActivated()) {
//			List<HASCOForMEKASolution> best = new ArrayList<>();
//			best.add(internallyOptimalSolution);
//			return best;
//		}
//
//		/* compute k pipeline candidates (the k/2 best, and k/2 random ones that do not deviate too much from the best one) */
//		double optimalInternalScore = internallyOptimalSolution.getScore();
//		int maxMarginFrombest = 300;
//		int bestK = (int) Math.ceil(numberOfConsideredSolutions / 2);
//		int randomK = numberOfConsideredSolutions - bestK;
//		Collection<HASCOForMEKASolution> potentialCandidates = new ArrayList<>(hasco.getFoundClassifiers()).stream().filter(solution -> {
//			return solution.getScore() <= optimalInternalScore + maxMarginFrombest;
//		}).collect(Collectors.toList());
//		logger.debug("Computing {} best and {} random solutions for a max runtime of {}. Number of candidates that are at most {} worse than optimum {} is: {}/{}", bestK, randomK,
//				remainingTime, maxMarginFrombest, optimalInternalScore, potentialCandidates.size(), hasco.getFoundClassifiers().size());
//		List<HASCOForMEKASolution> selectionCandidates = potentialCandidates.stream().limit(bestK).collect(Collectors.toList());
//		List<HASCOForMEKASolution> remainingCandidates = new ArrayList<>(SetUtil.difference(potentialCandidates, selectionCandidates));
//		Collections.shuffle(remainingCandidates, new Random(randomSeed));
//		selectionCandidates.addAll(remainingCandidates.stream().limit(randomK).collect(Collectors.toList()));
//		
//		/* if the candidates can be evaluated in the remaining time, return all of them */
//		int budget = getExpectedRuntimeForPhase2ForAGivenPool(selectionCandidates);
//		if (budget < remainingTime) {
//			return selectionCandidates;
//		}
//
//		/* otherwise return as much as can be expectedly done in the time */
//		List<HASCOForMEKASolution> actuallySelectedSolutions = new ArrayList<>();
//		int expectedRuntime;
//		for (HASCOForMEKASolution pl : selectionCandidates) {
//			actuallySelectedSolutions.add(pl);
//			expectedRuntime = getExpectedRuntimeForPhase2ForAGivenPool(actuallySelectedSolutions);
//			if (expectedRuntime > remainingTime && actuallySelectedSolutions.size() > 1) {
//				logger.debug("Not considering solution {} for phase 2, because the expected runtime of the whole thing would be {}/{}", pl, expectedRuntime, remainingTime);
//				actuallySelectedSolutions.remove(pl);
//			}
//		}
//		assert getExpectedRuntimeForPhase2ForAGivenPool(
//				actuallySelectedSolutions) > remainingTime : "Invalid result. Expected runtime is higher than it should be based on the computation.";
//		return actuallySelectedSolutions;
//	}
//
//	private int getInSearchEvaluationTimeOfSolutionSet(Collection<HASCOForMEKASolution> solutions) {
//		return hasco.getFoundClassifiers().stream().map(pl -> pl.getTimeForScoreComputation()).reduce(0, (a, b) -> a + b).intValue();
//	}
//
//	public int getExpectedRuntimeForPhase2ForAGivenPool(Collection<HASCOForMEKASolution> solutions) {
//		long estimateForPhase2IfSequential = (int) (getInSearchEvaluationTimeOfSolutionSet(solutions) / (1 - portionOfDataForPhase2)); // consider the fact the inner search train time was on only 70% of the data
//		long estimateForAvailableCores = numberOfCPUs;
//		// double cacheFactor = Math.pow(getNumberOfCPUs(), -.6);
//		double cacheFactor = .8;
//		double conservativenessFactor = 1;
//		int runtime = (int) (conservativenessFactor * estimateForPhase2IfSequential * numberOfMCIterationsPerSolutionInSelectionPhase * cacheFactor / estimateForAvailableCores);
//		logger.debug("Expected runtime is {} = {} * {} * {} * {} / {}", runtime, conservativenessFactor, estimateForPhase2IfSequential,
//				numberOfMCIterationsPerSolutionInSelectionPhase, cacheFactor, estimateForAvailableCores);
//		return runtime;
//	}
//
//	protected MultiLabelClassifier selectModel(ClassifierEvaluator evaluator) {
//
//		Queue<HASCOForMEKASolution> solutions = hasco.getFoundClassifiers();
//		HASCOForMEKASolution bestSolution = solutions.peek();
//		double scoreOfBestSolution = bestSolution.getScore();
//
//		if (!isSelectionActivated()) {
//			logger.info("Selection disabled, just returning first element.");
//			return bestSolution.getClassifier();
//		}
//
//		/* determine the models from which we want to select */
//		logger.info("Starting with phase 2: Selection of final model among the {} solutions that were identified.", solutions.size());
//		long startOfPhase2 = System.currentTimeMillis();
//		List<HASCOForMEKASolution> ensembleToSelectFrom;
//		if (timeoutInS > 0) {
//			int remainingTime = (int) (timeoutInS * 1000 - (System.currentTimeMillis() - timeOfStart));
//			if (remainingTime < 0) {
//				logger.info("Timelimit is already exhausted, just returning a greedy solution that had internal error {}.", scoreOfBestSolution);
//				return bestSolution.getClassifier();
//			}
//			ensembleToSelectFrom = getSelectionForPhase2(remainingTime); // should be ordered by f-value already (at least the first k)
//			int expectedTimeForSolution;
//			expectedTimeForSolution = getExpectedRuntimeForPhase2ForAGivenPool(ensembleToSelectFrom);
//			remainingTime = (int) (timeoutInS * 1000 - (System.currentTimeMillis() - timeOfStart));
//
//			if (expectedTimeForSolution > remainingTime)
//				logger.warn("Only {}ms remaining. We probably cannot make it in time.", remainingTime);
//			logger.info("We expect phase 2 to consume {}ms for {} candidates. {}ms are permitted by timeout. The following pipelines are considered: ", expectedTimeForSolution,
//					ensembleToSelectFrom.size(), remainingTime);
//			// ensembleToSelectFrom.stream().forEach(pl -> logger.info("\t{}: {} (f) / {} (t)", pl, hasco, getAnnotation(pl).getFTime()));
//		} else
//			ensembleToSelectFrom = getSelectionForPhase2();
//
//		AtomicInteger evaluatorCounter = new AtomicInteger(0);
//		ExecutorService pool = Executors.newFixedThreadPool(numberOfCPUs, r -> {
//			Thread t = new Thread(r);
//			t.setName("final-evaluator-" + evaluatorCounter.incrementAndGet());
//			return t;
//		});
//		HASCOForMEKASolution selectedModel = bestSolution; // backup solution
//		final Semaphore sem = new Semaphore(0);
//		long timestampOfDeadline = timeOfStart + timeoutInS * 1000;
//
//		/* evaluate each candiate */
//		List<DescriptiveStatistics> stats = new ArrayList<>();
//		final TimeoutSubmitter ts = TimeoutTimer.getInstance().getSubmitter();
//		ensembleToSelectFrom.forEach(c -> stats.add(new DescriptiveStatistics()));
//		for (int i = 0; i < ensembleToSelectFrom.size(); i++) {
//			HASCOForMEKASolution c = ensembleToSelectFrom.get(i);
//			final DescriptiveStatistics statsForThisCandidate = stats.get(i);
//			for (int j = 0; j < numberOfMCIterationsPerSolutionInSelectionPhase; j++) {
//				pool.submit(new Runnable() {
//					public void run() {
//						int taskId = -1;
//						try {
//							int indexOfCurrentlyChosenModel = getClassifierThatWouldCurrentlyBeSelectedWithinPhase2(ensembleToSelectFrom, stats, false);
//							HASCOForMEKASolution currentlyChosenSolution = ensembleToSelectFrom.get(indexOfCurrentlyChosenModel);
//							int trainingTimeForChosenModelInsideSearch = currentlyChosenSolution.getTimeForScoreComputation();
//							int estimatedOverallTrainingTimeForChosenModel = (int) Math.round(trainingTimeForChosenModelInsideSearch / (1 - portionOfDataForPhase2) / .7); // we assume a linear growth
//							int expectedTrainingTimeOfThisModel = (int) Math.round(c.getTimeForScoreComputation() / .7);
//							if (timeoutPerNodeFComputation > 0)
//								taskId = ts.interruptMeAfterMS(timeoutPerNodeFComputation);
//							if (timeoutInS > 0) {
//								int remainingTime = (int) (timestampOfDeadline - System.currentTimeMillis());
//								if (estimatedOverallTrainingTimeForChosenModel + expectedTrainingTimeOfThisModel >= remainingTime) {
//									logger.info(
//											"Not evaluating solutiom {} anymore, because expected time is {}, overall training time of currently selected solution is {}. This adds up to {}, which exceeds the remaining time of {}!",
//											c, expectedTrainingTimeOfThisModel, estimatedOverallTrainingTimeForChosenModel,
//											expectedTrainingTimeOfThisModel + estimatedOverallTrainingTimeForChosenModel, remainingTime);
//									return;
//								}
//							}
//							Classifier clone = WekaUtil.cloneClassifier(c.getClassifier());
//							double selectionScore = evaluator.evaluate(clone);
//							synchronized (statsForThisCandidate) {
//								statsForThisCandidate.addValue(selectionScore);
//							}
//						} catch (Throwable e) {
//							logger.error("Observed an exeption when trying to evaluate a candidate in the selection phase. Details:\n{}", LoggerUtil.getExceptionInfo(e));
//						} finally {
//							sem.release();
//							if (taskId >= 0)
//								ts.cancelTimeout(taskId);
//						}
//					}
//				});
//			}
//		}
//
//		try {
//
//			/* now wait for results */
//			logger.info("Waiting for termination of threads that compute the selection scores.");
//			sem.acquire(ensembleToSelectFrom.size() * numberOfMCIterationsPerSolutionInSelectionPhase);
//			long endOfPhase2 = System.currentTimeMillis();
//			logger.info("Finished phase 2 within {}ms net. Total runtime was {}ms. ", endOfPhase2 - startOfPhase2, endOfPhase2 - timeOfStart);
//			logger.debug("Shutting down thread pool");
//			pool.shutdownNow();
//			pool.awaitTermination(5, TimeUnit.SECONDS);
//			if (!pool.isShutdown())
//				logger.warn("Thread pool is not shut down yet!");
//
//			/* set chosen model */
//			if (ensembleToSelectFrom.isEmpty()) {
//				logger.warn("No solution contained in ensemble.");
//			} else {
//				int selectedModelIndex = getClassifierThatWouldCurrentlyBeSelectedWithinPhase2(ensembleToSelectFrom, stats, true);
//				selectedModel = ensembleToSelectFrom.get(selectedModelIndex);
//				DescriptiveStatistics statsOfBest = stats.get(selectedModelIndex);
//				logger.info("Selected a model. The model is: {}. Its internal error was {}. Validation error was {}", selectedModel,
//						selectedModel.getScore(), statsOfBest.getMean());
//			}
//
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//		// else
//		// logger.warn("Thread pool still active!");
//		return selectedModel.getClassifier();
//	}
//
//	private synchronized int getClassifierThatWouldCurrentlyBeSelectedWithinPhase2(List<HASCOForMEKASolution> ensembleToSelectFrom, List<DescriptiveStatistics> stats,
//			boolean logComputations) {
//		int selectedModel = 0;
//		double best = Double.MAX_VALUE;
//		for (int i = 0; i < ensembleToSelectFrom.size(); i++) {
//			HASCOForMEKASolution candidate = ensembleToSelectFrom.get(i);
//			DescriptiveStatistics statsOfCandidate = stats.get(i);
//			if (statsOfCandidate.getN() == 0) {
//				if (logComputations)
//					logger.info("Ignoring candidate {} because no results were obtained in selection phase.", candidate);
//				continue;
//			}
//			double avgError = statsOfCandidate.getMean() / 100f;
//			double quartileScore = statsOfCandidate.getPercentile(75) / 100;
//			double score = (avgError + quartileScore) / 2f;
//			if (logComputations)
//				logger.info("Score of candidate {} is {} based on {} (avg) and {} (.75-pct) with {} samples", candidate, score, avgError, quartileScore, statsOfCandidate.getN());
//			if (score < best) {
//				best = score;
//				selectedModel = i;
//			}
//		}
//		return selectedModel;
//	}
//
//	public int getTimeoutPerNodeFComputation() {
//		return timeoutPerNodeFComputation;
//	}
//
//	public void setTimeoutPerNodeFComputation(int timeoutPerNodeFComputation) {
//		this.timeoutPerNodeFComputation = timeoutPerNodeFComputation;
//	}
//
//	public int getRandomSeed() {
//		return randomSeed;
//	}
//
//	public void setRandomSeed(int randomSeed) {
//		this.randomSeed = randomSeed;
//	}
//
//	public int getNumberOfCPUs() {
//		return numberOfCPUs;
//	}
//
//	public void setNumberOfCPUs(int numberOfCPUs) {
//		this.numberOfCPUs = numberOfCPUs;
//	}
//
//	public int getMemory() {
//		return memory;
//	}
//
//	public void setMemory(int memory) {
//		this.memory = memory;
//	}
//
//	public float getPortionOfDataForPhase2() {
//		return portionOfDataForPhase2;
//	}
//
//	public void setPortionOfDataForPhase2(float portionOfDataForPhase2) {
//		this.portionOfDataForPhase2 = portionOfDataForPhase2;
//	}
//
//	public int getNumberOfConsideredSolutions() {
//		return numberOfConsideredSolutions;
//	}
//
//	public void setNumberOfConsideredSolutions(int numberOfConsideredSolutions) {
//		this.numberOfConsideredSolutions = numberOfConsideredSolutions;
//	}
//
//	public int getNumberOfMCIterationsPerSolutionInSelectionPhase() {
//		return numberOfMCIterationsPerSolutionInSelectionPhase;
//	}
//
//	public void setNumberOfMCIterationsPerSolutionInSelectionPhase(int numberOfMCIterationsPerSolutionInSelectionPhase) {
//		this.numberOfMCIterationsPerSolutionInSelectionPhase = numberOfMCIterationsPerSolutionInSelectionPhase;
//	}
//
//	public int getTimeout() {
//		return timeoutInS;
//	}
//
//	public MultiLabelClassifier getSelectedClassifier() {
//		return selectedClassifier;
//	}
//
//	@Override
//	public void registerListener(Object listener) {
//		hasco.registerListener(listener);
//	}
//	
//	/**
//	 * Set the preferred node evaluator. If this returns NULL, the random completion is conducted
//	 */
//	public void setNodeEvaluator(INodeEvaluator<TFDNode, Double> nodeEvaluator) {
//		hasco.setPreferredNodeEvaluator(nodeEvaluator);
//	}
//}
