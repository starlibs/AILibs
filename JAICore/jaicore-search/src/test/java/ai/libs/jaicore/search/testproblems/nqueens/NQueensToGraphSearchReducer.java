package ai.libs.jaicore.search.testproblems.nqueens;

import java.util.List;

import org.api4.java.datastructure.graph.implicit.IGraphGenerator;

import ai.libs.jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import ai.libs.jaicore.problems.nqueens.NQueensProblem;
import ai.libs.jaicore.search.exampleproblems.nqueens.QueenNode;
import ai.libs.jaicore.search.model.other.AgnosticPathEvaluator;
import ai.libs.jaicore.search.model.other.SearchGraphPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchInput;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;

public class NQueensToGraphSearchReducer implements AlgorithmicProblemReduction<NQueensProblem, List<Integer>, GraphSearchInput<QueenNode, String>, SearchGraphPath<QueenNode, String>> {

	@Override
	public GraphSearchInput<QueenNode, String> encodeProblem(final NQueensProblem problem) {
		IGraphGenerator<QueenNode, String> graphGenerator = new NQueensGraphGenerator(problem.getN());
		return new GraphSearchWithPathEvaluationsInput<>(graphGenerator, new NQueensGoalPredicate(problem.getN()), new AgnosticPathEvaluator<>());
	}

	@Override
	public List<Integer> decodeSolution(final SearchGraphPath<QueenNode, String> solution) {
		return solution.getHead().getPositions();
	}
}
