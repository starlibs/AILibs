package de.upb.crc901.mlplan.dyadranking;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
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
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.crc901.mlplan.metamining.pipelinecharacterizing.WEKAPipelineCharacterizer;
import de.upb.isys.linearalgebra.DenseDoubleVector;
import de.upb.isys.linearalgebra.Vector;
import hasco.core.Util;
import hasco.model.Component;
import hasco.model.ComponentInstance;
import hasco.serialization.ComponentLoader;
import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.sets.SetUtil.Pair;
import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.dyadranking.Dyad;
import jaicore.ml.dyadranking.algorithm.ADyadRanker;
import jaicore.ml.dyadranking.algorithm.PLNetDyadRanker;
import jaicore.ml.dyadranking.dataset.IDyadRankingInstance;
import jaicore.ml.dyadranking.dataset.SparseDyadRankingInstance;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.IGraphDependentNodeEvaluator;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.RandomCompletionBasedNodeEvaluator;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.RandomizedDepthFirstNodeEvaluator;
import jaicore.search.algorithms.standard.random.RandomSearch;
import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.core.interfaces.ISolutionEvaluator;
import jaicore.search.model.other.SearchGraphPath;
import jaicore.search.model.probleminputs.GeneralEvaluatedTraversalTree;
import jaicore.search.model.travesaltree.Node;

