package ai.libs.mlplan.metamining.dyadranking;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.aeonbits.owner.ConfigFactory;
import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.DualHashBidiMap;
import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.IPathGoalTester;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPotentiallyGraphDependentPathEvaluator;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPotentiallySolutionReportingPathEvaluator;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.PathEvaluationException;
import org.api4.java.ai.ml.classification.IClassifier;
import org.api4.java.ai.ml.core.dataset.splitter.SplitFailedException;
import org.api4.java.ai.ml.core.exception.PredictionException;
import org.api4.java.ai.ml.ranking.IRanking;
import org.api4.java.ai.ml.ranking.dyad.dataset.IDyad;
import org.api4.java.ai.ml.ranking.dyad.dataset.IDyadRankingInstance;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.common.attributedobjects.IObjectEvaluator;
import org.api4.java.common.math.IVector;
import org.api4.java.datastructure.graph.ILabeledPath;
import org.api4.java.datastructure.graph.implicit.IGraphGenerator;
import org.openml.webapplication.fantail.dc.LandmarkerCharacterizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.hasco.core.HASCOUtil;
import ai.libs.jaicore.basic.algorithm.AlgorithmInitializedEvent;
import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.components.api.IComponent;
import ai.libs.jaicore.components.api.IComponentRepository;
import ai.libs.jaicore.components.model.ComponentInstance;
import ai.libs.jaicore.math.linearalgebra.DenseDoubleVector;
import ai.libs.jaicore.ml.classification.loss.dataset.EClassificationPerformanceMeasure;
import ai.libs.jaicore.ml.core.evaluation.evaluator.FixedSplitClassifierEvaluator;
import ai.libs.jaicore.ml.ranking.dyad.dataset.DyadRankingDataset;
import ai.libs.jaicore.ml.ranking.dyad.dataset.SparseDyadRankingInstance;
import ai.libs.jaicore.ml.ranking.dyad.learner.algorithm.PLNetDyadRanker;
import ai.libs.jaicore.ml.ranking.dyad.learner.util.DyadMinMaxScaler;
import ai.libs.jaicore.ml.weka.WekaUtil;
import ai.libs.jaicore.ml.weka.dataset.WekaInstances;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.events.EvaluatedSearchSolutionCandidateFoundEvent;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.events.FValueEvent;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.nodeevaluation.RandomCompletionBasedNodeEvaluator;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.nodeevaluation.RandomizedDepthFirstNodeEvaluator;
import ai.libs.jaicore.search.algorithms.standard.gbf.SolutionEventBus;
import ai.libs.jaicore.search.algorithms.standard.random.RandomSearch;
import ai.libs.jaicore.search.model.other.EvaluatedSearchGraphPath;
import ai.libs.jaicore.search.model.other.SearchGraphPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;
import ai.libs.mlplan.core.ILearnerFactory;
import ai.libs.mlplan.metamining.pipelinecharacterizing.ComponentInstanceVectorFeatureGenerator;
import ai.libs.mlplan.metamining.pipelinecharacterizing.IPipelineCharacterizer;
import weka.core.Instances;

/**
 * This NodeEvaluator can calculate the f-Value for nodes using dyad ranking.
 * Thereby, a huge amount of random completion will be drawn in a node, then
 * these pipelines will ranked using dyad ranking and finally the top k
 * pipelines will be evaluated, using the best observed score as the f-Value of
 * the node.
 *
 * @param <T>
 *            the node type, typically it is {@link TFDNode}
 * @param <V>
 *            the type of the score
 * @author Mirko Juergens
 *
 */
public class DyadRankingBasedNodeEvaluator<T, A, V extends Comparable<V>> implements IPotentiallyGraphDependentPathEvaluator<T, A, V>, IPotentiallySolutionReportingPathEvaluator<T, A, V> {

	private static final Logger logger = LoggerFactory.getLogger(DyadRankingBasedNodeEvaluator.class);

	/* Key is a path (hence, List<T>) value is a ComponentInstance */
	private BidiMap pathToPipelines = new DualHashBidiMap();

