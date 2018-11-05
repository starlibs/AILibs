package jaicore.search.algorithms.standard.bestfirst;

import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.search.core.interfaces.IGraphSearchFactory;
import jaicore.search.model.other.EvaluatedSearchGraphPath;
import jaicore.search.model.probleminputs.GeneralEvaluatedTraversalTree;
import jaicore.search.model.travesaltree.Node;
import jaicore.search.testproblems.enhancedttsp.EnhancedTTSP;
import jaicore.search.testproblems.enhancedttsp.EnhancedTTSPNode;
import jaicore.search.testproblems.enhancedttsp.EnhancedTTSPTester;

public class BestFirstEnhancedTTSPTester extends EnhancedTTSPTester<GeneralEvaluatedTraversalTree<EnhancedTTSPNode, String, Double>, EvaluatedSearchGraphPath<EnhancedTTSPNode, String, Double>, Node<EnhancedTTSPNode,Double>, String> {
	
	@Override
	public IGraphSearchFactory<GeneralEvaluatedTraversalTree<EnhancedTTSPNode, String, Double>, EvaluatedSearchGraphPath<EnhancedTTSPNode, String, Double>, EnhancedTTSPNode, String, Double, Node<EnhancedTTSPNode,Double>, String> getFactory() {
		return new BestFirstFactory<>();
	}

	@Override
	public AlgorithmProblemTransformer<EnhancedTTSP, GeneralEvaluatedTraversalTree<EnhancedTTSPNode, String, Double>> getProblemReducer() {
		return a -> new GeneralEvaluatedTraversalTree<>(a.getGraphGenerator(), n -> a.getSolutionEvaluator().evaluateSolution(n.externalPath()));
	}
}