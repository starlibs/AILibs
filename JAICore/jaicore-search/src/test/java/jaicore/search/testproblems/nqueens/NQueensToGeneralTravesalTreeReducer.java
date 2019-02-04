package jaicore.search.testproblems.nqueens;

import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;

public class NQueensToGeneralTravesalTreeReducer implements AlgorithmProblemTransformer<Integer, GraphSearchWithSubpathEvaluationsInput<QueenNode, String, Double>> {

	@Override
	public GraphSearchWithSubpathEvaluationsInput<QueenNode, String, Double> transform(Integer problem) {
		GraphGenerator<QueenNode, String> graphGenerator = new NQueenGenerator(problem);
		INodeEvaluator<QueenNode, Double> nodeEvaluator = node -> (double) node.getPoint().getNumberOfAttackedCells();
		return new GraphSearchWithSubpathEvaluationsInput<>(graphGenerator, nodeEvaluator);

	}
}
