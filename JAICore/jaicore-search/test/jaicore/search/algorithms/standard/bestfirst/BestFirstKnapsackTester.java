package jaicore.search.algorithms.standard.bestfirst;

import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.search.core.interfaces.IGraphSearchFactory;
import jaicore.search.model.other.EvaluatedSearchGraphPath;
import jaicore.search.model.probleminputs.GeneralEvaluatedTraversalTree;
import jaicore.search.model.travesaltree.Node;
import jaicore.search.testproblems.knapsack.KnapsackTester;
import jaicore.search.testproblems.knapsack.KnapsackProblem;
import jaicore.search.testproblems.knapsack.KnapsackProblem.KnapsackNode;
import jaicore.search.testproblems.knapsack.KnapsackToGeneralTraversalTreeReducer;

public class BestFirstKnapsackTester extends KnapsackTester<GeneralEvaluatedTraversalTree<KnapsackNode, String, Double>, EvaluatedSearchGraphPath<KnapsackNode, String, Double>, Node<KnapsackNode,Double>, String> {
	
	@Override
	public IGraphSearchFactory<GeneralEvaluatedTraversalTree<KnapsackNode, String, Double>, EvaluatedSearchGraphPath<KnapsackNode, String, Double>, KnapsackNode, String, Double, Node<KnapsackNode,Double>, String> getFactory() {
		return new BestFirstFactory<>();
	}

	@Override
	public AlgorithmProblemTransformer<KnapsackProblem, GeneralEvaluatedTraversalTree<KnapsackNode, String, Double>> getProblemReducer() {
		return new KnapsackToGeneralTraversalTreeReducer();
	}
}