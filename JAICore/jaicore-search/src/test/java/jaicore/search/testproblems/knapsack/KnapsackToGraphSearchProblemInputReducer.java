package jaicore.search.testproblems.knapsack;

import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;

public class KnapsackToGraphSearchProblemInputReducer implements AlgorithmProblemTransformer<KnapsackProblem, GraphSearchWithPathEvaluationsInput<KnapsackNode, String, Double>> {

	@Override
	public GraphSearchWithPathEvaluationsInput<KnapsackNode, String, Double> transform(KnapsackProblem problem) {
		return new GraphSearchWithPathEvaluationsInput<>(new KnapsackProblemGraphGenerator(problem), problem.getSolutionEvaluator());
	}
}