/**
 * 1 Nicer Node Evaluator.
 * 
 * This Node Evaluator stole literally all its code from the
 * {@link RandomCompletionBasedNodeEvaluator}, however, i removed all the crap
 * ;)
 * 
 * @param <T>
 *            the node type, typically it is {@link Node}<TFDNode, String>
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
	private final ISolutionEvaluator<T, V> solutionEvaluator;

	/* Stores the computed f-values s.t. they can be cached */
	private final Map<T, V> fValueCache = new HashMap<>();

	/* MutEx Semaphore for writing in the cache */
	private final Semaphore fValueCacheWriteSem = new Semaphore(1);

	private Collection<Component> components;
	/*
	 * Specifies the amount of paths that are randomly completed for the computation
	 * of the f-value
	 */
	private final int randomlyCompletedPaths;

	private final double[] datasetMetaFeatures;

	/*
	 * Specifies the amount of paths that will be evaluated after ranking the paths
	 */
	private final int evaluatedPaths;

	private final Random random;

	private final Predicate<T> priorityPredicateForRDFS;

	private ADyadRanker dyadRanker = new PLNetDyadRanker();

	private WEKAPipelineCharacterizer characterizer;

	public DyadRankingBasedNodeEvaluator(Collection<Component> components, ISolutionEvaluator<T, V> solutionEvaluator,
			int randomlyCompletedPaths, int evaluatedPaths, Random random, Predicate<T> priorityPredicateForRDFS,
			double[] datasetMetaFeatures) {
		super();
		this.components = components;
		this.solutionEvaluator = solutionEvaluator;
		this.randomlyCompletedPaths = randomlyCompletedPaths;
		this.evaluatedPaths = evaluatedPaths;
		this.random = random;
		this.priorityPredicateForRDFS = priorityPredicateForRDFS;
		this.datasetMetaFeatures = datasetMetaFeatures;

		// pretrain dyadRanker
		File jsonFile;
		try {
			jsonFile = Paths.get(getClass().getClassLoader()
					.getResource(Paths.get("automl", "searchmodels", "weka", "weka-all-autoweka.json").toString())
					.toURI()).toFile();

			ComponentLoader loader = new ComponentLoader(jsonFile);
			this.characterizer = new WEKAPipelineCharacterizer(loader.getParamConfigs());
			characterizer.buildFromFile();
		} catch (URISyntaxException | IOException e) {
			logger.error("Couldn't load weka models", e);
			this.characterizer = null;

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
		List<List<T>> randomPaths = getNRandomPaths(node);
		// order them according to dyad ranking
		List<List<T>> allRankedPaths = getDyadRankedPaths(randomPaths, node);
		// get the top k paths
		List<List<T>> topKRankedPaths = allRankedPaths.subList(0, evaluatedPaths);
		// evaluate the top k paths
		List<Pair<List<T>, V>> allEvaluatedPaths = evaluateTopKPaths(topKRankedPaths);
		Duration evaluationTime = Duration.between(startOfEvaluation, Instant.now());
		logger.debug("Evaluation of node {} took {}ms", node.getPoint(), evaluationTime.toMillis());

		return getBestSolution(allEvaluatedPaths);
	}

	private List<List<T>> getDyadRankedPaths(List<List<T>> randomPaths, Node<T, ?> node) {
		List<Pair<List<T>, ComponentInstance>> randomPipelines = new ArrayList<>();
		// extract componentInstances that we can rank
		for (List<T> randomPath : randomPaths) {
			TFDNode goalNode = (TFDNode) randomPath.get(randomPath.size() - 1);
			ComponentInstance cI = Util.getSolutionCompositionFromState(components, goalNode.getState(), true);
			randomPipelines.add(new Pair<>(randomPath, cI));
		}
		// invoke dyad ranker
		return rankRandomPipelines(randomPipelines);
	}

	private List<List<T>> rankRandomPipelines(List<Pair<List<T>, ComponentInstance>> randomPipelines) {
		Map<Vector, List<T>> dyadToPath = new HashMap<>();
		List<Vector> alternatives = new ArrayList<>();
		for (Pair<List<T>, ComponentInstance> ci : randomPipelines) {
			Vector pipelineCharacterization = new DenseDoubleVector(characterizer.characterize(ci.getY()));
			dyadToPath.put(pipelineCharacterization, ci.getX());
			alternatives.add(pipelineCharacterization);
		}
		SparseDyadRankingInstance toRank = new SparseDyadRankingInstance(new DenseDoubleVector(datasetMetaFeatures),
				alternatives);
		IDyadRankingInstance rankedInstance;
		try {
			rankedInstance = dyadRanker.predict(toRank);
			List<List<T>> rankedPipelines = new ArrayList<>();
			for (Dyad dyad : rankedInstance) {
				rankedPipelines.add(dyadToPath.get(dyad.getAlternative()));
			}
			return rankedPipelines;
		} catch (PredictionException e) {
			throw new RuntimeException(e);
		}
	}

	private List<List<T>> getNRandomPaths(Node<T, ?> node) throws InterruptedException, TimeoutException {
		List<List<T>> completedPaths = new ArrayList<>();
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
			completedPaths.add(completedPath);
		}
		return completedPaths;
	}

	/**
	 * Invokes the solution-evaluator to get the performances of the best k paths.
	 * 
	 * @param topKRankedPaths
	 *            the paths, after ranking
	 * @return the list of scores.
	 */
	private List<Pair<List<T>, V>> evaluateTopKPaths(List<List<T>> topKRankedPaths) {
		List<Pair<List<T>, V>> evaluatedSolutions = new ArrayList<>();
		for (List<T> node : topKRankedPaths) {
			try {
				V score = solutionEvaluator.evaluateSolution(node);
				evaluatedSolutions.add(new Pair<>(node, score));
			} catch (Exception e) {
				logger.error("Couldn't evaluate {}", node);
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
	private V getBestSolution(List<Pair<List<T>, V>> allEvaluatedPaths) {
		return allEvaluatedPaths.stream().map(Pair::getY).min(V::compareTo).orElseThrow(NoSuchElementException::new);
	}

	@Override
	public void setGenerator(GraphGenerator<T, String> generator) {
		INodeEvaluator<T, Double> nodeEvaluator = new RandomizedDepthFirstNodeEvaluator<>(this.random);
		GeneralEvaluatedTraversalTree<T, String, Double> completionProblem = new GeneralEvaluatedTraversalTree<>(
				generator, nodeEvaluator);
		randomPathCompleter = new RandomSearch<>(completionProblem, priorityPredicateForRDFS, this.random);
	}
}
