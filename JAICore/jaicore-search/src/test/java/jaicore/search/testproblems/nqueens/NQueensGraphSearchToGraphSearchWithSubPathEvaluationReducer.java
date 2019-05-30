package jaicore.search.testproblems.nqueens;

import jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.model.other.SearchGraphPath;
import jaicore.search.probleminputs.GraphSearchInput;
import jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;

public class NQueensGraphSearchToGraphSearchWithSubPathEvaluationReducer implements AlgorithmicProblemReduction<GraphSearchInput<QueenNode, String>, SearchGraphPath<QueenNode, String>, GraphSearchWithSubpathEvaluationsInput<QueenNode, String, Double>, SearchGraphPath<QueenNode, String>> {

	@Override
	public GraphSearchWithSubpathEvaluationsInput<QueenNode, String, Double> encodeProblem(final GraphSearchInput<QueenNode, String> problem) {
		INodeEvaluator<QueenNode, Double> nodeEvaluator = node -> (double) node.getPoint().getNumberOfAttackedCells();
		return new GraphSearchWithSubpathEvaluationsInput<>(problem.getGraphGenerator(), nodeEvaluator);

	}

	@Override
	public SearchGraphPath<QueenNode, String> decodeSolution(final SearchGraphPath<QueenNode, String> solution) {
		return solution;
	}
}
