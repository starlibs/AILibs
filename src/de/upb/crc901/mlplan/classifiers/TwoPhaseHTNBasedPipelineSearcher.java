package de.upb.crc901.mlplan.classifiers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.crc901.mlplan.core.MLPipeline;
import de.upb.crc901.mlplan.core.MLPipelineSolutionAnnotation;
import de.upb.crc901.mlplan.core.MLUtil;
import de.upb.crc901.mlplan.core.SolutionEvaluator;
import de.upb.crc901.mlplan.search.algorithms.GraphBasedPipelineSearcher;
import de.upb.crc901.mlplan.search.evaluators.CVEvaluator;
import de.upb.crc901.mlplan.search.evaluators.MonteCarloCrossValidationEvaluator;
import de.upb.crc901.mlplan.search.evaluators.ProcessBasedSolutionEvaluator;
import de.upb.crc901.mlplan.search.evaluators.RandomCompletionEvaluator;
import jaicore.basic.SetUtil;
import jaicore.ml.WekaUtil;
import jaicore.planning.graphgenerators.task.ceociptfd.CEOCIPTFDGraphGenerator;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.planning.model.task.ceocstn.CEOCSTNUtil;
import jaicore.search.algorithms.interfaces.IObservableORGraphSearch;
import jaicore.search.algorithms.parallel.parallelexploration.distributed.interfaces.SerializableGraphGenerator;
import jaicore.search.algorithms.standard.core.INodeEvaluator;
import jaicore.search.algorithms.standard.core.ORGraphSearch;
import jaicore.search.structure.core.GraphGenerator;
import weka.classifiers.evaluation.Evaluation;
import weka.core.Instances;

public class TwoPhaseHTNBasedPipelineSearcher<V extends Comparable<V>> extends GraphBasedPipelineSearcher<TFDNode, String, V> {

	private final static Logger logger = LoggerFactory.getLogger(TwoPhaseHTNBasedPipelineSearcher.class);
	private File htnSearchSpaceFile, evaluablePredicateFile;
	private SerializableGraphGenerator<TFDNode, String> graphGenerator;
	private int timeoutPerNodeFComputation;
	private File tmpDir;
	private int memory = 256;
	private int memoryOverheadPerProcessInMB = 256;
	private Instances dataForSearch, dataPreservedForSelection;
	private SolutionEvaluator solutionEvaluator;
	private float portionOfDataForPhase2 = 0;
	private RandomCompletionEvaluator<V> rce;
	private int numberOfConsideredSolutions = 100;
	protected int numberOfMCIterationsPerSolutionInSelectionPhase = 3;

	public TwoPhaseHTNBasedPipelineSearcher() { }

	public TwoPhaseHTNBasedPipelineSearcher(File testsetFile, File evaluablePredicates, Instances dataset) throws IOException {
		this(MLUtil.getGraphGenerator(testsetFile, evaluablePredicates, dataset));
	}

	public TwoPhaseHTNBasedPipelineSearcher(SerializableGraphGenerator<TFDNode, String> graphGenerator) throws IOException {
		this(graphGenerator, new Random(0), 60 * 1000, 5000, 20, 50, false);
	}

	public TwoPhaseHTNBasedPipelineSearcher(SerializableGraphGenerator<TFDNode, String> graphGenerator, Random random, int timeoutTotal, int timeoutPerNodeFComputation,
			int numberOfSolutions, int selectionDepth, boolean showGraph) {
		super(random, timeoutTotal, showGraph);
		this.graphGenerator = graphGenerator;
		this.timeoutPerNodeFComputation = timeoutPerNodeFComputation;
	}

