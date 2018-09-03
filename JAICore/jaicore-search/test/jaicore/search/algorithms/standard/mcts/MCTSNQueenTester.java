package jaicore.search.algorithms.standard.mcts;

import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.search.core.interfaces.IGraphSearchFactory;
import jaicore.search.model.probleminputs.GraphSearchProblemInput;
import jaicore.search.model.travesaltree.Node;
import jaicore.search.testproblems.nqueens.NQueenTester;
import jaicore.search.testproblems.nqueens.NQueensToGraphSearchProblemInputReducer;
import jaicore.search.testproblems.nqueens.QueenNode;

public class MCTSNQueenTester extends NQueenTester<GraphSearchProblemInput<QueenNode,String,Double>,Object,Node<QueenNode, Double>, String> {

	@Override
	public AlgorithmProblemTransformer<Integer, GraphSearchProblemInput<QueenNode, String, Double>> getProblemReducer() {
		return new NQueensToGraphSearchProblemInputReducer();
	}

	@Override
	public IGraphSearchFactory<GraphSearchProblemInput<QueenNode, String, Double>, Object, QueenNode, String, Double, Node<QueenNode, Double>, String> getFactory() {
		return new UCTFactory<>();
	}

}
