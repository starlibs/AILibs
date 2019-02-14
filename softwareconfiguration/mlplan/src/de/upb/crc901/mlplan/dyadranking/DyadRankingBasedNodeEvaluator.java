package de.upb.crc901.mlplan.dyadranking;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.aeonbits.owner.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.crc901.mlplan.metamining.pipelinecharacterizing.WEKAPipelineCharacterizer;
import de.upb.crc901.mlplan.multiclass.wekamlplan.ClassifierFactory;
import de.upb.isys.linearalgebra.DenseDoubleVector;
import de.upb.isys.linearalgebra.Vector;
import hasco.core.Util;
import hasco.model.Component;
import hasco.model.ComponentInstance;
import hasco.serialization.ComponentLoader;
import jaicore.basic.IObjectEvaluator;
import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.sets.SetUtil.Pair;
import jaicore.ml.WekaUtil;
import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.dyadranking.Dyad;
import jaicore.ml.dyadranking.algorithm.PLNetDyadRanker;
import jaicore.ml.dyadranking.dataset.IDyadRankingInstance;
import jaicore.ml.dyadranking.dataset.SparseDyadRankingInstance;
import jaicore.ml.evaluation.evaluators.weka.FixedSplitClassifierEvaluator;
import jaicore.ml.metafeatures.LandmarkerCharacterizer;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.IGraphDependentNodeEvaluator;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.RandomCompletionBasedNodeEvaluator;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.RandomizedDepthFirstNodeEvaluator;
import jaicore.search.algorithms.standard.random.RandomSearch;
import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.model.other.SearchGraphPath;
import jaicore.search.model.probleminputs.GeneralEvaluatedTraversalTree;
import jaicore.search.model.travesaltree.Node;
import weka.core.Instances;

/**
 * 1 Nicer Node Evaluator.
 * 
 * This Node Evaluator stole literally all its code from the
 * {@link RandomCompletionBasedNodeEvaluator}, however, i removed all the crap
 * ;)
 * 
 * @param <T>
 *            the node type, typically it is {@link TFDNode}
 * @param <V>
 *            the type of the score, literally always Double...
 * @author Mirko!
 *
 */
