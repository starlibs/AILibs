package jaicore.search.testproblems.cannibals;

import java.util.List;

import jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import jaicore.search.model.other.SearchGraphPath;
import jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;
import jaicore.testproblems.cannibals.CannibalProblem;

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
