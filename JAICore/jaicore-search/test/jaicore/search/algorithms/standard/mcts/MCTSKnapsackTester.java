package jaicore.search.algorithms.standard.mcts;

import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.search.core.interfaces.IGraphSearchFactory;
import jaicore.search.model.probleminputs.GraphSearchProblemInput;
import jaicore.search.model.travesaltree.Node;
import jaicore.search.testproblems.knapsack.KnapsackProblem;
import jaicore.search.testproblems.knapsack.KnapsackProblem.KnapsackNode;
import jaicore.search.testproblems.knapsack.KnapsackTester;
import jaicore.search.testproblems.knapsack.KnapsackToGraphSearchProblemInputReducer;

public class MCTSKnapsackTester extends KnapsackTester<GraphSearchProblemInput<KnapsackNode,String,Double>,Object,Node<KnapsackNode,Double>, String> {

	@Override
	public AlgorithmProblemTransformer<KnapsackProblem, GraphSearchProblemInput<KnapsackNode, String, Double>> getProblemReducer() {
		return new KnapsackToGraphSearchProblemInputReducer();
	}

	@Override
	public IGraphSearchFactory<GraphSearchProblemInput<KnapsackNode, String, Double>, Object, KnapsackNode, String, Double, Node<KnapsackNode, Double>, String> getFactory() {
		return new UCTFactory<>();
	}
	
}