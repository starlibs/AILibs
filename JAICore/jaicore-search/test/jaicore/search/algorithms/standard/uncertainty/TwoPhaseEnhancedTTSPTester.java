package jaicore.search.algorithms.standard.uncertainty;

import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.search.algorithms.standard.uncertainty.OversearchAvoidanceConfig.OversearchAvoidanceMode;
import jaicore.search.core.interfaces.IGraphSearchFactory;
import jaicore.search.model.other.EvaluatedSearchGraphPath;
import jaicore.search.model.probleminputs.UncertainlyEvaluatedTraversalTree;
import jaicore.search.model.travesaltree.Node;
import jaicore.search.testproblems.enhancedttsp.EnhancedTTSP;
import jaicore.search.testproblems.enhancedttsp.EnhancedTTSPNode;
import jaicore.search.testproblems.enhancedttsp.EnhancedTTSPTester;
import jaicore.search.testproblems.enhancedttsp.EnhancedTTSPToUncertainlyEvaluatedTravesalTreeReducer;

public class TwoPhaseEnhancedTTSPTester extends EnhancedTTSPTester<UncertainlyEvaluatedTraversalTree<EnhancedTTSPNode, String, Double>, EvaluatedSearchGraphPath<EnhancedTTSPNode, String, Double>, Node<EnhancedTTSPNode,Double>, String> {
	
	@Override
	public IGraphSearchFactory<UncertainlyEvaluatedTraversalTree<EnhancedTTSPNode, String, Double>, EvaluatedSearchGraphPath<EnhancedTTSPNode, String, Double>, EnhancedTTSPNode, String, Double, Node<EnhancedTTSPNode,Double>, String> getFactory() {
		OversearchAvoidanceConfig<EnhancedTTSPNode, Double> config = new OversearchAvoidanceConfig<>(OversearchAvoidanceMode.TWO_PHASE_SELECTION, 0);
		UncertaintyORGraphSearchFactory<EnhancedTTSPNode, String, Double> searchFactory = new UncertaintyORGraphSearchFactory<>();
		searchFactory.setConfig(config);
		return searchFactory;
	}

	@Override
	public AlgorithmProblemTransformer<EnhancedTTSP, UncertainlyEvaluatedTraversalTree<EnhancedTTSPNode, String, Double>> getProblemReducer() {
		return a -> new EnhancedTTSPToUncertainlyEvaluatedTravesalTreeReducer().transform(a);
	}
}