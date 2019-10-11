package ai.libs.jaicore.search.experiments;

import org.api4.java.ai.graphsearch.problem.IGraphSearchWithPathEvaluationsInput;
import org.api4.java.ai.graphsearch.problem.IOptimalPathInORGraphSearch;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IEvaluatedPath;

import ai.libs.jaicore.basic.IOwnerBasedRandomConfig;
import ai.libs.jaicore.experiments.Experiment;
import ai.libs.jaicore.experiments.configurations.IAlgorithmNameConfig;
import ai.libs.jaicore.search.algorithms.standard.auxilliary.iteratingoptimizer.IteratingGraphSearchOptimizer;
import ai.libs.jaicore.search.algorithms.standard.auxilliary.iteratingoptimizer.IteratingGraphSearchOptimizerFactory;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.BestFirst;
import ai.libs.jaicore.search.algorithms.standard.dfs.DepthFirstSearchFactory;
import ai.libs.jaicore.search.algorithms.standard.mcts.MCTSWithPLKPathSearch;
import ai.libs.jaicore.search.algorithms.standard.mcts.SPUCTPathSearch;
import ai.libs.jaicore.search.algorithms.standard.mcts.UCTPathSearch;
import ai.libs.jaicore.search.algorithms.standard.mcts.comparison.BradleyTerryMCTSPathSearch;
import ai.libs.jaicore.search.algorithms.standard.mcts.comparison.FixedCommitmentMCTSPathSearch;
import ai.libs.jaicore.search.algorithms.standard.mcts.tag.TAGMCTSPathSearch;
import ai.libs.jaicore.search.algorithms.standard.mcts.thompson.DNGMCTSPathSearch;
import ai.libs.jaicore.search.algorithms.standard.random.RandomSearchFactory;
import ai.libs.jaicore.search.problemtransformers.GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformerViaRDFS;
import ai.libs.jaicore.search.problemtransformers.GraphSearchWithPathEvaluationsInputToGraphSearchWithSubpathEvaluationViaUninformedness;

public class StandardExperimentSearchAlgorithmFactory<N, A, I extends IGraphSearchWithPathEvaluationsInput<N, A, Double>> {

	public IOptimalPathInORGraphSearch<I, ? extends IEvaluatedPath<N, A, Double>, N, A, Double> getAlgorithm(final Experiment experiment, final IGraphSearchWithPathEvaluationsInput<N, A, Double> input) {
		final int seed = Integer.parseInt(experiment.getValuesOfKeyFields().get(IOwnerBasedRandomConfig.K_SEED));
		final String algorithm = experiment.getValuesOfKeyFields().get(IAlgorithmNameConfig.K_ALGORITHM_NAME);
		switch (algorithm) {
		case "random":
			IteratingGraphSearchOptimizerFactory<I, N, A, Double> factory = new IteratingGraphSearchOptimizerFactory<I, N, A, Double>();
			factory.setBaseAlgorithmFactory(new RandomSearchFactory<>());
			IteratingGraphSearchOptimizer<I, N, A, Double> optimizer = factory.getAlgorithm((I)input);
			return optimizer;
		case "bf-uninformed":
			GraphSearchWithPathEvaluationsInputToGraphSearchWithSubpathEvaluationViaUninformedness<N, A> reducer = new GraphSearchWithPathEvaluationsInputToGraphSearchWithSubpathEvaluationViaUninformedness<>();
			IGraphSearchWithPathEvaluationsInput<N, A, Double> reducedProblem = reducer.encodeProblem(input);
			return new BestFirst<I, N, A, Double>((I)reducedProblem);
		case "bf-informed":
			GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformerViaRDFS<N, A, Double> reducer2 = new GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformerViaRDFS<>(n -> null,
					n -> false, seed, 3, 10000, 10000);
			return new BestFirst<>((I)reducer2.encodeProblem(input));
		case "uct":
			return new UCTPathSearch<I, N, A>((I)input, seed, 0.0, false);
		case "uct-sp":
			return new SPUCTPathSearch<>((I)input, seed, 0.0, Math.sqrt(2), 100, false);
		case "uct-pl":
			return new MCTSWithPLKPathSearch<>((I)input, 1, seed, 0.0, 100);
		case "bt":
			return new BradleyTerryMCTSPathSearch<>((I)input, seed, true);
		case "mcts-kfix-100-mean":
			return new FixedCommitmentMCTSPathSearch<>((I)input, 0.0, 100, d -> d.getMean());
		case "mcts-kfix-200-mean":
			return new FixedCommitmentMCTSPathSearch<>((I)input, 0.0, 200, d -> d.getMean());
		case "dng":
			return new DNGMCTSPathSearch<>((I)input, seed, 0.0);
		case "tag":
			return new TAGMCTSPathSearch<>((I)input, seed, 0.0);
		case "dfs":
			IteratingGraphSearchOptimizerFactory<I, N, A, Double> dfsFactory = new IteratingGraphSearchOptimizerFactory<I, N, A, Double>();
			dfsFactory.setBaseAlgorithmFactory(new DepthFirstSearchFactory<>());
			return dfsFactory.getAlgorithm((I)input);
		default:
			throw new IllegalArgumentException("Unsupported algorithm " + algorithm);
		}
	}
}
