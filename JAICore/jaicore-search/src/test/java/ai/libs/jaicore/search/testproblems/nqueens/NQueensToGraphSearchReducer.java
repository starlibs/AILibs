package ai.libs.jaicore.search.testproblems.nqueens;

import java.util.List;

import ai.libs.jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import ai.libs.jaicore.search.core.interfaces.GraphGenerator;
import ai.libs.jaicore.search.model.other.AgnosticPathEvaluator;
import ai.libs.jaicore.search.model.other.SearchGraphPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchInput;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;
import ai.libs.jaicore.testproblems.nqueens.NQueensProblem;

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
