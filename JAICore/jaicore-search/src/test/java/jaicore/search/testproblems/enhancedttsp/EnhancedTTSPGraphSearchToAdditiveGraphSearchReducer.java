package jaicore.search.testproblems.enhancedttsp;

import jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import jaicore.search.model.other.EvaluatedSearchGraphPath;
import jaicore.search.probleminputs.GraphSearchWithNumberBasedAdditivePathEvaluation;
import jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;
import jaicore.testproblems.enhancedttsp.EnhancedTTSPNode;

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
