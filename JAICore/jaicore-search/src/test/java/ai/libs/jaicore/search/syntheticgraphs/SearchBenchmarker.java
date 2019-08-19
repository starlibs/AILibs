package ai.libs.jaicore.search.syntheticgraphs;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

import org.api4.java.algorithm.TimeOut;

import ai.libs.jaicore.experiments.ExperimentDBEntry;
import ai.libs.jaicore.experiments.IExperimentIntermediateResultProcessor;
import ai.libs.jaicore.experiments.IExperimentSetEvaluator;
import ai.libs.jaicore.experiments.exceptions.ExperimentEvaluationFailedException;
import ai.libs.jaicore.search.algorithms.standard.auxilliary.iteratingoptimizer.IteratingGraphSearchOptimizer;
import ai.libs.jaicore.search.algorithms.standard.auxilliary.iteratingoptimizer.IteratingGraphSearchOptimizerFactory;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.StandardBestFirst;
import ai.libs.jaicore.search.algorithms.standard.dfs.DepthFirstSearchFactory;
import ai.libs.jaicore.search.algorithms.standard.mcts.SPUCTPathSearch;
import ai.libs.jaicore.search.algorithms.standard.mcts.UCTPathSearch;
import ai.libs.jaicore.search.algorithms.standard.random.RandomSearchFactory;
import ai.libs.jaicore.search.core.interfaces.AOptimalPathInORGraphSearch;
import ai.libs.jaicore.search.model.other.EvaluatedSearchGraphPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;
import ai.libs.jaicore.search.problemtransformers.GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformerViaRDFS;
import ai.libs.jaicore.search.problemtransformers.GraphSearchWithPathEvaluationsInputToGraphSearchWithSubpathEvaluationViaUninformedness;
import ai.libs.jaicore.search.syntheticgraphs.graphmodels.ITransparentTreeNode;
import ai.libs.jaicore.search.syntheticgraphs.graphmodels.balanced.BalancedGraphSearchWithPathEvaluationsProblem;
import ai.libs.jaicore.search.syntheticgraphs.islandmodels.IIslandModel;
import ai.libs.jaicore.search.syntheticgraphs.islandmodels.equalsized.EqualSizedIslandsModel;
import ai.libs.jaicore.search.syntheticgraphs.treasuremodels.noisymean.LinkedTreasureIslandPathCostGenerator;
import ai.libs.jaicore.search.syntheticgraphs.treasuremodels.noisymean.NoisyMeanTreasureModel;
import ai.libs.jaicore.search.syntheticgraphs.treasuremodels.noisymean.ShiftedSineTreasureGenerator;

public class SearchBenchmarker implements IExperimentSetEvaluator {

	@Override
	public void evaluate(final ExperimentDBEntry experimentEntry, final IExperimentIntermediateResultProcessor processor) throws ExperimentEvaluationFailedException, InterruptedException {

		/* read experiment data */
		Map<String, String> experiment = experimentEntry.getExperiment().getValuesOfKeyFields();
		int seed = Integer.parseInt(experiment.get("seed"));
		int branchingFactor = Integer.parseInt(experiment.get("branching"));
		int depth = Integer.parseInt(experiment.get("depth"));
		double relativeDistanceToIslands = Double.parseDouble(experiment.get("distance"));
		int absoluteDistanceToIslands = (int)Math.ceil(relativeDistanceToIslands * depth);
		int numberOfIslands = (int) Math.pow(branchingFactor, absoluteDistanceToIslands);
		int numberOfIslandsWithTreasure = Math.min(numberOfIslands, Integer.parseInt(experiment.get("treasures")));
		int treasureNodes = (int)Math.pow(branchingFactor, depth - absoluteDistanceToIslands);
		int maxiter = Math.min(treasureNodes < 1000 ? treasureNodes : (int)(treasureNodes * 0.5), Integer.parseInt(experiment.get("maxiter")));

		/* create graph search input */
		IIslandModel islandModel = new EqualSizedIslandsModel(numberOfIslands);
		NoisyMeanTreasureModel treasureGenerator = this.getTreasureGenerator(experiment.get("function"), islandModel, numberOfIslandsWithTreasure);
		BalancedGraphSearchWithPathEvaluationsProblem input = new BalancedGraphSearchWithPathEvaluationsProblem(branchingFactor, depth, treasureGenerator);

		/* get algorithm */
		AOptimalPathInORGraphSearch<?, ITransparentTreeNode, Integer, Double> optimizer = this.getSearchAlgorithm(experiment.get("search"), input);
		optimizer.setTimeout(new TimeOut(30, TimeUnit.SECONDS));

		System.out.println("Starting " + experiment.get("search") + " with " + maxiter + " iterations for " + branchingFactor + "/" + depth + "/" + absoluteDistanceToIslands + "/" + numberOfIslandsWithTreasure);

		/* run search algorithm */
		try {
			for (int i = 0; i < maxiter; i++) {
				optimizer.nextSolutionCandidate();
				if (i > 0 && i % 1000 == 0) {
					System.out.println("Collected " + i + " solutions already.");
				}
			}
		}
		catch (NoSuchElementException e) {
			/* this just may happen */
		}
		catch (Exception e) {
			throw new ExperimentEvaluationFailedException(e);
		}

		/* store result of best seen solution */
		EvaluatedSearchGraphPath<ITransparentTreeNode, Integer, Double> path = optimizer.getBestSeenSolution();
		Map<String, Object> result = new HashMap<>();
		result.put("score", path.getScore());
		processor.processResults(result);
	}

