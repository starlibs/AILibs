package ai.libs.jaicore.search.exampleproblems.enhancedttsp;

import ai.libs.jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import ai.libs.jaicore.problems.enhancedttsp.EnhancedTTSPState;
import ai.libs.jaicore.search.model.other.EvaluatedSearchGraphPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithNumberBasedAdditivePathEvaluation;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;

public class EnhancedTTSPGraphSearchToAdditiveGraphSearchReducer implements AlgorithmicProblemReduction<GraphSearchWithSubpathEvaluationsInput<EnhancedTTSPState, String, Double>, EvaluatedSearchGraphPath<EnhancedTTSPState, String, Double>, GraphSearchWithNumberBasedAdditivePathEvaluation<EnhancedTTSPState, String>, EvaluatedSearchGraphPath<EnhancedTTSPState, String, Double>> {

	@Override
	public GraphSearchWithNumberBasedAdditivePathEvaluation<EnhancedTTSPState, String> encodeProblem(final GraphSearchWithSubpathEvaluationsInput<EnhancedTTSPState, String, Double> problem) {
		return new GraphSearchWithNumberBasedAdditivePathEvaluation<>(problem, (from, to) -> to.getHead().getTime() - from.getHead().getTime(), node -> 0.0);
	}

	@Override
	public EvaluatedSearchGraphPath<EnhancedTTSPState, String, Double> decodeSolution(final EvaluatedSearchGraphPath<EnhancedTTSPState, String, Double> solution) {
		return solution;
	}
}
