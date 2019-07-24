package ai.libs.jaicore.search.syntheticgraphs;

import java.util.concurrent.TimeUnit;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;
import org.api4.java.algorithm.TimeOut;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.junit.Test;

import ai.libs.jaicore.search.algorithms.standard.auxilliary.iteratingoptimizer.IteratingGraphSearchOptimizer;
import ai.libs.jaicore.search.algorithms.standard.auxilliary.iteratingoptimizer.IteratingGraphSearchOptimizerFactory;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.StandardBestFirst;
import ai.libs.jaicore.search.algorithms.standard.dfs.DepthFirstSearchFactory;
import ai.libs.jaicore.search.algorithms.standard.mcts.UCTPathSearch;
import ai.libs.jaicore.search.algorithms.standard.random.RandomSearchFactory;
import ai.libs.jaicore.search.core.interfaces.AOptimalPathInORGraphSearch;
import ai.libs.jaicore.search.model.other.EvaluatedSearchGraphPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchInput;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;
import ai.libs.jaicore.search.problemtransformers.GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformerViaRDFS;
import ai.libs.jaicore.search.syntheticgraphs.BalancedGraphGeneratorGenerator.N;

public class SyntheticBenchmarks {

	private int branchingFactor = 10;
	private int depth = 20;
	private int numberOfIslandsWithTreasure = 1;
	private int distanceToIslands = 5;
	private int seed = 0;
	private TimeOut timeout = new TimeOut(3, TimeUnit.SECONDS);

	private final GraphSearchWithPathEvaluationsInput<N, Integer, Double> input;

	public SyntheticBenchmarks() {
		GraphSearchInput<N, Integer> plainInput = new BalanceGraphSearchProblem(this.branchingFactor, this.depth);
		IPathEvaluator<N, Integer, Double> evaluator = new TreasureIslandPathCostGenerator(this.numberOfIslandsWithTreasure, this.distanceToIslands, (int)Math.pow(this.branchingFactor, this.distanceToIslands));
		this.input = new GraphSearchWithPathEvaluationsInput<>(plainInput, evaluator);
	}

	@Test
	public void runRandomSearch() throws AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {
		IteratingGraphSearchOptimizerFactory<GraphSearchWithPathEvaluationsInput<N, Integer, Double>, N, Integer, Double> factory = new IteratingGraphSearchOptimizerFactory<GraphSearchWithPathEvaluationsInput<N, Integer, Double>, N, Integer, Double>();
		factory.setBaseAlgorithmFactory(new RandomSearchFactory<>());
		IteratingGraphSearchOptimizer<GraphSearchWithPathEvaluationsInput<N, Integer, Double>, N, Integer, Double> optimizer = factory.getAlgorithm(this.input);
		this.run(optimizer);
		System.out.println(optimizer.getNumberOfSeenSolutions());
	}

	@Test
	public void runBestFirstSearch() throws AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {
		GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformerViaRDFS<N, Integer, Double> reducer = new GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformerViaRDFS<>(n -> null, n -> false, 0, 3, 100, 100);
		StandardBestFirst<N, Integer, Double> optimizer = new StandardBestFirst<>(reducer.encodeProblem(this.input));

		this.run(optimizer);
		System.out.println(optimizer.getSolutionQueue().size());
	}

	@Test
	public void runUCT() throws AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {
		UCTPathSearch<N, Integer> optimizer = new UCTPathSearch<>(this.input, this.seed, 0.0, false);
		this.run(optimizer);
		System.out.println(optimizer.getNumberOfPlayouts());
	}

	@Test
	public void runDepthFirstSearch() throws AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {
		IteratingGraphSearchOptimizerFactory<GraphSearchWithPathEvaluationsInput<N, Integer, Double>, N, Integer, Double> factory = new IteratingGraphSearchOptimizerFactory<GraphSearchWithPathEvaluationsInput<N, Integer, Double>, N, Integer, Double>();
		factory.setBaseAlgorithmFactory(new DepthFirstSearchFactory<>());
		IteratingGraphSearchOptimizer<GraphSearchWithPathEvaluationsInput<N, Integer, Double>, N, Integer, Double> optimizer = factory.getAlgorithm(this.input);
		this.run(optimizer);
		System.out.println(optimizer.getNumberOfSeenSolutions());
	}

	public void run(final AOptimalPathInORGraphSearch<?, N, Integer, Double> optimizer) throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {
		optimizer.setTimeout(this.timeout);
		try {
			optimizer.call();
		}
		catch (AlgorithmTimeoutedException e) {
		}
		EvaluatedSearchGraphPath<N, Integer, Double> path = optimizer.getBestSeenSolution();
		System.out.println(path.getScore());
	}
}
