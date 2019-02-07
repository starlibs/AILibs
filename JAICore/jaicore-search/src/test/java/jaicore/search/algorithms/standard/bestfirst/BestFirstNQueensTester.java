package jaicore.search.algorithms.standard.bestfirst;

import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.search.core.interfaces.IGraphSearchFactory;
import jaicore.search.model.other.EvaluatedSearchGraphPath;
import jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;
import jaicore.search.testproblems.nqueens.NQueenTester;
import jaicore.search.testproblems.nqueens.NQueensToGeneralTravesalTreeReducer;
import jaicore.search.testproblems.nqueens.QueenNode;

public class BestFirstNQueensTester
		extends NQueenTester<GraphSearchWithSubpathEvaluationsInput<QueenNode, String, Double>, EvaluatedSearchGraphPath<QueenNode, String, Double>> {

	@Override
	public IGraphSearchFactory<GraphSearchWithSubpathEvaluationsInput<QueenNode, String, Double>, EvaluatedSearchGraphPath<QueenNode, String, Double>, QueenNode, String> getFactory() {
		BestFirstFactory<GraphSearchWithSubpathEvaluationsInput<QueenNode, String, Double>,QueenNode, String, Double> searchFactory = new BestFirstFactory<>();
		return searchFactory;
	}

	@Override
	public AlgorithmProblemTransformer<Integer, GraphSearchWithSubpathEvaluationsInput<QueenNode, String, Double>> getProblemReducer() {
		return new NQueensToGeneralTravesalTreeReducer();
	}
}
