package ai.libs.jaicore.search.testproblems.enhancedttsp;

import ai.libs.jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import ai.libs.jaicore.search.model.other.EvaluatedSearchGraphPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithNumberBasedAdditivePathEvaluation;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;
import ai.libs.jaicore.testproblems.enhancedttsp.EnhancedTTSPNode;

public class EnhancedTTSPGraphSearchToAdditiveGraphSearchReducer implements AlgorithmicProblemReduction<GraphSearchWithSubpathEvaluationsInput<EnhancedTTSPNode, String, Double>, EvaluatedSearchGraphPath<EnhancedTTSPNode, String, Double>, GraphSearchWithNumberBasedAdditivePathEvaluation<EnhancedTTSPNode, String>, EvaluatedSearchGraphPath<EnhancedTTSPNode, String, Double>> {

	@Override
	public GraphSearchWithNumberBasedAdditivePathEvaluation<EnhancedTTSPNode, String> encodeProblem(final GraphSearchWithSubpathEvaluationsInput<EnhancedTTSPNode, String, Double> problem) {
		return new GraphSearchWithNumberBasedAdditivePathEvaluation<>(problem.getGraphGenerator(), (from, to) -> to.getPoint().getTime() - from.getPoint().getTime(), node -> 0.0);
	}

	@Override
	public EvaluatedSearchGraphPath<EnhancedTTSPNode, String, Double> decodeSolution(final EvaluatedSearchGraphPath<EnhancedTTSPNode, String, Double> solution) {
		return solution;
	}
}
