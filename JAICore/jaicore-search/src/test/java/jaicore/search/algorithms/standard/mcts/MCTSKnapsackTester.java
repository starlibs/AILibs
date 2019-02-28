package jaicore.search.algorithms.standard.mcts;

import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.search.core.interfaces.IGraphSearchFactory;
import jaicore.search.model.other.EvaluatedSearchGraphPath;
import jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;
import jaicore.search.testproblems.knapsack.KnapsackProblem;
import jaicore.search.testproblems.knapsack.KnapsackProblem.KnapsackNode;
import jaicore.search.testproblems.knapsack.KnapsackTester;
import jaicore.search.testproblems.knapsack.KnapsackToGraphSearchProblemInputReducer;

public class MCTSKnapsackTester extends KnapsackTester<GraphSearchWithPathEvaluationsInput<KnapsackNode, String, Double>, EvaluatedSearchGraphPath<KnapsackNode, String, Double>> {

	@Override
	public AlgorithmProblemTransformer<KnapsackProblem, GraphSearchWithPathEvaluationsInput<KnapsackNode, String, Double>> getProblemReducer() {
		return new KnapsackToGraphSearchProblemInputReducer();
	}

	@Override
	public IGraphSearchFactory<GraphSearchWithPathEvaluationsInput<KnapsackNode, String, Double>, EvaluatedSearchGraphPath<KnapsackNode, String, Double>, KnapsackNode, String> getFactory() {
		UCTFactory<KnapsackNode, String> factory = new UCTFactory<>();
		factory.setEvaluationFailurePenalty(0.0);
		return factory;
	}

}