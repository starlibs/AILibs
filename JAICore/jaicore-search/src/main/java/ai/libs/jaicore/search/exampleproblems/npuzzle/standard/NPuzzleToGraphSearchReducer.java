package ai.libs.jaicore.search.exampleproblems.npuzzle.standard;

import java.util.Arrays;
import java.util.List;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;

import ai.libs.jaicore.basic.StringUtil;
import ai.libs.jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import ai.libs.jaicore.problems.npuzzle.NPuzzleProblem;
import ai.libs.jaicore.problems.npuzzle.NPuzzleState;
import ai.libs.jaicore.search.core.interfaces.EdgeCountingSolutionEvaluator;
import ai.libs.jaicore.search.model.other.EvaluatedSearchGraphPath;
import ai.libs.jaicore.search.model.other.SearchGraphPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;

public class NPuzzleToGraphSearchReducer implements AlgorithmicProblemReduction<NPuzzleProblem, List<String>, GraphSearchWithSubpathEvaluationsInput<NPuzzleState, String, Integer>, EvaluatedSearchGraphPath<NPuzzleState, String, Integer>> {

	@Override
	public GraphSearchWithSubpathEvaluationsInput<NPuzzleState, String, Integer> encodeProblem(final NPuzzleProblem problem) {
		IPathEvaluator<NPuzzleState, String, Integer> pathEvaluator = p -> new EdgeCountingSolutionEvaluator<NPuzzleState, String>()
				.evaluate(new SearchGraphPath<>(p.getNodes(), Arrays.asList(StringUtil.getArrayWithValues(p.getNodes().size() - 1, "")))).intValue();
		return new GraphSearchWithSubpathEvaluationsInput<>(new NPuzzleGraphGenerator(problem.getBoard()), new NPuzzleGoalPredicate(problem.getDim()), pathEvaluator);
	}

	@Override
	public List<String> decodeSolution(final EvaluatedSearchGraphPath<NPuzzleState, String, Integer> solution) {
		return solution.getArcs();
	}
}
