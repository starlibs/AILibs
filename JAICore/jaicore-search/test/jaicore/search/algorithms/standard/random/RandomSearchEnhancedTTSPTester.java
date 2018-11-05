package jaicore.search.algorithms.standard.random;

import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.search.core.interfaces.IGraphSearchFactory;
import jaicore.search.model.probleminputs.GraphSearchInput;
import jaicore.search.testproblems.enhancedttsp.EnhancedTTSP;
import jaicore.search.testproblems.enhancedttsp.EnhancedTTSPNode;
import jaicore.search.testproblems.enhancedttsp.EnhancedTTSPTester;

public class RandomSearchEnhancedTTSPTester extends EnhancedTTSPTester<GraphSearchInput<EnhancedTTSPNode, String>, Object, EnhancedTTSPNode, String> {

	

	@Override
	public IGraphSearchFactory<GraphSearchInput<EnhancedTTSPNode, String>, Object, EnhancedTTSPNode, String, Double, EnhancedTTSPNode, String> getFactory() {
		return new RandomSearchFactory<>();
	}

	@Override
	public AlgorithmProblemTransformer<EnhancedTTSP, GraphSearchInput<EnhancedTTSPNode, String>> getProblemReducer() {
		return new AlgorithmProblemTransformer<EnhancedTTSP, GraphSearchInput<EnhancedTTSPNode,String>>() {

			@Override
			public GraphSearchInput<EnhancedTTSPNode, String> transform(EnhancedTTSP problem) {
				return new GraphSearchInput<>(problem.getGraphGenerator());
			}
		};
	}

}
