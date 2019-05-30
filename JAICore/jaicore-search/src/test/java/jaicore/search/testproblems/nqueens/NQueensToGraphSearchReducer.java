package jaicore.search.testproblems.nqueens;

import java.util.List;

import jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.model.other.AgnosticPathEvaluator;
import jaicore.search.model.other.SearchGraphPath;
import jaicore.search.probleminputs.GraphSearchInput;
import jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;
import jaicore.testproblems.nqueens.NQueensProblem;

public class NQueensToGraphSearchReducer implements AlgorithmicProblemReduction<NQueensProblem, List<Integer>, GraphSearchInput<QueenNode, String>, SearchGraphPath<QueenNode, String>> {

	@Override
	public GraphSearchInput<QueenNode, String> encodeProblem(final NQueensProblem problem) {
		GraphGenerator<QueenNode, String> graphGenerator = new NQueensGraphGenerator(problem.getN());
		return new GraphSearchWithPathEvaluationsInput<>(graphGenerator, new AgnosticPathEvaluator<>());
	}

	@Override
	public List<Integer> decodeSolution(final SearchGraphPath<QueenNode, String> solution) {
		return solution.getNodes().get(solution.getNodes().size() - 1).getPositions();
	}
}
