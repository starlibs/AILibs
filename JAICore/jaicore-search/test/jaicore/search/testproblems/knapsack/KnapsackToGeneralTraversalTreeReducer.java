package jaicore.search.testproblems.knapsack;

import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.search.model.probleminputs.GeneralEvaluatedTraversalTree;
import jaicore.search.testproblems.knapsack.KnapsackProblem.KnapsackNode;

public class KnapsackToGeneralTraversalTreeReducer implements AlgorithmProblemTransformer<KnapsackProblem, GeneralEvaluatedTraversalTree<KnapsackNode, String, Double>>{

	@Override
	public GeneralEvaluatedTraversalTree<KnapsackNode, String, Double> transform(KnapsackProblem problem) {
		return new GeneralEvaluatedTraversalTree<>(problem.getGraphGenerator(), n -> problem.getSolutionEvaluator().evaluateSolution(n.externalPath()));
	}

}
