package jaicore.search.algorithms.standard.mcts;

import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.search.core.interfaces.IGraphSearchFactory;
import jaicore.search.model.other.EvaluatedSearchGraphPath;
import jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;
import jaicore.search.testproblems.nqueens.NQueenTester;
import jaicore.search.testproblems.nqueens.NQueensToGraphSearchProblemInputReducer;
import jaicore.search.testproblems.nqueens.QueenNode;

public class MCTSNQueenTester extends NQueenTester<GraphSearchWithPathEvaluationsInput<QueenNode,String,Double>, EvaluatedSearchGraphPath<QueenNode, String,Double>> {

	@Override
	public AlgorithmProblemTransformer<Integer, GraphSearchWithPathEvaluationsInput<QueenNode, String, Double>> getProblemReducer() {
		return new NQueensToGraphSearchProblemInputReducer();
	}
	
	@Override
	public IGraphSearchFactory<GraphSearchWithPathEvaluationsInput<QueenNode, String, Double>, EvaluatedSearchGraphPath<QueenNode, String,Double>, QueenNode, String> getFactory() {
		UCTFactory<QueenNode, String> factory = new UCTFactory<>();
		factory.setEvaluationFailurePenalty(0.0);
		return factory;
	}

}