	/* Used to draw random completions for nodes that are not in the final state */
	private RandomSearch<T, A> randomPathCompleter;

	/* The evaluator that can be used to get the performance of the paths */
	private IObjectEvaluator<ComponentInstance, V> pipelineEvaluator;

	/* Specifies the components of this MLPlan run. */
	private Collection<IComponent> components;

	/*
	 * Specifies the amount of paths that are randomly completed for the computation
	 * of the f-value
	 */
	private final int randomlyCompletedPaths;

	/* The dataset of this MLPlan run. */
	private Instances evaluationDataset;

	/*
	 * X in the paper, these are usually derived using landmarking algorithms on the
	 * dataset
	 */
	private double[] datasetMetaFeatures;

	/*
	 * Specifies the amount of paths that will be evaluated after ranking the paths
	 */
	private final int evaluatedPaths;

	/* The Random instance used to randomly complete the paths */
	private final Random random;

	/* The ranker to use for dyad ranking */
	private PLNetDyadRanker dyadRanker = new PLNetDyadRanker();

	/* The characterizer to use to derive meta features for pipelines */
	private IPipelineCharacterizer characterizer;

	/* Only used if useLandmarkers is set to true */
	/*
	 * Defines how many evaluations for each of the landmarkers are performed, to
	 * reduce variance
	 */
	private final int landmarkerSampleSize;

	/* Only used if useLandmarkers is set to true */
	/* Defines the size of the different landmarkers */
	private final int[] landmarkers;

	/* Only used if useLandmarkers is set to true */
	/*
	 * The concete lanmarker instances, this array has dimension landmakers.size
	 * \cdot landmarkerSampleSize
	 */
	private Instances[][] landmarkerSets;

	/* Only used if useLandmarkers is set to true */
	/*
	 * Used to create landmarker values for pipelines where no such landmarker has
	 * yet been evaluated.
	 */
	private ILearnerFactory<IClassifier> classifierFactory;

	/*
	 * Defines if a landmarking based approach is used for defining the meta
	 * features of the algorithm.
	 */
	private boolean useLandmarkers;

	/*
	 * Used to derive the time until a certain solution has been found, useful for
	 * evaluations
	 */
	private Instant firstEvaluation = null;

	private SolutionEventBus<T> eventBus;

	private IGraphGenerator<T, A> graphGenerator;
	private IPathGoalTester<T, A> goalTester;

	private DyadMinMaxScaler scaler = null;

	public void setClassifierFactory(final ILearnerFactory<IClassifier> classifierFactory) {
		this.classifierFactory = classifierFactory;
	}

	public DyadRankingBasedNodeEvaluator(final IComponentRepository repository) {
		this(repository, ConfigFactory.create(DyadRankingBasedNodeEvaluatorConfig.class));
	}

