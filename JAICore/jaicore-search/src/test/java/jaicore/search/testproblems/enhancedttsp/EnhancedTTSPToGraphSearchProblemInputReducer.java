package jaicore.search.testproblems.enhancedttsp;

import jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;

public class EnhancedTTSPToGraphSearchProblemInputReducer implements AlgorithmicProblemReduction<EnhancedTTSP, GraphSearchWithPathEvaluationsInput<EnhancedTTSPNode, String,Double>> {

	@Override
	public GraphSearchWithPathEvaluationsInput<EnhancedTTSPNode, String, Double> transform(final EnhancedTTSP problem) {
		return new GraphSearchWithPathEvaluationsInput<>(new EnhancedTTSPGraphGenerator(problem), problem.getSolutionEvaluator());
	}
}
