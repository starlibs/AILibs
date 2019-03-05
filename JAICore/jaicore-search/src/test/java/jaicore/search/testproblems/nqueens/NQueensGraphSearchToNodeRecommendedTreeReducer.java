package jaicore.search.testproblems.nqueens;

import jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import jaicore.search.model.other.SearchGraphPath;
import jaicore.search.probleminputs.GraphSearchInput;
import jaicore.search.probleminputs.GraphSearchWithNodeRecommenderInput;

public class NQueensGraphSearchToNodeRecommendedTreeReducer implements AlgorithmicProblemReduction<GraphSearchInput<QueenNode, String>, SearchGraphPath<QueenNode, String>, GraphSearchWithNodeRecommenderInput<QueenNode, String>, SearchGraphPath<QueenNode, String>> {

	@Override
	public GraphSearchWithNodeRecommenderInput<QueenNode, String> encodeProblem(final GraphSearchInput<QueenNode, String> problem) {
		return new GraphSearchWithNodeRecommenderInput<>(problem.getGraphGenerator(), (n1, n2) -> Integer.valueOf(n1.getNumberOfAttackedCells()).compareTo(n2.getNumberOfAttackedCells()));
	}

	@Override
	public SearchGraphPath<QueenNode, String> decodeSolution(final SearchGraphPath<QueenNode, String> solution) {
		return solution;
	}
}
