package jaicore.search.algorithms.standard.bestfirst;

import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.search.core.interfaces.IGraphSearchFactory;
import jaicore.search.model.other.EvaluatedSearchGraphPath;
import jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;
import jaicore.search.testproblems.knapsack.KnapsackProblem;
import jaicore.search.testproblems.knapsack.KnapsackProblem.KnapsackNode;
import jaicore.search.testproblems.knapsack.KnapsackTester;
import jaicore.search.testproblems.knapsack.KnapsackToGeneralTraversalTreeReducer;

public class BestFirstKnapsackTester extends KnapsackTester<GraphSearchWithSubpathEvaluationsInput<KnapsackNode, String, Double>, EvaluatedSearchGraphPath<KnapsackNode, String, Double>> {
	
	@Override
	public IGraphSearchFactory<GraphSearchWithSubpathEvaluationsInput<KnapsackNode, String, Double>, EvaluatedSearchGraphPath<KnapsackNode, String, Double>, KnapsackNode, String> getFactory() {
		return new BestFirstFactory<>();
	}

	@Override
	public AlgorithmProblemTransformer<KnapsackProblem, GraphSearchWithSubpathEvaluationsInput<KnapsackNode, String, Double>> getProblemReducer() {
		return new KnapsackToGeneralTraversalTreeReducer();
	}
}