	@Override
	protected IObservableORGraphSearch<TFDNode, String, V> getSearch(Instances data) throws IOException {
		
		if (graphGenerator == null) {
			System.out.println("Automatically building graph generator using HTN file " + htnSearchSpaceFile + ", evaluable predicates " + evaluablePredicateFile + ", and dataset " + data.relationName() + " with " + data.numClasses() + " classes and " + data.size() + " points.");
			graphGenerator = MLUtil.getGraphGenerator(htnSearchSpaceFile, evaluablePredicateFile, data);
		}

		if (rce == null) {
			throw new IllegalStateException("Cannot determine search before random completion evaluator has been set.");
		}

		/* split data set */
		if (portionOfDataForPhase2 > 0) {
			List<Instances> split = WekaUtil.getStratifiedSplit(data, getRandom(), portionOfDataForPhase2 / 100);
			this.dataForSearch = split.get(1);
			this.dataPreservedForSelection = split.get(0);
		} else {
			this.dataForSearch = data;
			this.dataPreservedForSelection = null;
		}
		rce.setGenerator(graphGenerator);
		rce.setData(dataForSearch);
		// TimeLoggingNodeEvaluator<TFDNode, Integer> nodeEvaluator = new TimeLoggingNodeEvaluator<>(rce);

		/* we allow CPUs-1 threads for node evaluation. Setting the timeout evaluator to null means to really prune all those */
		if (getNumberOfCPUs() < 1)
			throw new IllegalStateException("Cannot generate search where number of CPUs is " + getNumberOfCPUs());
		ORGraphSearch<TFDNode, String, V> search = createActualSearchObject(graphGenerator, rce, getNumberOfCPUs());
		search.setTimeoutForComputationOfF(timeoutPerNodeFComputation, n -> null);
		return search;
	}

	protected ORGraphSearch<TFDNode, String, V> createActualSearchObject(GraphGenerator<TFDNode, String> graphGenerator, INodeEvaluator<TFDNode, V> nodeEvaluator,
			int numberOfCPUs) {
		ORGraphSearch<TFDNode, String, V> search = new ORGraphSearch<>(graphGenerator, nodeEvaluator);
		if (numberOfCPUs > 1) {
			search.parallelizeNodeExpansion(numberOfCPUs - 1);
		}
		return search;
	}

	public MLPipelineSolutionAnnotation<V> getAnnotation(MLPipeline pipeline) {
		return (MLPipelineSolutionAnnotation<V>) super.getAnnotation(pipeline);
	}

	protected void logSolution(MLPipeline mlp) {
		logger.info("Registered solution #{} with f-value {} (computation took {}). Budget of current solution pool: {}. Solution: {}", solutions.size(),
				solutionAnnotationCache.get(mlp).getF(), getAnnotation(mlp).getFTime(), getInSearchEvaluationTimeOfSolutionSet(getSelectionForPhase2()), mlp);
	}

	@Override
	protected MLPipeline convertPathToPipeline(List<TFDNode> path) {
		return MLUtil.extractPipelineFromPlan(CEOCSTNUtil.extractPlanFromSolutionPath(path));
	}

	public SerializableGraphGenerator<TFDNode, String> getGraphGenerator() {
		return graphGenerator;
	}

	public int getTimeoutPerNodeFComputation() {
		return timeoutPerNodeFComputation;
	}

	public void setTimeoutPerNodeFComputation(int timeoutPerNodeFComputation) {
		this.timeoutPerNodeFComputation = timeoutPerNodeFComputation;
	}

	public File getTmpDir() {
		return tmpDir;
	}

	public void setTmpDir(File tmpDir) {
		this.tmpDir = tmpDir;
	}

	public int getMemory() {
		return memory;
	}

	public void setMemory(int memoryInMB) {
		this.memory = memoryInMB;
	}

	public int getMemoryOverheadPerProcessInMB() {
		return memoryOverheadPerProcessInMB;
	}

	public void setMemoryOverheadPerProcessInMB(int memoryOverheadPerProcessInMB) {
		this.memoryOverheadPerProcessInMB = memoryOverheadPerProcessInMB;
	}

	public SolutionEvaluator getSolutionEvaluator() {
		return solutionEvaluator;
	}

