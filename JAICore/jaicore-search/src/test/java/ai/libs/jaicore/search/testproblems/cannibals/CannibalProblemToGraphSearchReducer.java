package ai.libs.jaicore.search.testproblems.cannibals;

import java.util.List;

import ai.libs.jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import ai.libs.jaicore.testproblems.cannibals.CannibalProblem;
import jaicore.search.model.other.SearchGraphPath;
import jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;

public class CannibalProblemToGraphSearchReducer implements AlgorithmicProblemReduction<CannibalProblem, List<String>, GraphSearchWithPathEvaluationsInput<CannibalProblem, String, Integer>, SearchGraphPath<CannibalProblem, String>> {

	@Override
	public GraphSearchWithPathEvaluationsInput<CannibalProblem, String, Integer> encodeProblem(final CannibalProblem problem) {
		return new GraphSearchWithPathEvaluationsInput<>(new CannibalGraphGenerator(problem), p -> p.getEdges().size());
	}

	@Override
	public List<String> decodeSolution(final SearchGraphPath<CannibalProblem, String> solution) {
		return solution.getEdges();
	}

}
