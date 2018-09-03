package jaicore.search.algorithms.standard.lds;

import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.search.core.interfaces.IGraphSearchFactory;
import jaicore.search.model.other.EvaluatedSearchGraphPath;
import jaicore.search.model.probleminputs.NodeRecommendedTree;
import jaicore.search.model.travesaltree.Node;
import jaicore.search.testproblems.nqueens.NQueenTester;
import jaicore.search.testproblems.nqueens.NQueensToNodeRecommendedTreeReducer;
import jaicore.search.testproblems.nqueens.QueenNode;

public class BestFirstLimitedDiscrepancySearchNQueenTester extends NQueenTester<NodeRecommendedTree<QueenNode,String>,EvaluatedSearchGraphPath<QueenNode,String,Double>,Node<QueenNode,NodeOrderList>,String> {

	@Override
	public AlgorithmProblemTransformer<Integer, NodeRecommendedTree<QueenNode, String>> getProblemReducer() {
		return new NQueensToNodeRecommendedTreeReducer();
	}

	@Override
	public IGraphSearchFactory<NodeRecommendedTree<QueenNode, String>, EvaluatedSearchGraphPath<QueenNode, String, Double>, QueenNode, String, Double, Node<QueenNode, NodeOrderList>, String> getFactory() {
		return new BestFirstLimitedDiscrepancySearchFactory<>();
	}
	
	
}
