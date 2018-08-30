package jaicore.search.testproblems.knapsack;

import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.search.model.probleminputs.GraphSearchProblemInput;
import jaicore.search.testproblems.knapsack.KnapsackProblem.KnapsackNode;

public class KnapsackToGraphSearchProblemInputReducer implements AlgorithmProblemTransformer<KnapsackProblem, GraphSearchProblemInput<KnapsackNode, String, Double>> {

	@Override
	public GraphSearchProblemInput<KnapsackNode, String, Double> transform(KnapsackProblem problem) {
		return new GraphSearchProblemInput<>(problem.getGraphGenerator(), problem.getSolutionEvaluator());

	}
}
