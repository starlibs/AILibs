package jaicore.search.testproblems.npuzzle.standard;

import java.util.List;

import jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import jaicore.search.core.interfaces.EdgeCountingSolutionEvaluator;
import jaicore.search.model.other.EvaluatedSearchGraphPath;
import jaicore.search.model.other.SearchGraphPath;
import jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;
import jaicore.testproblems.npuzzle.NPuzzleProblem;
import jaicore.testproblems.npuzzle.NPuzzleState;

public class NPuzzleToGraphSearchReducer implements AlgorithmicProblemReduction<NPuzzleProblem, List<String>, GraphSearchWithSubpathEvaluationsInput<NPuzzleState, String, Integer>, EvaluatedSearchGraphPath<NPuzzleState, String, Integer>> {

	@Override
	public GraphSearchWithSubpathEvaluationsInput<NPuzzleState, String, Integer> encodeProblem(final NPuzzleProblem problem) {
		return new GraphSearchWithSubpathEvaluationsInput<>(new NPuzzleGraphGenerator(problem.getBoard()), n -> new EdgeCountingSolutionEvaluator<NPuzzleState, String>().evaluate(new SearchGraphPath<>(n.externalPath())).intValue());
	}

	@Override
	public List<String> decodeSolution(final EvaluatedSearchGraphPath<NPuzzleState, String, Integer> solution) {
		return solution.getEdges();
	}
}
