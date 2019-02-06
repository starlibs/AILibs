package jaicore.search.algorithms.standard.mcts;

import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.search.core.interfaces.IGraphSearchFactory;
import jaicore.search.model.other.EvaluatedSearchGraphPath;
import jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;
import jaicore.search.testproblems.enhancedttsp.EnhancedTTSP;
import jaicore.search.testproblems.enhancedttsp.EnhancedTTSPNode;
import jaicore.search.testproblems.enhancedttsp.EnhancedTTSPTester;
import jaicore.search.testproblems.enhancedttsp.EnhancedTTSPToGraphSearchProblemInputReducer;

public class MCTSEnhancedTTSPTester extends EnhancedTTSPTester<GraphSearchWithPathEvaluationsInput<EnhancedTTSPNode,String,Double>,EvaluatedSearchGraphPath<EnhancedTTSPNode, String,Double>> {

	@Override
	public AlgorithmProblemTransformer<EnhancedTTSP, GraphSearchWithPathEvaluationsInput<EnhancedTTSPNode, String, Double>> getProblemReducer() {
		return new EnhancedTTSPToGraphSearchProblemInputReducer();
	}

	@Override
	public IGraphSearchFactory<GraphSearchWithPathEvaluationsInput<EnhancedTTSPNode, String, Double>, EvaluatedSearchGraphPath<EnhancedTTSPNode, String,Double>, EnhancedTTSPNode, String> getFactory() {
		return new UCTFactory<EnhancedTTSPNode, String>();
	}
	
}