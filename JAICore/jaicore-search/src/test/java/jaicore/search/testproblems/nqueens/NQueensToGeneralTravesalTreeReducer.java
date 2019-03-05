package jaicore.search.testproblems.nqueens;

import java.util.List;
import java.util.stream.Collectors;

import jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.model.other.SearchGraphPath;
import jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;
import jaicore.testproblems.nqueens.NQueensProblem;

public class NQueensToGeneralTravesalTreeReducer implements AlgorithmicProblemReduction<NQueensProblem, List<Integer>, GraphSearchWithSubpathEvaluationsInput<QueenNode, String, Double>, SearchGraphPath<QueenNode, String>> {

	@Override
	public GraphSearchWithSubpathEvaluationsInput<QueenNode, String, Double> encodeProblem(final NQueensProblem problem) {
		GraphGenerator<QueenNode, String> graphGenerator = new NQueensGraphGenerator(problem.getN());
		INodeEvaluator<QueenNode, Double> nodeEvaluator = node -> (double) node.getPoint().getNumberOfAttackedCells();
		return new GraphSearchWithSubpathEvaluationsInput<>(graphGenerator, nodeEvaluator);

	}

	@Override
	public List<Integer> decodeSolution(final SearchGraphPath<QueenNode, String> solution) {
		return solution.getEdges().stream().map(Integer::valueOf).collect(Collectors.toList());
	}
}