public class DyadRankingBasedNodeEvaluator<T, V extends Comparable<V>>
		implements IGraphDependentNodeEvaluator<T, String, V> {

	private static final Logger logger = LoggerFactory.getLogger(DyadRankingBasedNodeEvaluator.class);

	/* Used to draw random completions for nodes that are not in the final state */
	private RandomSearch<T, String> randomPathCompleter;

	/* The evaluator that can be used to get the performance of the paths */
	private IObjectEvaluator<ComponentInstance, V> pipelineEvaluator;

	private Collection<Component> components;
	/*
	 * Specifies the amount of paths that are randomly completed for the computation
	 * of the f-value
	 */
	private final int randomlyCompletedPaths;

	private Instances evaluationDataset;

	private double[] datasetMetaFeatures;

	/*
	 * Specifies the amount of paths that will be evaluated after ranking the paths
	 */
	private final int evaluatedPaths;

	/* The Random instance used to randomly complete the paths */
	private final Random random;

	private PLNetDyadRanker dyadRanker = new PLNetDyadRanker();

	private WEKAPipelineCharacterizer characterizer;

	private final int landmarkerSampleSize;

	private final int[] landmarkers;

	private Instances[][] landmarkerSets;
	
	private ClassifierFactory classifierFactory;

	public void setClassifierFactory(ClassifierFactory classifierFactory) {
		this.classifierFactory = classifierFactory;
	}

	public DyadRankingBasedNodeEvaluator(ComponentLoader loader) {
		this(loader, ConfigFactory.create(DyadRankingBasedNodeEvaluatorConfig.class));
	}

	public DyadRankingBasedNodeEvaluator(ComponentLoader loader, DyadRankingBasedNodeEvaluatorConfig config) {
		this.components = loader.getComponents();
		this.random = new Random(config.getSeed());
		this.evaluatedPaths = config.getNumberOfEvaluations();
		this.randomlyCompletedPaths = config.getNumberOfRandomSamples();

		System.out.println("Initialized DyadRankingBasedNodeEvaluator with evalNum:" + evaluatedPaths
				+ " and completionNum:" + randomlyCompletedPaths);

		this.characterizer = new WEKAPipelineCharacterizer(loader.getParamConfigs());
		characterizer.buildFromFile();
		this.landmarkers = config.getLandmarkers();
		this.landmarkerSampleSize = config.getLandmarkerSampleSize();

		// load the dyadranker from the config
		try {
			this.dyadRanker.loadModelFromFile(Paths.get(config.getPlNetPath()).toString());
		} catch (IOException e) {
			logger.error("Could load model for plnet");
		}

	}

	@Override
	public V f(Node<T, ?> node) throws Exception {
		/* Let the random completer handle this use-case. */
		if (node.isGoal()) {
			return null;
		}
		/* Time measuring */
		Instant startOfEvaluation = Instant.now();

		/* Make sure that the completer knows the path until this node */

		if (!randomPathCompleter.knowsNode(node.getPoint())) {
			synchronized (randomPathCompleter) {
				randomPathCompleter.appendPathToNode(node.externalPath());
			}
		}
		// draw N paths at random
		List<T> randomPaths = getNRandomPaths(node);
		// order them according to dyad ranking
		List<ComponentInstance> allRankedPaths = getDyadRankedPaths(randomPaths);
		// get the top k paths
		List<ComponentInstance> topKRankedPaths = allRankedPaths.subList(0,
				Math.min(evaluatedPaths, allRankedPaths.size()));
		// evaluate the top k paths
		List<Pair<ComponentInstance, V>> allEvaluatedPaths = evaluateTopKPaths(topKRankedPaths);
		Duration evaluationTime = Duration.between(startOfEvaluation, Instant.now());
		logger.info("Evaluation of node {} took {}ms", node.getPoint(), evaluationTime.toMillis());
		return getBestSolution(allEvaluatedPaths);
	}

	private List<T> getNRandomPaths(Node<T, ?> node) throws InterruptedException, TimeoutException {
		List<T> completedPaths = new ArrayList<>();
		for (int currentPath = 0; currentPath < randomlyCompletedPaths; currentPath++) {
			/*
			 * complete the current path by the dfs-solution; we assume that this goes in
			 * almost constant time
			 */
			List<T> pathCompletion = null;
			List<T> completedPath = null;
			synchronized (randomPathCompleter) {

				if (randomPathCompleter.isCanceled()) {
					logger.info("Completer has been canceled (perhaps due a cancel on the evaluator). Canceling RDFS");
					break;
				}
				completedPath = new ArrayList<>(node.externalPath());

				logger.info("Starting search for next solution ...");

				SearchGraphPath<T, String> solutionPathFromN = null;
				try {
					solutionPathFromN = randomPathCompleter.nextSolutionUnderNode(node.getPoint());
				} catch (AlgorithmExecutionCanceledException e) {
					logger.info("Completer has been canceled. Returning control.");
					break;
				}
				if (solutionPathFromN == null) {
					logger.info("No completion was found for path {}.", node.externalPath());
					break;
				}
				logger.info("Found solution {}", solutionPathFromN);
				pathCompletion = new ArrayList<>(solutionPathFromN.getNodes());
				pathCompletion.remove(0);
				completedPath.addAll(pathCompletion);
			}
			completedPaths.add(completedPath.get(completedPath.size() - 1));
		}
		return completedPaths;
	}

	private List<ComponentInstance> getDyadRankedPaths(List<T> randomPaths) {
		Map<Vector, ComponentInstance> pipelineToCharacterization = new HashMap<>();
		// extract componentInstances that we can rank
		for (T randomPath : randomPaths) {
			TFDNode goalNode = (TFDNode) randomPath;
			ComponentInstance cI = Util.getSolutionCompositionFromState(components, goalNode.getState(), true);
			// fill the y with landmarkers
			Vector y_prime = evaluateLandmarkersForAlgorithm(cI);
			pipelineToCharacterization.put(y_prime, cI);
		}
		// invoke dyad ranker
		return rankRandomPipelines(pipelineToCharacterization);
	}

	/**
	 * Calculates the landmarkers for the given Pipeline
	 * 
	 * @param y
	 *            the pipeline characterization
	 * @param cI
	 * @return
	 */
	private Vector evaluateLandmarkersForAlgorithm(ComponentInstance cI) {
		double[] y = characterizer.characterize(cI);
		int sizeOfYPrime = characterizer.getLengthOfCharacrization() + landmarkers.length;
		double[] y_prime = new double[sizeOfYPrime];
		System.arraycopy(y, 0, y_prime, 0, y.length);
		for (int i = 0; i < landmarkers.length; i++) {
			Instances[] subsets = landmarkerSets[i];
			double score = 0d;
			for (Instances train : subsets) {
				FixedSplitClassifierEvaluator evaluator = new FixedSplitClassifierEvaluator(train, evaluationDataset);
				try {
					score += evaluator.evaluate(classifierFactory.getComponentInstantiation(cI));
				} catch (Exception e) {
					logger.error("Couldn't get classifier for {}", cI);
				}
			}
			//average the score
			if (score != 0) {
				score = score / (double) subsets.length;
			}
			y_prime[y.length + i] = score;
		}
		return new DenseDoubleVector(y_prime);
	}

	private List<ComponentInstance> rankRandomPipelines(Map<Vector, ComponentInstance> randomPipelines) {
		List<Vector> alternatives = new ArrayList<>(randomPipelines.keySet());

		/* Use a sparse instance for ranking */
		SparseDyadRankingInstance toRank = new SparseDyadRankingInstance(new DenseDoubleVector(datasetMetaFeatures),
				alternatives);
		IDyadRankingInstance rankedInstance;
		try {
			rankedInstance = dyadRanker.predict(toRank);
			List<ComponentInstance> rankedPipelines = new ArrayList<>();
			for (Dyad dyad : rankedInstance) {
				rankedPipelines.add(randomPipelines.get(dyad.getAlternative()));
			}
			return rankedPipelines;
		} catch (PredictionException e) {
			logger.error("Couldn't rank charaterized pipelines.", e);
			throw new RuntimeException(e);
		}
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
	private List<Pair<ComponentInstance, V>> evaluateTopKPaths(List<ComponentInstance> topKRankedPaths)
			throws InterruptedException, ExecutionException, TimeoutException {

		// we use the executionservice mechanism to make sure we wait at most 5 seconds
		// for an evaluation
		Executor executor = Executors.newFixedThreadPool(1);
		CompletionService<Pair<ComponentInstance, V>> completionService = new ExecutorCompletionService<>(executor);

		List<Pair<ComponentInstance, V>> evaluatedSolutions = new ArrayList<>();
		// schedule the tasks
		for (ComponentInstance node : topKRankedPaths) {
			try {
				completionService.submit(() -> {
					V score = pipelineEvaluator.evaluate(node);
					return new Pair<>(node, score);
				});
			} catch (Exception e) {
				logger.error("Couldn't evaluate {}", node);
			}
		}
		// collect the results but not wait longer than 5 seconds for a result to appear
		for (int i = 0; i < topKRankedPaths.size(); i++) {
			Future<Pair<ComponentInstance, V>> evaluatedPipe = completionService.take();
			try {
				Pair<ComponentInstance, V> solution = evaluatedPipe.get(5, TimeUnit.SECONDS);
				evaluatedSolutions.add(solution);
			} catch (Exception e) {
				evaluatedPipe.cancel(true);
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
	private V getBestSolution(List<Pair<ComponentInstance, V>> allEvaluatedPaths) {
		return allEvaluatedPaths.stream().map(Pair::getY).min(V::compareTo).orElseThrow(NoSuchElementException::new);
	}

	@Override
	public void setGenerator(GraphGenerator<T, String> generator) {
		INodeEvaluator<T, Double> nodeEvaluator = new RandomizedDepthFirstNodeEvaluator<>(this.random);
		GeneralEvaluatedTraversalTree<T, String, Double> completionProblem = new GeneralEvaluatedTraversalTree<>(
				generator, nodeEvaluator);
		randomPathCompleter = new RandomSearch<>(completionProblem, null, this.random);
	}

	public void setDataset(Instances dataset) {
		// first we split the dataset into train & testdata
		List<Instances> split = WekaUtil.getStratifiedSplit(dataset, 42l, 0.8d);
		Instances trainData = split.get(0);
		evaluationDataset = split.get(1);
		Map<String, Double> metaFeatures;
		try {
			metaFeatures = new LandmarkerCharacterizer().characterize(dataset);
			datasetMetaFeatures = metaFeatures.entrySet().stream().mapToDouble(Map.Entry::getValue).toArray();
		} catch (Exception e) {
			logger.error("Failed to characterize the dataset", e);
		}

		landmarkerSets = new Instances[landmarkers.length][landmarkerSampleSize];
		Random random = new Random();
		// draw instances used for the landmarkers
		for (int i = 0; i < landmarkers.length; i++) {
			int landmarker = landmarkers[i];
			for (int j = 0; j < landmarkerSampleSize; j++) {
				Instances instances = new Instances(dataset, landmarker);
				for (int k = 0; k < landmarker; k++) {
					int randomEntry = random.nextInt(trainData.size());
					instances.add(trainData.get(randomEntry));
				}
				landmarkerSets[i][j] = instances;
			}
		}
	}

	public void setPipelineEvaluator(IObjectEvaluator<ComponentInstance, V> wrappedSearchBenchmark) {
		this.pipelineEvaluator = wrappedSearchBenchmark;
	}
}
