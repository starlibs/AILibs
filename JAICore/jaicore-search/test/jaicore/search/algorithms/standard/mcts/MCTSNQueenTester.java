package jaicore.search.algorithms.standard.mcts;

import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.search.core.interfaces.IGraphSearchFactory;
import jaicore.search.model.other.EvaluatedSearchGraphPath;
import jaicore.search.model.travesaltree.Node;
import jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;
import jaicore.search.testproblems.nqueens.NQueenTester;
import jaicore.search.testproblems.nqueens.NQueensToGraphSearchProblemInputReducer;
import jaicore.search.testproblems.nqueens.QueenNode;

public class MCTSNQueenTester extends NQueenTester<GraphSearchWithPathEvaluationsInput<QueenNode,String,Double>, EvaluatedSearchGraphPath<QueenNode, String,Double>,Node<QueenNode, Double>, String> {

	@Override
	public AlgorithmProblemTransformer<Integer, GraphSearchWithPathEvaluationsInput<QueenNode, String, Double>> getProblemReducer() {
		return new NQueensToGraphSearchProblemInputReducer();
	}

	@Override
	public IGraphSearchFactory<GraphSearchWithPathEvaluationsInput<QueenNode, String, Double>, EvaluatedSearchGraphPath<QueenNode, String,Double>, QueenNode, String, Node<QueenNode, Double>, String> getFactory() {
		return new UCTFactory<>();
	}

}
