package jaicore.search.testproblems.enhancedttsp;

import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.search.model.probleminputs.GraphSearchProblemInput;

public class EnhancedTTSPToGraphSearchProblemInputReducer implements AlgorithmProblemTransformer<EnhancedTTSP, GraphSearchProblemInput<EnhancedTTSPNode, String,Double>> {

	@Override
	public GraphSearchProblemInput<EnhancedTTSPNode, String, Double> transform(EnhancedTTSP problem) {
		return new GraphSearchProblemInput<>(problem.getGraphGenerator(), problem.getSolutionEvaluator());
	}
}
