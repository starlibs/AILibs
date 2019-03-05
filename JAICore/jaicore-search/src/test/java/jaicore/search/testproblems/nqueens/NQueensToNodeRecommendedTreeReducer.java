package jaicore.search.testproblems.nqueens;

import java.util.List;
import java.util.stream.Collectors;

import jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import jaicore.search.model.other.SearchGraphPath;
import jaicore.search.probleminputs.GraphSearchWithNodeRecommenderInput;
import jaicore.testproblems.nqueens.NQueensProblem;

public class NQueensToNodeRecommendedTreeReducer implements AlgorithmicProblemReduction<NQueensProblem, List<Integer>, GraphSearchWithNodeRecommenderInput<QueenNode, String>, SearchGraphPath<QueenNode, String>> {

	@Override
	public GraphSearchWithNodeRecommenderInput<QueenNode, String> encodeProblem(final NQueensProblem problem) {
		return new GraphSearchWithNodeRecommenderInput<>(new NQueensGraphGenerator(problem.getN()), (n1, n2) -> Integer.valueOf(n1.getNumberOfAttackedCells()).compareTo(n2.getNumberOfAttackedCells()));
	}

	@Override
	public List<Integer> decodeSolution(final SearchGraphPath<QueenNode, String> solution) {
		return solution.getEdges().stream().map(Integer::valueOf).collect(Collectors.toList());
	}
}
