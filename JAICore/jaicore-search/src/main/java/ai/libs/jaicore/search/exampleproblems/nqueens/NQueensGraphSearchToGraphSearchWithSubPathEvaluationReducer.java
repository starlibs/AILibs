package ai.libs.jaicore.search.exampleproblems.nqueens;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;

import ai.libs.jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import ai.libs.jaicore.search.model.other.SearchGraphPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchInput;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;

public class NQueensGraphSearchToGraphSearchWithSubPathEvaluationReducer implements AlgorithmicProblemReduction<GraphSearchInput<QueenNode, String>, SearchGraphPath<QueenNode, String>, GraphSearchWithSubpathEvaluationsInput<QueenNode, String, Double>, SearchGraphPath<QueenNode, String>> {

	@Override
	public GraphSearchWithSubpathEvaluationsInput<QueenNode, String, Double> encodeProblem(final GraphSearchInput<QueenNode, String> problem) {
		IPathEvaluator<QueenNode, String, Double> nodeEvaluator = node -> (double) node.getHead().getNumberOfAttackedCells();
		return new GraphSearchWithSubpathEvaluationsInput<>(problem, nodeEvaluator);

	}

	@Override
	public SearchGraphPath<QueenNode, String> decodeSolution(final SearchGraphPath<QueenNode, String> solution) {
		return solution;
	}
}
