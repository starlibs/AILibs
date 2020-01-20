package ai.libs.jaicore.search.exampleproblems.cannibals;

import java.util.List;

import ai.libs.jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import ai.libs.jaicore.problems.cannibals.CannibalProblem;
import ai.libs.jaicore.search.model.other.SearchGraphPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;

public class CannibalProblemToGraphSearchReducer implements AlgorithmicProblemReduction<CannibalProblem, List<String>, GraphSearchWithPathEvaluationsInput<CannibalProblem, String, Integer>, SearchGraphPath<CannibalProblem, String>> {

	@Override
	public GraphSearchWithPathEvaluationsInput<CannibalProblem, String, Integer> encodeProblem(final CannibalProblem problem) {
		return new GraphSearchWithPathEvaluationsInput<>(new CannibalGraphGenerator(problem), new CannibalNodeGoalPredicate(), p -> p.getArcs().size());
	}

	@Override
	public List<String> decodeSolution(final SearchGraphPath<CannibalProblem, String> solution) {
		return solution.getArcs();
	}

}
