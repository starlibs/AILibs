package jaicore.search.algorithms.standard.bestfirst;

import java.util.concurrent.TimeoutException;

import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.basic.algorithm.exceptions.ObjectEvaluationFailedException;
import jaicore.search.algorithms.standard.bestfirst.exceptions.NodeEvaluationException;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.core.interfaces.IGraphSearchFactory;
import jaicore.search.model.other.EvaluatedSearchGraphPath;
import jaicore.search.model.travesaltree.Node;
import jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;
import jaicore.search.testproblems.enhancedttsp.EnhancedTTSP;
import jaicore.search.testproblems.enhancedttsp.EnhancedTTSPNode;
import jaicore.search.testproblems.enhancedttsp.EnhancedTTSPTester;

public class BestFirstEnhancedTTSPTester extends EnhancedTTSPTester<GraphSearchWithSubpathEvaluationsInput<EnhancedTTSPNode, String, Double>, EvaluatedSearchGraphPath<EnhancedTTSPNode, String, Double>> {
	
	@Override
	public IGraphSearchFactory<GraphSearchWithSubpathEvaluationsInput<EnhancedTTSPNode, String, Double>, EvaluatedSearchGraphPath<EnhancedTTSPNode, String, Double>, EnhancedTTSPNode, String> getFactory() {
		return new BestFirstFactory<>();
	}

	@Override
	public AlgorithmProblemTransformer<EnhancedTTSP, GraphSearchWithSubpathEvaluationsInput<EnhancedTTSPNode, String, Double>> getProblemReducer() {
		return a -> new GraphSearchWithSubpathEvaluationsInput<>(a.getGraphGenerator(), new INodeEvaluator<EnhancedTTSPNode, Double>() {

			@Override
			public Double f(Node<EnhancedTTSPNode, ?> node) throws NodeEvaluationException, TimeoutException, AlgorithmExecutionCanceledException, InterruptedException {
				try {
					return a.getSolutionEvaluator().evaluateSolution(node.externalPath());
				} catch (ObjectEvaluationFailedException e) {
					throw new NodeEvaluationException(e, "Could not evaluate node. " + e.getMessage());
				}
			}
		});
	}
}