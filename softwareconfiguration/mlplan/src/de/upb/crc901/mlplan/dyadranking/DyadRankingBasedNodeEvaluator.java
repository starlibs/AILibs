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
import jaicore.basic.IObjectEvaluator;
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
	private IObjectEvaluator<ComponentInstance, V> pipelineEvaluator;

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

	private double[] datasetMetaFeatures;

	/*
	 * Specifies the amount of paths that will be evaluated after ranking the paths
	 */
	private final int evaluatedPaths;

	private final Random random;

	private final Predicate<T> priorityPredicateForRDFS;

	private PLNetDyadRanker dyadRanker = new PLNetDyadRanker();

	private WEKAPipelineCharacterizer characterizer;

	public DyadRankingBasedNodeEvaluator(Collection<Component> components, IObjectEvaluator<ComponentInstance, V> pipelineEvaluator,
			int randomlyCompletedPaths, int evaluatedPaths, Random random, Predicate<T> priorityPredicateForRDFS,
			double[] datasetMetaFeatures) {
		super();
		this.components = components;
		this.pipelineEvaluator = pipelineEvaluator;
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
		try {
			this.dyadRanker.loadModelFromFile(Paths.get("resources","draco", "plnet", "pretrained_plnet.zip").toString());
		} catch (IOException e) {
			throw new RuntimeException(e);
			//logger.error("Could load model for plnet");
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
		List<ComponentInstance> topKRankedPaths = allRankedPaths.subList(0, evaluatedPaths);
		// evaluate the top k paths
		List<Pair<ComponentInstance, V>> allEvaluatedPaths = evaluateTopKPaths(topKRankedPaths);
		Duration evaluationTime = Duration.between(startOfEvaluation, Instant.now());
		logger.info("Evaluation of node {} took {}ms", node.getPoint(), evaluationTime.toMillis());

		return getBestSolution(allEvaluatedPaths);
	}

	private List<ComponentInstance> getDyadRankedPaths(List<T> randomPaths) {
		List<ComponentInstance> randomPipelines = new ArrayList<>();
		// extract componentInstances that we can rank
		for (T randomPath : randomPaths) {
			TFDNode goalNode = (TFDNode) randomPath;
			ComponentInstance cI = Util.getSolutionCompositionFromState(components, goalNode.getState(), true);
			randomPipelines.add(cI);
		}
		// invoke dyad ranker
		return rankRandomPipelines(randomPipelines);
	}

	private List<ComponentInstance> rankRandomPipelines(List<ComponentInstance> randomPipelines) {
		Map<Vector, ComponentInstance> dyadToPath = new HashMap<>();
		List<Vector> alternatives = new ArrayList<>();
		for (ComponentInstance ci : randomPipelines) {
			Vector pipelineCharacterization = new DenseDoubleVector(characterizer.characterize(ci));
			dyadToPath.put(pipelineCharacterization, ci);
			alternatives.add(pipelineCharacterization);
		}
		SparseDyadRankingInstance toRank = new SparseDyadRankingInstance(new DenseDoubleVector(datasetMetaFeatures),
				alternatives);
		IDyadRankingInstance rankedInstance;
		try {
			rankedInstance = dyadRanker.predict(toRank);
			List<ComponentInstance> rankedPipelines = new ArrayList<>();
			for (Dyad dyad : rankedInstance) {
				rankedPipelines.add(dyadToPath.get(dyad.getAlternative()));
			}
			return rankedPipelines;
		} catch (PredictionException e) {
			throw new RuntimeException(e);
		}
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

	/**
	 * Invokes the solution-evaluator to get the performances of the best k paths.
	 * 
	 * @param topKRankedPaths
	 *            the paths, after ranking
	 * @return the list of scores.
	 */
	private List<Pair<ComponentInstance, V>> evaluateTopKPaths(List<ComponentInstance> topKRankedPaths) {
		List<Pair<ComponentInstance, V>> evaluatedSolutions = new ArrayList<>();
		for (ComponentInstance node : topKRankedPaths) {
			try {
				V score = pipelineEvaluator.evaluate(node);
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
	private V getBestSolution(List<Pair<ComponentInstance, V>> allEvaluatedPaths) {

		return allEvaluatedPaths.stream().map(Pair::getY).min(V::compareTo).orElseThrow(NoSuchElementException::new);
	}

	@Override
	public void setGenerator(GraphGenerator<T, String> generator) {
		INodeEvaluator<T, Double> nodeEvaluator = new RandomizedDepthFirstNodeEvaluator<>(this.random);
		GeneralEvaluatedTraversalTree<T, String, Double> completionProblem = new GeneralEvaluatedTraversalTree<>(
				generator, nodeEvaluator);
		randomPathCompleter = new RandomSearch<>(completionProblem, priorityPredicateForRDFS, this.random);
	}

	public void setMetaFeatures(double[] array) {
		this.datasetMetaFeatures = array;
	}

	public void setPipelineEvaluator(IObjectEvaluator<ComponentInstance, V> wrappedSearchBenchmark) {
		this.pipelineEvaluator = wrappedSearchBenchmark;
	}
}
