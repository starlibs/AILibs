package de.upb.crc901.mlplan.classifiers;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.AtomicDouble;

import de.upb.crc901.mlplan.core.ClassifierSolutionAnnotation;
import de.upb.crc901.mlplan.core.ISolutionEvaluatorFactory;
import de.upb.crc901.mlplan.core.MLUtil;
import de.upb.crc901.mlplan.core.SolutionEvaluator;
import de.upb.crc901.mlplan.oracles.RPNDOracleTaskSolver;
import de.upb.crc901.mlplan.search.algorithms.GraphBasedPipelineSearcher;
import de.upb.crc901.mlplan.search.evaluators.BasicMLEvaluator;
import de.upb.crc901.mlplan.search.evaluators.RandomCompletionEvaluator;
import jaicore.basic.SetUtil;
import jaicore.ml.WekaUtil;
import jaicore.planning.graphgenerators.task.ceociptfd.OracleTaskResolver;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.planning.graphgenerators.task.tfd.TFDTooltipGenerator;
import jaicore.planning.model.task.ceocstn.CEOCSTNUtil;
import jaicore.search.algorithms.parallel.parallelexploration.distributed.interfaces.SerializableGraphGenerator;
import jaicore.search.algorithms.standard.core.INodeEvaluator;
import jaicore.search.algorithms.standard.core.ORGraphSearch;
import jaicore.search.structure.core.GraphGenerator;
import weka.classifiers.Classifier;
import weka.core.Instances;

public class TwoPhaseHTNBasedPipelineSearcher<V extends Comparable<V>> extends GraphBasedPipelineSearcher<TFDNode, String, V> implements Serializable {

	private final static Logger logger = LoggerFactory.getLogger(TwoPhaseHTNBasedPipelineSearcher.class);
	private File htnSearchSpaceFile, evaluablePredicateFile;
	private transient Map<String, OracleTaskResolver> oracleResolvers; // kann man im Moment nicht gut in Datei auslagern, weil die parametrisiert werden müssen
	private SerializableGraphGenerator<TFDNode, String> graphGenerator;
	private int timeoutPerNodeFComputation;
	private File tmpDir;
	private int memory = 256;
	private int memoryOverheadPerProcessInMB = 256;
	private Instances dataForSearch, dataPreservedForSelection;
	private ISolutionEvaluatorFactory solutionEvaluatorFactory4Search;
	private ISolutionEvaluatorFactory solutionEvaluatorFactory4Selection;
	private float portionOfDataForPhase2 = 0;
	private RandomCompletionEvaluator<V> rce;
	private int numberOfConsideredSolutions = 100;
	protected int numberOfMCIterationsPerSolutionInSelectionPhase = 3;

	public TwoPhaseHTNBasedPipelineSearcher() {
	}

