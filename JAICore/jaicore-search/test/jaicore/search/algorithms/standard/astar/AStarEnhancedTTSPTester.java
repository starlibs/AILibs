package jaicore.search.algorithms.standard.astar;

import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.search.core.interfaces.IGraphSearchFactory;
import jaicore.search.model.other.EvaluatedSearchGraphPath;
import jaicore.search.model.probleminputs.NumberBasedAdditiveTraversalTree;
import jaicore.search.model.travesaltree.Node;
import jaicore.search.testproblems.enhancedttsp.EnhancedTTSP;
import jaicore.search.testproblems.enhancedttsp.EnhancedTTSPAdditiveGraphReducer;
import jaicore.search.testproblems.enhancedttsp.EnhancedTTSPNode;
import jaicore.search.testproblems.enhancedttsp.EnhancedTTSPTester;

public class AStarEnhancedTTSPTester extends EnhancedTTSPTester<NumberBasedAdditiveTraversalTree<EnhancedTTSPNode,String>,EvaluatedSearchGraphPath<EnhancedTTSPNode,String,Double>, Node<EnhancedTTSPNode,Double>, String> {
	
	@Override
	public IGraphSearchFactory<NumberBasedAdditiveTraversalTree<EnhancedTTSPNode,String>,EvaluatedSearchGraphPath<EnhancedTTSPNode,String,Double>,EnhancedTTSPNode, String, Double, Node<EnhancedTTSPNode, Double>, String> getFactory() {
		AStarFactory<EnhancedTTSPNode, String> factory = new AStarFactory<>();
		return factory;
	}

	@Override
	public AlgorithmProblemTransformer<EnhancedTTSP, NumberBasedAdditiveTraversalTree<EnhancedTTSPNode,String>> getProblemReducer() {
		return new EnhancedTTSPAdditiveGraphReducer();
	}

}
