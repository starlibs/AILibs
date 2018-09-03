package jaicore.search.algorithms.standard.mcts;

import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.search.core.interfaces.IGraphSearchFactory;
import jaicore.search.model.probleminputs.GraphSearchProblemInput;
import jaicore.search.model.travesaltree.Node;
import jaicore.search.testproblems.enhancedttsp.EnhancedTTSP;
import jaicore.search.testproblems.enhancedttsp.EnhancedTTSPNode;
import jaicore.search.testproblems.enhancedttsp.EnhancedTTSPTester;
import jaicore.search.testproblems.enhancedttsp.EnhancedTTSPToGraphSearchProblemInputReducer;

public class MCTSEnhancedTTSPTester extends EnhancedTTSPTester<GraphSearchProblemInput<EnhancedTTSPNode,String,Double>,Object,Node<EnhancedTTSPNode,Double>, String> {

	@Override
	public AlgorithmProblemTransformer<EnhancedTTSP, GraphSearchProblemInput<EnhancedTTSPNode, String, Double>> getProblemReducer() {
		return new EnhancedTTSPToGraphSearchProblemInputReducer();
	}

	@Override
	public IGraphSearchFactory<GraphSearchProblemInput<EnhancedTTSPNode, String, Double>, Object, EnhancedTTSPNode, String, Double, Node<EnhancedTTSPNode, Double>, String> getFactory() {
		return new UCTFactory<>();
	}
	
}