	public TwoPhaseHTNBasedPipelineSearcher(File testsetFile, File evaluablePredicates, Instances dataset) throws IOException {
		this(MLUtil.getGraphGenerator(testsetFile, evaluablePredicates, null, dataset));
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
	protected ORGraphSearch<TFDNode, String, V> getSearch(Instances data) throws IOException {
		
		/* TODO DIRRRTYYYYY!!!! set oracle resolver */
//		oracleResolvers = new HashMap<>();
//		oracleResolvers.put("configChildNodesT",
//				new RPNDOracleTaskSolver(getRandom(), "weka.classifiers.trees.RandomTree", data, MLUtil.getPlanningProblem(htnSearchSpaceFile, data)));

		if (graphGenerator == null) {
			System.out.println("Automatically building graph generator using HTN file " + htnSearchSpaceFile + ", evaluable predicates " + evaluablePredicateFile + ", and dataset "
					+ data.relationName() + " with " + data.classIndex() + " classes and " + data.size() + " points.");
			graphGenerator = MLUtil.getGraphGenerator(htnSearchSpaceFile, evaluablePredicateFile, oracleResolvers, data);
		}
		
		if (graphGenerator == null) {
			throw new IllegalStateException("No graph generator has been set.");
		}

		if (rce == null) {
			throw new IllegalStateException("Cannot determine search before random completion evaluator has been set.");
		}

		/* split data set */
		if (portionOfDataForPhase2 > 0) {
//			List<Instances> split = WekaUtil.getStratifiedSplit(data, getRandom(), portionOfDataForPhase2 / 100f);
			List<Instances> split = WekaUtil.realizeSplit(data,WekaUtil.getArbitrarySplit(data, getRandom(), portionOfDataForPhase2));
			this.dataForSearch = split.get(1);
			this.dataPreservedForSelection = split.get(0);
			if (dataForSearch.isEmpty()) {
				throw new IllegalStateException("Cannot search on no data and select on " + dataPreservedForSelection.size() + " data points.");
			}
		} else {
			this.dataForSearch = data;
			this.dataPreservedForSelection = null;
		}
		rce.setGenerator(graphGenerator);
		rce.setData(dataForSearch);
		// TimeLoggingNodeEvaluator<TFDNode, Integer> nodeEvaluator = new TimeLoggingNodeEvaluator<>(rce);

		logger.info("Creating search with a data split {}/{} for search/selection, which yields effectively a split of size:  {}/{}", 1 - portionOfDataForPhase2, portionOfDataForPhase2, dataForSearch.size(), dataPreservedForSelection.size());
		
		/* we allow CPUs-1 threads for node evaluation. Setting the timeout evaluator to null means to really prune all those */
		if (getNumberOfCPUs() < 1)
			throw new IllegalStateException("Cannot generate search where number of CPUs is " + getNumberOfCPUs());
		ORGraphSearch<TFDNode, String, V> search = createActualSearchObject(graphGenerator, rce, getNumberOfCPUs());
		search.setLoggerName("mlplan");
		search.setTimeoutForComputationOfF(timeoutPerNodeFComputation, n -> null);
		
		/* reset graph tooltip */
		if (isShowGraph() && tooltipGenerator == null)
			setTooltipGenerator(new TFDTooltipGenerator<>());
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

	public ClassifierSolutionAnnotation<V> getAnnotation(Classifier pipeline) {
		return (ClassifierSolutionAnnotation<V>) super.getAnnotation(pipeline);
	}

	protected void logSolution(Classifier mlp) {
		logger.info("Registered solution #{} with f-value {} (computation took {}). Budget of current solution pool: {}. Solution: {}", solutions.size(),
				solutionAnnotationCache.get(mlp).getF(), getAnnotation(mlp).getFTime(), getInSearchEvaluationTimeOfSolutionSet(getSelectionForPhase2()), mlp);
	}

	@Override
	protected Classifier convertPathToPipeline(List<TFDNode> path) {
		try {
			return MLUtil.extractGeneratedClassifierFromPlan(CEOCSTNUtil.extractPlanFromSolutionPath(path));
		} catch (Throwable e) {
			throw new IllegalArgumentException("Converting path to pipeline caused " + e.getClass().getName() + " with message " + e.getMessage() + " for path " + path);
		}
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

	public ISolutionEvaluatorFactory getSolutionEvaluator() {
		return solutionEvaluatorFactory4Search;
	}

	public void setSolutionEvaluator(BasicMLEvaluator basicEvaluator, String validationAlgorithmKey) throws IOException {

		/* set validation mechanism */
		validationAlgorithmKey = validationAlgorithmKey.toLowerCase();
		if (validationAlgorithmKey.matches("\\d+-cv")) {
			int folds = Integer.valueOf(validationAlgorithmKey.substring(0, validationAlgorithmKey.indexOf("-")));
			// solutionEvaluatorFactory = () -> new CVEvaluator(folds);
		} else if (validationAlgorithmKey.matches("mccv-\\d+-\\d+")) {
			String[] split = validationAlgorithmKey.split("-");
			int repeats = Integer.parseInt(split[1].trim());
			float trainingPortion = Integer.parseInt(split[2].trim()) / 100f;
			// solutionEvaluatorFactory = () -> new MonteCarloCrossValidationEvaluator(basicEvaluator, repeats, trainingPortion);

		} else {
			throw new UnsupportedOperationException("No validator available for \"" + validationAlgorithmKey + "\"");
		}
	}

	public void setSolutionEvaluatorFactory4Search(ISolutionEvaluatorFactory solutionEvaluatorFactory) {
		this.solutionEvaluatorFactory4Search = solutionEvaluatorFactory;
	}

	public void setSolutionEvaluatorFactory4Selection(ISolutionEvaluatorFactory solutionEvaluatorFactory) {
		this.solutionEvaluatorFactory4Selection = solutionEvaluatorFactory;
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
		Collection<Classifier> currentSelection = getSelectionForPhase2();
		int estimateForPhase2 = isSelectionActivated() ? getExpectedRuntimeForPhase2ForAGivenPool(currentSelection) : 0;
		boolean terminatePhase1 = estimateForPhase2 + 5000 > timeRemaining;
		logger.debug("{}ms remaining in total, and we estimate {}ms for phase 2. Terminate phase 1: {}", timeRemaining, estimateForPhase2, terminatePhase1);
		return terminatePhase1;
	}

	private synchronized Collection<Classifier> getSelectionForPhase2() {
		return getSelectionForPhase2(Integer.MAX_VALUE);
	}

	private synchronized Collection<Classifier> getSelectionForPhase2(int remainingTime) {
		
		/* some initial checks for cases where we do not really have to do anything */
		if (remainingTime < 0)
			throw new IllegalArgumentException("Cannot do anything in negative time (" + remainingTime + "ms)");
		Classifier internallyOptimalSolution = solutions.peek();
		if (internallyOptimalSolution == null)
			return new ArrayList<>();
		if (!isSelectionActivated()) {
			Collection<Classifier> best = new ArrayList<>();
			best.add(internallyOptimalSolution);
			return best;
		}
		
		/* compute k pipeline candidates (the k/2 best, and k/2 random ones that do not deviate too much from the best one) */
		ClassifierSolutionAnnotation<V> optimalInternalScoreTmp = getAnnotation(internallyOptimalSolution);
		double optimalInternalScore = Double.valueOf(String.valueOf(optimalInternalScoreTmp.getF()));
		int maxMarginFrombest = 300;
		int bestK = (int) Math.ceil(numberOfConsideredSolutions / 2);
		int randomK = numberOfConsideredSolutions - bestK;
		Collection<Classifier> potentialCandidates = new ArrayList<>(solutions).stream().filter(pl -> {
			if (getAnnotation(pl) == null)
				logger.warn("Could not find any annotation for solution {}.", pl);
			return getAnnotation(pl) != null && Double.parseDouble(String.valueOf(getAnnotation(pl).getF())) <= optimalInternalScore + maxMarginFrombest;
		}).collect(Collectors.toList());
		logger.debug("Computing {} best and {} random solutions for a max runtime of {}. Number of candidates that are at most {} worse than optimum {} is: {}/{}", bestK, randomK,
				remainingTime, maxMarginFrombest, optimalInternalScore, potentialCandidates.size(), solutions.size());
		Collection<Classifier> selectionCandidates = potentialCandidates.stream().limit(bestK).collect(Collectors.toList());
		List<Classifier> remainingCandidates = new ArrayList<>(SetUtil.difference(potentialCandidates, selectionCandidates));
		Collections.shuffle(remainingCandidates, getRandom());
		selectionCandidates.addAll(remainingCandidates.stream().limit(randomK).collect(Collectors.toList()));

		/* if the candidates can be evaluated in the remaining time, return all of them */
		int budget = getExpectedRuntimeForPhase2ForAGivenPool(selectionCandidates);
		if (budget < remainingTime) {
			return selectionCandidates;
		}
		
		/* otherwise return as much as can be expectedly done in the time */
		List<Classifier> actuallySelectedSolutions = new ArrayList<>();
		int expectedRuntime;
		for (Classifier pl : selectionCandidates) {
			actuallySelectedSolutions.add(pl);
			expectedRuntime = getExpectedRuntimeForPhase2ForAGivenPool(actuallySelectedSolutions);
			if (expectedRuntime > remainingTime) {
				actuallySelectedSolutions.remove(pl);
			}
		}
		assert getExpectedRuntimeForPhase2ForAGivenPool(actuallySelectedSolutions) > remainingTime : "Invalid result. Expected runtime is higher than it should be based on the computation.";
		return actuallySelectedSolutions;
	}

	private int getInSearchEvaluationTimeOfSolutionSet(Collection<Classifier> solutions) {
		return solutions.stream().map(pl -> getAnnotation(pl).getFTime()).reduce(0, (a, b) -> a + b).intValue();
	}

	public int getExpectedRuntimeForPhase2ForAGivenPool(Collection<Classifier> solutions) {
		long estimateForPhase2IfSequential = (int) (getInSearchEvaluationTimeOfSolutionSet(solutions));
		long estimateForAvailableCores = getNumberOfCPUs();
		double cacheFactor = Math.pow(getNumberOfCPUs(), -.6);
		double conservativenessFactor = 1.1;
		int runtime = (int) (conservativenessFactor * estimateForPhase2IfSequential * numberOfMCIterationsPerSolutionInSelectionPhase * cacheFactor / estimateForAvailableCores);
		logger.debug("Expected runtime is {} = {} * {} * {} * {} / {}", runtime, conservativenessFactor, estimateForPhase2IfSequential,
				numberOfMCIterationsPerSolutionInSelectionPhase, cacheFactor, estimateForAvailableCores);
		return runtime;
	}

	protected Classifier selectModel() {
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
		long startOfPhase2 = System.currentTimeMillis();
		Instances dataForSelection = new Instances(this.dataForSearch);
		dataForSelection.addAll(this.dataPreservedForSelection);
		int remainingTime = (int) (getTimeout() - (System.currentTimeMillis() - getTimeOfStart()));
		if (remainingTime < 0) {
			logger.info("Timelimit is already exhausted, just returning a greedy solution that had internal error {}.", solutionAnnotationCache.get(solutions.peek()).getF());
			return solutions.peek();
		}
		Collection<Classifier> ensembleToSelectFrom = getSelectionForPhase2(remainingTime);
		int expectedTimeForSolution;
		expectedTimeForSolution = getExpectedRuntimeForPhase2ForAGivenPool(ensembleToSelectFrom);
		remainingTime = (int) (getTimeout() - (System.currentTimeMillis() - getTimeOfStart()));;
		if (expectedTimeForSolution > remainingTime)
			logger.warn("Only {}ms remaining. We probably cannot make it in time.", remainingTime);
		logger.info("We expect phase 2 to consume {}ms. {}ms are permitted by timeout. The following pipelines are considered: ", expectedTimeForSolution, remainingTime);
		ensembleToSelectFrom.stream().forEach(pl -> logger.info("\t{}: {} (f) / {} (t)", pl, solutionAnnotationCache.get(pl).getF(), getAnnotation(pl).getFTime()));
		ExecutorService pool = Executors.newFixedThreadPool(getNumberOfCPUs(), r -> {
			Thread t = new Thread(r);
			t.setName("final-evaluator-");
			return t;
		});
		Classifier selectedModel = null;
		final Semaphore sem = new Semaphore(0);
		final long timestampOfDeadline = getTimeOfStart() + getTimeout() - 2000;
		SolutionEvaluator evaluator = solutionEvaluatorFactory4Selection.getInstance();
		evaluator.setData(dataForSelection);
		AtomicDouble best = new AtomicDouble(Double.MAX_VALUE);
		final Classifier selectedClassifier[] = new Classifier[1]; //because we needd something final changable
		ensembleToSelectFrom.stream().forEach(c -> pool.submit(new Runnable() {
			public void run() {
				try {
					if (System.currentTimeMillis() + getAnnotation(c).getFTime() >= timestampOfDeadline) {
						logger.info("Not evaluating solutiom {} anymore, because timeout would be violated!", c);
						return;
					}
//					if (System.currentTimeMillis())
//					double believedScore = Double.parseDouble(String.valueOf(getAnnotation(c).getF()));
					double selectionScore = evaluator.getSolutionScore(c);
					synchronized (best) {
						if (best.get() > selectionScore) {
							selectedClassifier[0] = c;
							best.set(selectionScore);
						}
					}
				} catch (Throwable e) {
					e.printStackTrace();
				} finally {
					sem.release();
				}
			}
		}));
		// sem.acquire(ensembleToSelectFrom.size());

		// try {

		// /* determine average accuracy and possibly update currently best classifier */
		// DescriptiveStatistics stats = new DescriptiveStatistics();
		// IntStream.range(0, numberOfMCIterationsPerSolutionInSelectionPhase).logger.info("Awaiting termination of quality computations for solution {},...", pl);
		// // sem.acquire(numberOfMCIterationsPerSolutionInSelectionPhase);
		// if (stats.getN() > 0) {
		// double avgError = stats.getMean() / 100f;
		// double quartileScore = stats.getPercentile(75) / 100;
		// double score = (avgError + quartileScore) / 2f;
		// logger.info("Ready. Error of solution: {}/{} = {}. Believed value was: {}. Classifier is: {}.", avgError, quartileScore, score, believedScore / 100f, pl);
		// if (score < bestScore) {
		// bestScore = score;
		// selectedModel = pl;
		// logger.info("This is the new best one ...");
		// }
		// } else {
		// logger.warn("No evaluation results for solution {}", pl);
		// }
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// }

		try {
			System.out.print("Waiting for termination ");
			sem.acquire(ensembleToSelectFrom.size());
			System.out.println("done");
			long endOfPhase2 = System.currentTimeMillis();
			logger.info("Finished phase 2 within {}ms net. Total runtime was {}ms. Selected model is {}", endOfPhase2 - startOfPhase2, endOfPhase2 - getTimeOfStart(), selectedModel);
			logger.debug("Shutting down thread pool");
			pool.shutdownNow();
			pool.awaitTermination(5, TimeUnit.SECONDS);
			selectedModel = selectedClassifier[0];
			System.out.println(selectedModel);
			if (selectedModel == null && !ensembleToSelectFrom.isEmpty()) {
				selectedModel = ensembleToSelectFrom.iterator().next();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (pool.isShutdown())
			logger.debug("Thread pool down");
//		else
			// logger.warn("Thread pool still active!");
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

	public Map<String, OracleTaskResolver> getOracleResolvers() {
		return oracleResolvers;
	}

	public void setOracleResolvers(Map<String, OracleTaskResolver> oracleResolvers) {
		this.oracleResolvers = oracleResolvers;
	}
}