	public void setSolutionEvaluator(String validationAlgorithmKey) throws IOException {

		/* set validation mechanism */
		validationAlgorithmKey = validationAlgorithmKey.toLowerCase();
		if (validationAlgorithmKey.matches("\\d+-cv")) {
			int folds = Integer.valueOf(validationAlgorithmKey.substring(0, validationAlgorithmKey.indexOf("-")));
			solutionEvaluator = new CVEvaluator(folds);
		} else if (validationAlgorithmKey.matches("mccv-\\d+-\\d+")) {
			String[] split = validationAlgorithmKey.split("-");
			int repeats = Integer.parseInt(split[1].trim());
			float trainingPortion = Integer.parseInt(split[2].trim()) / 100f;
			solutionEvaluator = new MonteCarloCrossValidationEvaluator(repeats, trainingPortion);

		} else {
			throw new UnsupportedOperationException("No validator available for \"" + validationAlgorithmKey + "\"");
		}
	}

	public void setSolutionEvaluator(SolutionEvaluator solutionEvaluator) {
		this.solutionEvaluator = solutionEvaluator;
	}

	public float getPortionOfDataForPhase2() {
		return portionOfDataForPhase2;
	}

	public void setPortionOfDataForPhase2(float portionOfDataForPhase2) {
		this.portionOfDataForPhase2 = portionOfDataForPhase2;
	}

	public boolean isSelectionActivated() {
		return portionOfDataForPhase2 > 0;
	}

	@Override
	protected boolean shouldSearchTerminate(long timeRemaining) {
		Collection<MLPipeline> currentSelection = getSelectionForPhase2();
		int estimateForPhase2 = isSelectionActivated() ? getExpectedRuntimeForPhase2ForAGivenPool(currentSelection) : 0;
		boolean terminatePhase1 = estimateForPhase2 + 5000 > timeRemaining;
		logger.debug("{}ms remaining in total, and we estimate {}ms for phase 2. Terminate phase 1: {}", timeRemaining, estimateForPhase2, terminatePhase1);
		return terminatePhase1;
	}

	private synchronized Collection<MLPipeline> getSelectionForPhase2() {
		return getSelectionForPhase2(Integer.MAX_VALUE);
	}

	private synchronized Collection<MLPipeline> getSelectionForPhase2(int remainingTime) {
		if (remainingTime < 0)
			throw new IllegalArgumentException("Cannot do anything in negative time (" + remainingTime + "ms)");
		MLPipeline internallyOptimalSolution = solutions.peek();
		if (internallyOptimalSolution == null)
			return new ArrayList<>();

		/* if no selection is activated, then just return the best one */
		if (!isSelectionActivated()) {
			Collection<MLPipeline> best = new ArrayList<>();
			best.add(internallyOptimalSolution);
			return best;
		}

		MLPipelineSolutionAnnotation<V> optimalInternalScoreTmp = getAnnotation(internallyOptimalSolution);
		double optimalInternalScore = Double.valueOf(String.valueOf(optimalInternalScoreTmp.getF()));
		int maxMarginFrombest = 300;
		int bestK = (int) Math.ceil(numberOfConsideredSolutions / 2);
		int randomK = numberOfConsideredSolutions - bestK;
		Collection<MLPipeline> potentialCandidates = new ArrayList<>(solutions).stream().filter(pl -> {
			if (getAnnotation(pl) == null)
				logger.warn("Could not find any annotation for solution {}.", pl);
			return getAnnotation(pl) != null && Double.parseDouble(String.valueOf(getAnnotation(pl).getF())) <= optimalInternalScore + maxMarginFrombest;
		}).collect(Collectors.toList());
		logger.debug("Computing {} best and {} random solutions for a max runtime of {}. Number of candidates that are at most {} worse than optimum {} is: {}/{}", bestK, randomK,
				remainingTime, maxMarginFrombest, optimalInternalScore, potentialCandidates.size(), solutions.size());
		Collection<MLPipeline> selectionCandidates = potentialCandidates.stream().limit(bestK).collect(Collectors.toList());
		List<MLPipeline> remainingCandidates = new ArrayList<>(SetUtil.difference(potentialCandidates, selectionCandidates));
		Collections.shuffle(remainingCandidates, getRandom());
		selectionCandidates.addAll(remainingCandidates.stream().limit(randomK).collect(Collectors.toList()));

		/* now get the first k solutions */
		int budget = getExpectedRuntimeForPhase2ForAGivenPool(selectionCandidates);
		if (budget < remainingTime) {
			return selectionCandidates;
		}

		List<MLPipeline> actuallySelectedSolutions = new ArrayList<>();
		int expectedRuntime;
		for (MLPipeline pl : selectionCandidates) {
			actuallySelectedSolutions.add(pl);
			expectedRuntime = getExpectedRuntimeForPhase2ForAGivenPool(actuallySelectedSolutions);
			if (expectedRuntime > remainingTime) {
				actuallySelectedSolutions.remove(pl);
			}
		}
		expectedRuntime = getExpectedRuntimeForPhase2ForAGivenPool(actuallySelectedSolutions);
		if (expectedRuntime > remainingTime)
			throw new IllegalStateException("Invalid result. Expected runtime is " + expectedRuntime + " but required is " + remainingTime);
		return actuallySelectedSolutions;
	}

