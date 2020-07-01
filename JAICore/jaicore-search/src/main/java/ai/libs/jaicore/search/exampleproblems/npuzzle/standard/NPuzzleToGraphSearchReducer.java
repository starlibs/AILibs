package ai.libs.jaicore.search.exampleproblems.npuzzle.standard;

import java.util.Arrays;
import java.util.List;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;

import ai.libs.jaicore.basic.StringUtil;
import ai.libs.jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import ai.libs.jaicore.problems.npuzzle.NPuzzleProblem;
import ai.libs.jaicore.search.core.interfaces.EdgeCountingSolutionEvaluator;
import ai.libs.jaicore.search.model.other.EvaluatedSearchGraphPath;
import ai.libs.jaicore.search.model.other.SearchGraphPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;

public class NPuzzleToGraphSearchReducer implements AlgorithmicProblemReduction<NPuzzleProblem, List<String>, GraphSearchWithSubpathEvaluationsInput<NPuzzleProblem, String, Double>, EvaluatedSearchGraphPath<NPuzzleProblem, String, Double>> {

	@Override
	public GraphSearchWithSubpathEvaluationsInput<NPuzzleProblem, String, Double> encodeProblem(final NPuzzleProblem problem) {
		IPathEvaluator<NPuzzleProblem, String, Double> pathEvaluator = p -> new EdgeCountingSolutionEvaluator<NPuzzleProblem, String>()
				.evaluate(new SearchGraphPath<>(p.getNodes(), Arrays.asList(StringUtil.getArrayWithValues(p.getNodes().size() - 1, "")))).doubleValue();
		return new GraphSearchWithSubpathEvaluationsInput<>(new NPuzzleGraphGenerator(problem.getBoard()), new NPuzzleGoalPredicate(), pathEvaluator);
	}

	@Override
	public List<String> decodeSolution(final EvaluatedSearchGraphPath<NPuzzleProblem, String, Double> solution) {
		return solution.getArcs();
	}
}
