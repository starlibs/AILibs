package ai.libs.jaicore.search.experiments;

import java.util.Arrays;
import java.util.Random;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.api4.java.ai.graphsearch.problem.IOptimalPathInORGraphSearch;
import org.api4.java.ai.graphsearch.problem.IPathSearchWithPathEvaluationsInput;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IEvaluatedPath;

import ai.libs.jaicore.basic.IOwnerBasedRandomConfig;
import ai.libs.jaicore.experiments.Experiment;
import ai.libs.jaicore.experiments.configurations.IAlgorithmNameConfig;
import ai.libs.jaicore.search.algorithms.mdp.mcts.MCTSFactory;
import ai.libs.jaicore.search.algorithms.mdp.mcts.brue.BRUEFactory;
import ai.libs.jaicore.search.algorithms.mdp.mcts.comparison.FixedCommitmentMCTSFactory;
import ai.libs.jaicore.search.algorithms.mdp.mcts.comparison.PlackettLuceMCTSFactory;
import ai.libs.jaicore.search.algorithms.mdp.mcts.comparison.preferencekernel.bootstrapping.BootstrappingPreferenceKernel;
import ai.libs.jaicore.search.algorithms.mdp.mcts.comparison.preferencekernel.bootstrapping.DefaultBootsrapConfigurator;
import ai.libs.jaicore.search.algorithms.mdp.mcts.ensemble.EnsembleMCTSFactory;
import ai.libs.jaicore.search.algorithms.mdp.mcts.spuct.SPUCTFactory;
import ai.libs.jaicore.search.algorithms.mdp.mcts.tag.TAGMCTSFactory;
import ai.libs.jaicore.search.algorithms.mdp.mcts.thompson.DNGMCTSFactory;
import ai.libs.jaicore.search.algorithms.mdp.mcts.thompson.DNGPolicy;
import ai.libs.jaicore.search.algorithms.mdp.mcts.uct.UCBPolicy;
import ai.libs.jaicore.search.algorithms.mdp.mcts.uct.UCTFactory;
import ai.libs.jaicore.search.algorithms.mdp.mcts.uuct.UUCTFactory;
import ai.libs.jaicore.search.algorithms.mdp.mcts.uuct.utility.VaR;
import ai.libs.jaicore.search.algorithms.standard.auxilliary.iteratingoptimizer.IteratingGraphSearchOptimizer;
import ai.libs.jaicore.search.algorithms.standard.auxilliary.iteratingoptimizer.IteratingGraphSearchOptimizerFactory;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.BestFirst;
import ai.libs.jaicore.search.algorithms.standard.dfs.DepthFirstSearchFactory;
import ai.libs.jaicore.search.algorithms.standard.mcts.MCTSPathSearch;
import ai.libs.jaicore.search.algorithms.standard.random.RandomSearchFactory;
import ai.libs.jaicore.search.problemtransformers.GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformerViaRDFS;
import ai.libs.jaicore.search.problemtransformers.GraphSearchWithPathEvaluationsInputToGraphSearchWithSubpathEvaluationViaUninformedness;

public class StandardExperimentSearchAlgorithmFactory<N, A, I extends IPathSearchWithPathEvaluationsInput<N, A, Double>> {

	private MCTSPathSearch<I, N, A> getMCTS(final I input, final MCTSFactory<N, A, ?> factory, final int maxiter, final int seed) {
		factory.withRandom(new Random(seed));
		factory.withMaxIterations(maxiter);
		return new MCTSPathSearch<>(input, factory);
	}