	public DyadRankingBasedNodeEvaluator(final IComponentRepository repository, final DyadRankingBasedNodeEvaluatorConfig config) {
		this.eventBus = new SolutionEventBus<>();
		this.components = repository;
		this.random = new Random(config.getSeed());
		this.evaluatedPaths = config.getNumberOfEvaluations();
		this.randomlyCompletedPaths = config.getNumberOfRandomSamples();

		logger.debug("Initialized DyadRankingBasedNodeEvaluator with evalNum: {} and completionNum: {}", this.randomlyCompletedPaths, this.evaluatedPaths);

		this.characterizer = new ComponentInstanceVectorFeatureGenerator(repository);

		this.landmarkers = config.getLandmarkers();
		this.landmarkerSampleSize = config.getLandmarkerSampleSize();
		this.useLandmarkers = config.useLandmarkers();

		String scalerPath = config.scalerPath();

		try {
			this.dyadRanker.loadModelFromFile(Paths.get(config.getPlNetPath()).toString());
		} catch (IOException e) {
			logger.error("Could not load model for plnet in {}", Paths.get(config.getPlNetPath()));
		}

		// load the dyadranker from the config
		try (ObjectInputStream oin = new ObjectInputStream(new FileInputStream(Paths.get(scalerPath).toFile()));) {
			this.scaler = (DyadMinMaxScaler) oin.readObject();
		} catch (IOException e) {
			logger.error("Could not load sclader for plnet in {}", Paths.get(config.scalerPath()));
		} catch (ClassNotFoundException e) {
			logger.error("Could not read scaler.", e);
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public V evaluate(final ILabeledPath<T, A> path) throws InterruptedException, PathEvaluationException {
		if (this.firstEvaluation == null) {
			this.firstEvaluation = Instant.now();
		}

		/* Let the random completer handle this use-case. */
		if (this.randomPathCompleter.getInput().getGoalTester().isGoal(path)) {
			return null;
		}

		/* Time measuring */
		Instant startOfEvaluation = Instant.now();

		/* Make sure that the completer knows the path until this node */

		if (!this.randomPathCompleter.knowsNode(path.getHead())) {
			synchronized (this.randomPathCompleter) {
				this.randomPathCompleter.appendPathToNode(path);
			}
		}
		// draw N paths at random
		List<List<T>> randomPaths = null;
		try {
			randomPaths = this.getNRandomPaths(path);
		} catch (InterruptedException | TimeoutException e) {
			logger.error("Interrupted in path completion!");
			Thread.currentThread().interrupt();
			Thread.interrupted();
			throw new InterruptedException();
		}
		// order them according to dyad ranking
		List<ComponentInstance> allRankedPaths;
		try {
			allRankedPaths = this.getDyadRankedPaths(randomPaths);
		} catch (PredictionException e1) {
			throw new PathEvaluationException("Could not rank nodes", e1);
		}

		// random search failed to find anything here
		if (allRankedPaths.isEmpty()) {
			return (V) ((Double) 9000.0d);
		}
		// get the top k paths
		List<ComponentInstance> topKRankedPaths = allRankedPaths.subList(0, Math.min(this.evaluatedPaths, allRankedPaths.size()));
		// evaluate the top k paths
		List<Pair<ComponentInstance, V>> allEvaluatedPaths = null;
		try {
			allEvaluatedPaths = this.evaluateTopKPaths(topKRankedPaths);
		} catch (InterruptedException | TimeoutException e) {
			logger.error("Interrupted while predicitng next best solution");
			Thread.currentThread().interrupt();
			Thread.interrupted();
			throw new InterruptedException();
		} catch (ExecutionException e2) {
			logger.error("Couldn't evaluate solution candidates- Returning null as FValue!.");
			return null;
		}
		Duration evaluationTime = Duration.between(startOfEvaluation, Instant.now());
		logger.info("Evaluation took {}ms", evaluationTime.toMillis());
		V bestSoultion = this.getBestSolution(allEvaluatedPaths);
		logger.info("Best solution is {}, {}", bestSoultion, allEvaluatedPaths.stream().map(Pair::getY).collect(Collectors.toList()));
		if (bestSoultion == null) {
			return (V) ((Double) 9000.0d);
		}
		this.eventBus.post(new FValueEvent<V>(null, bestSoultion, evaluationTime.toMillis()));
		return bestSoultion;
	}

	/**
	 * Stolen from {@link RandomCompletionBasedNodeEvaluator}, maybe should refactor
	 * this into a pattern.
	 *
	 * @param node
	 *            the starting node
	 * @return the randomPaths, described by their final node
	 * @throws InterruptedException
	 * @throws TimeoutException
	 */
	private List<List<T>> getNRandomPaths(final ILabeledPath<T, A> node) throws InterruptedException, TimeoutException {
		List<List<T>> completedPaths = new ArrayList<>();
		for (int currentPath = 0; currentPath < this.randomlyCompletedPaths; currentPath++) {
			/*
			 * complete the current path by the dfs-solution; we assume that this goes in
			 * almost constant time
			 */
			List<T> pathCompletion = null;
			List<T> completedPath = null;
			synchronized (this.randomPathCompleter) {

				if (this.randomPathCompleter.isCanceled()) {
					logger.info("Completer has been canceled (perhaps due a cancel on the evaluator). Canceling RDFS");
					break;
				}
				completedPath = new ArrayList<>(node.getNodes());

				SearchGraphPath<T, A> solutionPathFromN = null;
				try {
					solutionPathFromN = this.randomPathCompleter.nextSolutionUnderSubPath(node);
				} catch (AlgorithmExecutionCanceledException e) {
					logger.info("Completer has been canceled. Returning control.");
					break;
				}
				if (solutionPathFromN == null) {
					logger.info("No completion was found for path {}.", node.getNodes());
					break;
				}

				pathCompletion = new ArrayList<>(solutionPathFromN.getNodes());
				pathCompletion.remove(0);
				completedPath.addAll(pathCompletion);
			}
			completedPaths.add(completedPath);
		}
		logger.info("Returning {} paths", completedPaths.size());
		return completedPaths;
	}

	private List<ComponentInstance> getDyadRankedPaths(final List<List<T>> randomPaths) throws PredictionException, InterruptedException {
		Map<IVector, ComponentInstance> pipelineToCharacterization = new HashMap<>();
		// extract componentInstances that we can rank
		for (List<T> randomPath : randomPaths) {
			TFDNode goalNode = (TFDNode) randomPath.get(randomPath.size() - 1);
			ComponentInstance cI = HASCOUtil.getSolutionCompositionFromState(this.components, goalNode.getState(), true);
			this.pathToPipelines.put(randomPath, cI);
			// fill the y with landmarkers
			if (this.useLandmarkers) {
				IVector yPrime = this.evaluateLandmarkersForAlgorithm(cI);
				pipelineToCharacterization.put(yPrime, cI);
			} else {
				IVector y = new DenseDoubleVector(this.characterizer.characterize(cI));
				if (this.scaler != null) {
					List<IDyadRankingInstance> asList = Arrays.asList(new SparseDyadRankingInstance(new DenseDoubleVector(this.datasetMetaFeatures), Arrays.asList(y)));
					DyadRankingDataset dataset = new DyadRankingDataset(asList);
					this.scaler.transformAlternatives(dataset);
				}
				pipelineToCharacterization.put(y, cI);
			}
		}
		// invoke dyad ranker
		return this.rankRandomPipelines(pipelineToCharacterization);
	}

	/**
	 * Calculates the landmarkers for the given Pipeline, if the value
	 * {@link DyadRankingBasedNodeEvaluator#useLandmarkers} is set to
	 * <code>true</code>.
	 *
	 * @param y
	 *            the pipeline characterization
	 * @param cI
	 *            the pipeline to characterize
	 * @return the meta features of the pipeline with appended landmarking features
	 */
	private IVector evaluateLandmarkersForAlgorithm(final ComponentInstance cI) {
		double[] y = this.characterizer.characterize(cI);

		int sizeOfYPrime = this.characterizer.getLengthOfCharacterization() + this.landmarkers.length;
		double[] yPrime = new double[sizeOfYPrime];
		System.arraycopy(y, 0, yPrime, 0, y.length);
		for (int i = 0; i < this.landmarkers.length; i++) {
			Instances[] subsets = this.landmarkerSets[i];
			double score = 0d;
			for (Instances train : subsets) {
				FixedSplitClassifierEvaluator evaluator = new FixedSplitClassifierEvaluator(new WekaInstances(train), new WekaInstances(this.evaluationDataset), EClassificationPerformanceMeasure.ERRORRATE);
				try {
					score += evaluator.evaluate(this.classifierFactory.getComponentInstantiation(cI));
				} catch (Exception e) {
					logger.error("Couldn't get classifier for {}", cI);
				}
			}
			// average the score
			if (score != 0) {
				score = score / subsets.length;
			}
			yPrime[y.length + i] = score;
		}
		return new DenseDoubleVector(yPrime);
	}

	private List<ComponentInstance> rankRandomPipelines(final Map<IVector, ComponentInstance> randomPipelines) throws PredictionException, InterruptedException {
		List<IVector> alternatives = new ArrayList<>(randomPipelines.keySet());

		/* Use a sparse instance for ranking */
		SparseDyadRankingInstance toRank = new SparseDyadRankingInstance(new DenseDoubleVector(this.datasetMetaFeatures), alternatives);
		IRanking<IDyad> rankedInstance;
		rankedInstance = this.dyadRanker.predict(toRank);
		List<ComponentInstance> rankedPipelines = new ArrayList<>();
		for (IDyad dyad : rankedInstance) {
			rankedPipelines.add(randomPipelines.get(dyad.getAlternative()));
		}
		return rankedPipelines;
	}

	/**
	 * Invokes the solution-evaluator to get the performances of the best k paths.
	 *
	 * @param topKRankedPaths
	 *            the paths, after ranking
	 * @return the list of scores.
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	private List<Pair<ComponentInstance, V>> evaluateTopKPaths(final List<ComponentInstance> topKRankedPaths) throws InterruptedException, ExecutionException, TimeoutException {

		// we use the executionservice mechanism to make sure we wait at most 5 seconds
		// for an evaluation
		Executor executor = Executors.newFixedThreadPool(1);
		CompletionService<Pair<ComponentInstance, V>> completionService = new ExecutorCompletionService<>(executor);

		List<Pair<ComponentInstance, V>> evaluatedSolutions = new ArrayList<>();
		// schedule the tasks
		for (ComponentInstance node : topKRankedPaths) {

			completionService.submit(() -> {
				try {
					Instant startTime = Instant.now();
					V score = this.pipelineEvaluator.evaluate(node);
					Duration evalTime = Duration.between(startTime, Instant.now());
					this.postSolution(node, evalTime.toMillis(), score);
					return new Pair<>(node, score);
				} catch (Exception e) {
					logger.error("Couldn't evaluate {}", node);
					return null;
				}
			});

		}
		// collect the results but not wait longer than 5 seconds for a result to appear
		for (int i = 0; i < topKRankedPaths.size(); i++) {
			logger.info("Got {} solutions. Waiting for iteration {} of max iterations {}", evaluatedSolutions.size(), i + 1, topKRankedPaths.size());
			Future<Pair<ComponentInstance, V>> evaluatedPipe = completionService.poll(20, TimeUnit.SECONDS);
			if (evaluatedPipe == null) {
				logger.info("Didn't receive any futures (expected {} futures)", topKRankedPaths.size());
				continue;
			}
			try {
				Pair<ComponentInstance, V> solution = evaluatedPipe.get(20, TimeUnit.SECONDS);
				if (solution != null) {
					logger.info("Evaluation was successful. Adding {} to solutions", solution.getY());
					evaluatedSolutions.add(solution);
				} else {
					logger.info("No solution was found while waiting up to 20s.");
					evaluatedPipe.cancel(true);
				}

			} catch (Exception e) {
				logger.info("Got exception while evaluating {}", e.getMessage());
			}

		}
		return evaluatedSolutions;
	}

	/**
	 * Aggregates a list of found solutions to a f-value. Currently, this is the
	 * minimal value found
	 *
	 * @param allFoundSolutions
	 *            all solutions
	 * @return
	 */
	private V getBestSolution(final List<Pair<ComponentInstance, V>> allEvaluatedPaths) {
		return allEvaluatedPaths.stream().map(Pair::getY).min(V::compareTo).orElse(null);
	}

	@Override
	public void setGenerator(final IGraphGenerator<T, A> generator, final IPathGoalTester<T, A> goalTester) {
		this.graphGenerator = generator;
		this.goalTester = goalTester;
		this.initializeRandomSearch();
	}

	/**
	 * Can be used to reinitialize the random-search at every call of the f-Value
	 * computation.
	 *
	 * @param generator
	 */
	private void initializeRandomSearch() {
		IPathEvaluator<T, A, Double> nodeEvaluator = new RandomizedDepthFirstNodeEvaluator<>(this.random);
		GraphSearchWithSubpathEvaluationsInput<T, A, Double> completionProblem = new GraphSearchWithSubpathEvaluationsInput<>(this.graphGenerator, this.goalTester, nodeEvaluator);
		this.randomPathCompleter = new RandomSearch<>(completionProblem, null, this.random);
		while (!(this.randomPathCompleter.next() instanceof AlgorithmInitializedEvent)) {

			/* do not do anything, just skip until InitializationEvent is observed */
		}
	}

	/**
	 * Sets the data set in the node evaluator and calculates its meta features.
	 *
	 * @param dataset
	 */
	public void setDataset(final Instances dataset) {
		try {
			if (this.useLandmarkers) {
				List<Instances> split;
				split = WekaUtil.getStratifiedSplit(dataset, 42l, 0.8);
				Instances trainData = split.get(0);
				this.evaluationDataset = split.get(1);
				Map<String, Double> metaFeatures;
				metaFeatures = new LandmarkerCharacterizer().characterize(dataset);
				this.datasetMetaFeatures = metaFeatures.entrySet().stream().mapToDouble(Map.Entry::getValue).toArray();
				this.setUpLandmarkingDatasets(dataset, trainData);
			} else {
				Map<String, Double> metaFeatures = new LandmarkerCharacterizer().characterize(dataset);
				this.datasetMetaFeatures = metaFeatures.entrySet().stream().mapToDouble(Map.Entry::getValue).toArray();
			}
		} catch (SplitFailedException e) {
			throw new IllegalArgumentException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} catch (Exception e) {
			logger.error("Failed to characterize the dataset", e);
		}
	}

	/**
	 * Sets up the training data for the landmarkers that should be used.
	 */
	private void setUpLandmarkingDatasets(final Instances dataset, final Instances trainData) {
		this.landmarkerSets = new Instances[this.landmarkers.length][this.landmarkerSampleSize];
		// draw instances used for the landmarkers
		for (int i = 0; i < this.landmarkers.length; i++) {
			int landmarker = this.landmarkers[i];
			for (int j = 0; j < this.landmarkerSampleSize; j++) {
				Instances instances = new Instances(dataset, landmarker);
				for (int k = 0; k < landmarker; k++) {
					int randomEntry = this.random.nextInt(trainData.size());
					instances.add(trainData.get(randomEntry));
				}
				this.landmarkerSets[i][j] = instances;
			}
		}
	}

	/**
	 * Posts the solution to the EventBus of the search.
	 *
	 * @param solution
	 *            evaluated pipeline
	 * @param time
	 *            time it took
	 * @param score
	 *            the observed score
	 */
	protected void postSolution(final ComponentInstance solution, final long time, final V score) {
		try {
			@SuppressWarnings("unchecked")
			List<T> pathToSolution = (List<T>) this.pathToPipelines.getKey(solution);
			EvaluatedSearchGraphPath<T, ?, V> solutionObject = new EvaluatedSearchGraphPath<>(pathToSolution, null, score);
			solutionObject.setAnnotation("fTime", time);
			solutionObject.setAnnotation("timeToSolution", Duration.between(this.firstEvaluation, Instant.now()).toMillis());
			solutionObject.setAnnotation("nodesEvaluatedToSolution", this.randomlyCompletedPaths);
			logger.debug("Posting solution {}", solutionObject);
			this.eventBus.post(new EvaluatedSearchSolutionCandidateFoundEvent<>(null, solutionObject));
		} catch (Exception e) {
			logger.error("Couldn't post solution to event bus.", e);
		}
	}

	public void setPipelineEvaluator(final IObjectEvaluator<ComponentInstance, V> wrappedSearchBenchmark) {
		this.pipelineEvaluator = wrappedSearchBenchmark;
	}

	@Override
	public boolean requiresGraphGenerator() {
		return true;
	}

	@Override
	public void registerSolutionListener(final Object listener) {
		this.eventBus.register(listener);

	}

	@Override
	public boolean reportsSolutions() {
		return true;
	}

}