	private int getInSearchEvaluationTimeOfSolutionSet(Collection<MLPipeline> solutions) {
		return solutions.stream().map(pl -> getAnnotation(pl).getFTime()).reduce(0, (a, b) -> a + b).intValue();
	}

	public int getExpectedRuntimeForPhase2ForAGivenPool(Collection<MLPipeline> solutions) {
		long estimateForPhase2IfSequential = (int) (getInSearchEvaluationTimeOfSolutionSet(solutions));
		long estimateForAvailableCores = getNumberOfCPUs();
		double cacheFactor = Math.pow(getNumberOfCPUs(), -.6);
		double conservativenessFactor = 1.1;
		int runtime = (int) (conservativenessFactor * estimateForPhase2IfSequential * numberOfMCIterationsPerSolutionInSelectionPhase * cacheFactor / estimateForAvailableCores);
		logger.debug("Expected runtime is {} = {} * {} * {} * {} / {}", runtime, conservativenessFactor, estimateForPhase2IfSequential, numberOfMCIterationsPerSolutionInSelectionPhase, cacheFactor,
				estimateForAvailableCores);
		return runtime;
	}

	protected MLPipeline selectModel() {
		if (!isSelectionActivated()) {
			logger.info("Selection disabled, just returning first element.");
			return solutions.peek();
		}

		if (!(solutionAnnotationCache.get(solutions.peek()).getF() instanceof Number)) {
			logger.info("Avoid phase 2 since node evaluation criterion is not a number and we, hence, cannot apply the epsilon-technique to it. Just returning the best model.");
			return solutions.peek();
		}

		/* eval these candidates */
		logger.info("Starting with phase 2: Selection of final model.");
		long start = System.currentTimeMillis();
		double bestScore = 100;
		long baseForRandom = start;
		Instances dataForSelection = new Instances(this.dataForSearch);
		dataForSelection.addAll(this.dataPreservedForSelection);
		Collection<MLPipeline> ensembleToSelectFrom;
		double remainingTime;
		int expectedTimeForSolution;
		ensembleToSelectFrom = getSelectionForPhase2((int) (getTimeout() - (System.currentTimeMillis() - start)));
		expectedTimeForSolution = getExpectedRuntimeForPhase2ForAGivenPool(ensembleToSelectFrom);
		remainingTime = (int) (getTimeout() - (System.currentTimeMillis() - start));
		if (expectedTimeForSolution > remainingTime)
			logger.warn("Only {}ms remaining. We probably cannot make it in time.", remainingTime);
		logger.info("We expect phase 2 to consume {}ms. {}ms are permitted by timeout. The following pipelines are considered: ", expectedTimeForSolution, remainingTime);
		ensembleToSelectFrom.stream().forEach(pl -> logger.info("\t{}: {} (f) / {} (t)", pl, solutionAnnotationCache.get(pl).getF(), getAnnotation(pl).getFTime()));
		ExecutorService pool = Executors.newFixedThreadPool(getNumberOfCPUs(), r -> {
			Thread t = new Thread(r);
			t.setName("final-evaluator-");
			return t;
		});
		long startOfPhase2 = System.currentTimeMillis();
		MLPipeline selectedModel = null;
		for (MLPipeline pl : ensembleToSelectFrom) {
			try {

				double believedScore = Double.parseDouble(String.valueOf(getAnnotation(pl).getF()));

				/* determine average accuracy and possibly update currently best classifier */
				DescriptiveStatistics stats = new DescriptiveStatistics();
				Semaphore sem = new Semaphore(0);
				IntStream.range(0, numberOfMCIterationsPerSolutionInSelectionPhase).forEach(k -> pool.submit(new Runnable() {

					@Override
					public void run() {
						List<Instances> validationSplit;
						MLPipeline mlCopy;
						synchronized (this) {
							validationSplit = WekaUtil.getStratifiedSplit(dataForSelection, new Random(baseForRandom * k), .7f);
							mlCopy = MLUtil.extractPipelineFromPlan(pl.getCreationPlan()); // create a copy for the evaluation
						}
						try {
							mlCopy.buildClassifier(validationSplit.get(0));
							Evaluation eval = new Evaluation(validationSplit.get(0));
							eval.evaluateModel(mlCopy, validationSplit.get(1));
							synchronized (stats) {
								stats.addValue(eval.pctIncorrect() + eval.pctUnclassified());
							}
						} catch (Throwable e) {
							System.err.println("Problems when evaluating pipeline: " + mlCopy);
							e.printStackTrace();
						} finally {
							logger.debug("Releasing token to semaphore ...");
							sem.release();
						}
					}
				}));
				logger.info("Awaiting termination of quality computations for solution {},...", pl);
				sem.acquire(numberOfMCIterationsPerSolutionInSelectionPhase);
				if (stats.getN() > 0) {
					double avgError = stats.getMean() / 100f;
					double quartileScore = stats.getPercentile(75) / 100;
					double score = (avgError + quartileScore) / 2f;
					logger.info("Ready. Error of solution: {}/{} = {}. Believed value was: {}. {} (preprocessors) {} (classifier).", avgError, quartileScore, score,
							believedScore / 100f, pl.getPreprocessors(), WekaUtil.getClassifierDescriptor(pl.getBaseClassifier()));
					if (score < bestScore) {
						bestScore = score;
						selectedModel = pl;
						logger.info("This is the new best one ...");
					}
				} else {
					logger.warn("No evaluation results for solution {}", pl);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		long endOfPhase2 = System.currentTimeMillis();
		logger.info("Finished phase 2 within {}ms net. Total runtime was {}ms. Selected model is {}", endOfPhase2 - startOfPhase2, endOfPhase2 - start, selectedModel);
		logger.debug("Shutting down thread pool");
		pool.shutdownNow();
		try {
			pool.awaitTermination(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (pool.isShutdown())
			logger.debug("Thread pool down");
		else
			logger.warn("Thread pool still active!");
		return selectedModel;
	}

	public RandomCompletionEvaluator<V> getRce() {
		return rce;
	}

	public void setRce(RandomCompletionEvaluator<V> rce) {
		this.rce = rce;
	}

	public int getNumberOfMCIterationsPerSolutionInSelectionPhase() {
		return numberOfMCIterationsPerSolutionInSelectionPhase;
	}

	public void setNumberOfMCIterationsPerSolutionInSelectionPhase(int selectionDepth) {
		this.numberOfMCIterationsPerSolutionInSelectionPhase = selectionDepth;
	}

	public int getNumberOfConsideredSolutions() {
		return numberOfConsideredSolutions;
	}

	public void setNumberOfConsideredSolutions(int numberOfConsideredSolutions) {
		this.numberOfConsideredSolutions = numberOfConsideredSolutions;
	}

	public File getHtnSearchSpaceFile() {
		return htnSearchSpaceFile;
	}

	public void setHtnSearchSpaceFile(File htnSearchSpaceFile) {
		this.htnSearchSpaceFile = htnSearchSpaceFile;
	}

	public File getEvaluablePredicateFile() {
		return evaluablePredicateFile;
	}

	public void setEvaluablePredicateFile(File evaluablePredicateFile) {
		this.evaluablePredicateFile = evaluablePredicateFile;
	}

	public void setGraphGenerator(SerializableGraphGenerator<TFDNode, String> graphGenerator) {
		this.graphGenerator = graphGenerator;
	}
}
