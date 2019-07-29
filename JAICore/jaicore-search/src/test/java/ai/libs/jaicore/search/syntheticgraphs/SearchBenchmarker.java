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
import ai.libs.jaicore.search.syntheticgraphs.BalancedGraphGeneratorGenerator.N;

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
		TreasureIslandPathCostGenerator treasureGenerator = this.getTreasureGenerator(experiment.get("function"), numberOfIslands, numberOfIslandsWithTreasure, absoluteDistanceToIslands);
		BalancedGraphSearchWithPathEvaluationsProblem input = new BalancedGraphSearchWithPathEvaluationsProblem(branchingFactor, depth, treasureGenerator);

		/* get algorithm */
		AOptimalPathInORGraphSearch<?, N, Integer, Double> optimizer = this.getSearchAlgorithm(experiment.get("search"), input);
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
		EvaluatedSearchGraphPath<N, Integer, Double> path = optimizer.getBestSeenSolution();
		Map<String, Object> result = new HashMap<>();
		result.put("score", path.getScore());
		processor.processResults(result);
	}

	public TreasureIslandPathCostGenerator getTreasureGenerator(final String function, final int numberOfIslands, final int numberOfTreasures, final int distanceToIslands) {
		switch (function.toLowerCase()) {
		case "sine":
			ShiftedSineTreasureGenerator linkFuction = new ShiftedSineTreasureGenerator(numberOfIslands, numberOfTreasures, 0.1, 0.5);
			LinkedTreasureIslandPathCostGenerator treasureGenerator = new LinkedTreasureIslandPathCostGenerator(numberOfTreasures, distanceToIslands, numberOfIslands, linkFuction);
			return treasureGenerator;
		}
		throw new UnsupportedOperationException();
	}

	public AOptimalPathInORGraphSearch<?, N, Integer, Double> getSearchAlgorithm(final String algorithm, final GraphSearchWithPathEvaluationsInput<N, Integer, Double> input) {
		switch (algorithm) {
		case "random":
			IteratingGraphSearchOptimizerFactory<GraphSearchWithPathEvaluationsInput<N, Integer, Double>, N, Integer, Double> factory = new IteratingGraphSearchOptimizerFactory<GraphSearchWithPathEvaluationsInput<N, Integer, Double>, N, Integer, Double>();
			factory.setBaseAlgorithmFactory(new RandomSearchFactory<>());
			IteratingGraphSearchOptimizer<GraphSearchWithPathEvaluationsInput<N, Integer, Double>, N, Integer, Double> optimizer = factory.getAlgorithm(input);
			return optimizer;
		case "bf-uninformed":
			GraphSearchWithPathEvaluationsInputToGraphSearchWithSubpathEvaluationViaUninformedness<N, Integer> reducer = new GraphSearchWithPathEvaluationsInputToGraphSearchWithSubpathEvaluationViaUninformedness<>();
			return new StandardBestFirst<>(reducer.encodeProblem(input));
		case "bf-informed":
			GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformerViaRDFS<N, Integer, Double> reducer2 = new GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformerViaRDFS<>(n -> null, n -> false, 0, 3,
					10000, 10000);
			return new StandardBestFirst<>(reducer2.encodeProblem(input));
		case "uct":
			return new UCTPathSearch<>(input, 0, 0.0, false);
		case "uct-sp":
			return new SPUCTPathSearch<>(input, 0, 0.0, Math.sqrt(2), 100, false);
		case "dfs":
			IteratingGraphSearchOptimizerFactory<GraphSearchWithPathEvaluationsInput<N, Integer, Double>, N, Integer, Double> dfsFactory = new IteratingGraphSearchOptimizerFactory<GraphSearchWithPathEvaluationsInput<N, Integer, Double>, N, Integer, Double>();
			dfsFactory.setBaseAlgorithmFactory(new DepthFirstSearchFactory<>());
			return dfsFactory.getAlgorithm(input);
		default:
			throw new IllegalArgumentException("Unsupported algorithm " + algorithm);
		}
	}
}