	@SuppressWarnings("unchecked")
	public IOptimalPathInORGraphSearch<I, ? extends IEvaluatedPath<N, A, Double>, N, A, Double> getAlgorithm(final Experiment experiment, final IPathSearchWithPathEvaluationsInput<N, A, Double> input) {
		final int seed = Integer.parseInt(experiment.getValuesOfKeyFields().get(IOwnerBasedRandomConfig.K_SEED));
		final String algorithm = experiment.getValuesOfKeyFields().get(IAlgorithmNameConfig.K_ALGORITHM_NAME);

		final int maxiter = Integer.MAX_VALUE;

		if (algorithm.startsWith("uuct-")) {
			String[] parts = algorithm.split("-");
			double alpha = Double.parseDouble(parts[1]);
			double b = Double.parseDouble(parts[2]);
			UUCTFactory<N, A> uuctFactory = new UUCTFactory<>();
			uuctFactory.setUtility(new VaR(alpha, b));
			return this.getMCTS((I)input, uuctFactory, maxiter, seed);
		}

		switch (algorithm) {
		case "random":
			IteratingGraphSearchOptimizerFactory<I, N, A, Double> factory = new IteratingGraphSearchOptimizerFactory<>();
			RandomSearchFactory<N, A> rsf = new RandomSearchFactory<>();
			rsf.setSeed(seed);
			factory.setBaseAlgorithmFactory(rsf);
			IteratingGraphSearchOptimizer<I, N, A, Double> optimizer = factory.getAlgorithm((I)input);
			return optimizer;
		case "bf-uninformed":
			GraphSearchWithPathEvaluationsInputToGraphSearchWithSubpathEvaluationViaUninformedness<N, A> reducer = new GraphSearchWithPathEvaluationsInputToGraphSearchWithSubpathEvaluationViaUninformedness<>();
			IPathSearchWithPathEvaluationsInput<N, A, Double> reducedProblem = reducer.encodeProblem(input);
			return new BestFirst<>((I)reducedProblem);
		case "bf-informed":
			GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformerViaRDFS<N, A, Double> reducer2 = new GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformerViaRDFS<>(n -> null,
					n -> false, new Random(seed), 3,  30 * 1000, 60 * 1000);  // THIS IS AN ARBITRARY CONFIG USED FOR THE AUTOML SCENARIO (1h total timeout)!!
			return new BestFirst<>((I)reducer2.encodeProblem(input));
		case "uct":
			return this.getMCTS((I)input, new UCTFactory<>(), maxiter, seed);
		case "ensemble":

			DNGMCTSFactory<N, A> dngFactory = new DNGMCTSFactory<>();
			dngFactory.setInitLambda(0.01);
			DNGPolicy<N, A> dng001 = (DNGPolicy<N, A>)this.getMCTS((I)input, dngFactory, maxiter, seed).getMcts().getTreePolicy();
			dngFactory.setInitLambda(0.1);
			DNGPolicy<N, A> dng01 = (DNGPolicy<N, A>)this.getMCTS((I)input, dngFactory, maxiter, seed).getMcts().getTreePolicy();
			dngFactory.setInitLambda(1);
			DNGPolicy<N, A> dng1 = (DNGPolicy<N, A>)this.getMCTS((I)input, dngFactory, maxiter, seed).getMcts().getTreePolicy();
			dngFactory.setInitLambda(10);
			DNGPolicy<N, A> dng10 = (DNGPolicy<N, A>)this.getMCTS((I)input, dngFactory, maxiter, seed).getMcts().getTreePolicy();
			dngFactory.setInitLambda(100);
			DNGPolicy<N, A> dng100 = (DNGPolicy<N, A>)this.getMCTS((I)input, dngFactory, maxiter, seed).getMcts().getTreePolicy();
			EnsembleMCTSFactory<N, A> eFactory = new EnsembleMCTSFactory<>();
			eFactory.setTreePolicies(Arrays.asList(new UCBPolicy<>(1.0, true), dng001, dng01, dng1, dng01, dng10, dng100));
			return this.getMCTS((I)input, eFactory, maxiter, seed);
		case "sp-uct":
			SPUCTFactory<N, A> spucbFactory = new SPUCTFactory<>();
			spucbFactory.setBigD(10000);
			return this.getMCTS((I)input, spucbFactory, maxiter, seed);
		case "pl-mcts-mean":
			return this.getMCTS((I)input, new PlackettLuceMCTSFactory<N, A>().withPreferenceKernel(new BootstrappingPreferenceKernel<>(DescriptiveStatistics::getMean, new DefaultBootsrapConfigurator(), 1)), maxiter, seed);
		case "pl-mcts-mean+std":
			return this.getMCTS((I)input, new PlackettLuceMCTSFactory<N, A>().withPreferenceKernel(new BootstrappingPreferenceKernel<>(d -> d.getMean() + d.getStandardDeviation(), new DefaultBootsrapConfigurator(), 1)), maxiter, seed);
		case "pl-mcts-mean-std":
			return this.getMCTS((I)input, new PlackettLuceMCTSFactory<N, A>().withPreferenceKernel(new BootstrappingPreferenceKernel<>(d -> d.getMean() - d.getStandardDeviation(), new DefaultBootsrapConfigurator(), 1)), maxiter, seed);
		case "pl-mcts-min":
			return this.getMCTS((I)input, new PlackettLuceMCTSFactory<N, A>().withPreferenceKernel(new BootstrappingPreferenceKernel<>(DescriptiveStatistics::getMin, new DefaultBootsrapConfigurator(), 1)), maxiter, seed);
		case "mcts-kfix-100-mean":
			FixedCommitmentMCTSFactory<N, A> fcFactory1 = new FixedCommitmentMCTSFactory<>();
			fcFactory1.setK(100);
			fcFactory1.setMetric(DescriptiveStatistics::getMean);
			return this.getMCTS((I)input, fcFactory1, maxiter, seed);
		case "mcts-kfix-200-mean":
			FixedCommitmentMCTSFactory<N, A> fcFactory2 = new FixedCommitmentMCTSFactory<>();
			fcFactory2.setK(200);
			fcFactory2.setMetric(DescriptiveStatistics::getMean);
			return this.getMCTS((I)input, fcFactory2, maxiter, seed);
		case "dng":
			return this.getMCTS((I)input, new DNGMCTSFactory<>(), maxiter, seed);
		case "tag":
			return this.getMCTS((I)input, new TAGMCTSFactory<>(), maxiter, seed);
		case "brue":
			return this.getMCTS((I)input, new BRUEFactory<>(), maxiter, seed);
		case "dfs":
			IteratingGraphSearchOptimizerFactory<I, N, A, Double> dfsFactory = new IteratingGraphSearchOptimizerFactory<>();
			dfsFactory.setBaseAlgorithmFactory(new DepthFirstSearchFactory<>());
			return dfsFactory.getAlgorithm((I)input);
		default:
			throw new IllegalArgumentException("Unsupported algorithm " + algorithm);
		}
	}
}
