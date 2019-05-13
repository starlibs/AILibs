package ai.libs.jaicore.search.testproblems.npuzzle.standard;

import java.util.List;

import ai.libs.jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import ai.libs.jaicore.search.core.interfaces.EdgeCountingSolutionEvaluator;
import ai.libs.jaicore.search.model.other.EvaluatedSearchGraphPath;
import ai.libs.jaicore.search.model.other.SearchGraphPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;
import ai.libs.jaicore.testproblems.npuzzle.NPuzzleProblem;
import ai.libs.jaicore.testproblems.npuzzle.NPuzzleState;

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