	public NoisyMeanTreasureModel getTreasureGenerator(final String function, final IIslandModel islandModel, final int numberOfTreasures) {
		switch (function.toLowerCase()) {
		case "sine":
			ShiftedSineTreasureGenerator linkFuction = new ShiftedSineTreasureGenerator(islandModel, numberOfTreasures, 0.1, 0.5);
			LinkedTreasureIslandPathCostGenerator treasureGenerator = new LinkedTreasureIslandPathCostGenerator(islandModel, linkFuction);
			return treasureGenerator;
		}
		throw new UnsupportedOperationException();
	}

	public AOptimalPathInORGraphSearch<?, ITransparentTreeNode, Integer, Double> getSearchAlgorithm(final String algorithm, final GraphSearchWithPathEvaluationsInput<ITransparentTreeNode, Integer, Double> input) {
		switch (algorithm) {
		case "random":
			IteratingGraphSearchOptimizerFactory<GraphSearchWithPathEvaluationsInput<ITransparentTreeNode, Integer, Double>, ITransparentTreeNode, Integer, Double> factory = new IteratingGraphSearchOptimizerFactory<GraphSearchWithPathEvaluationsInput<ITransparentTreeNode, Integer, Double>, ITransparentTreeNode, Integer, Double>();
			factory.setBaseAlgorithmFactory(new RandomSearchFactory<>());
			IteratingGraphSearchOptimizer<GraphSearchWithPathEvaluationsInput<ITransparentTreeNode, Integer, Double>, ITransparentTreeNode, Integer, Double> optimizer = factory.getAlgorithm(input);
			return optimizer;
		case "bf-uninformed":
			GraphSearchWithPathEvaluationsInputToGraphSearchWithSubpathEvaluationViaUninformedness<ITransparentTreeNode, Integer> reducer = new GraphSearchWithPathEvaluationsInputToGraphSearchWithSubpathEvaluationViaUninformedness<>();
			return new StandardBestFirst<>(reducer.encodeProblem(input));
		case "bf-informed":
			GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformerViaRDFS<ITransparentTreeNode, Integer, Double> reducer2 = new GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformerViaRDFS<>(n -> null, n -> false, 0, 3,
					10000, 10000);
			return new StandardBestFirst<>(reducer2.encodeProblem(input));
		case "uct":
			return new UCTPathSearch<>(input, 0, 0.0, false);
		case "uct-sp":
			return new SPUCTPathSearch<>(input, 0, 0.0, Math.sqrt(2), 100, false);
		case "dfs":
			IteratingGraphSearchOptimizerFactory<GraphSearchWithPathEvaluationsInput<ITransparentTreeNode, Integer, Double>, ITransparentTreeNode, Integer, Double> dfsFactory = new IteratingGraphSearchOptimizerFactory<GraphSearchWithPathEvaluationsInput<ITransparentTreeNode, Integer, Double>, ITransparentTreeNode, Integer, Double>();
			dfsFactory.setBaseAlgorithmFactory(new DepthFirstSearchFactory<>());
			return dfsFactory.getAlgorithm(input);
		default:
			throw new IllegalArgumentException("Unsupported algorithm " + algorithm);
		}
	}
}
