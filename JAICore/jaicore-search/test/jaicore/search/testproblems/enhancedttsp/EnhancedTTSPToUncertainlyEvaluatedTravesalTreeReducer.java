package jaicore.search.testproblems.enhancedttsp;

import java.util.Random;

import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.IUncertaintyAnnotatingNodeEvaluator;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.RandomCompletionBasedNodeEvaluator;
import jaicore.search.algorithms.standard.uncertainty.IUncertaintySource;
import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.core.interfaces.ISolutionEvaluator;
import jaicore.search.model.other.AgnosticPathEvaluator;
import jaicore.search.model.probleminputs.UncertainlyEvaluatedTraversalTree;

public class EnhancedTTSPToUncertainlyEvaluatedTravesalTreeReducer implements AlgorithmProblemTransformer<EnhancedTTSP, UncertainlyEvaluatedTraversalTree<EnhancedTTSPNode, String, Double>> {
	
	private Random random = new Random(0);
	private int samples = 3;
	private ISolutionEvaluator<EnhancedTTSPNode, Double> solutionEvaluator = new AgnosticPathEvaluator<>();
	private IUncertaintySource<EnhancedTTSPNode, Double> uncertaintySource = (n, simulationPaths, simulationEvaluations) -> 0.5;
	
	
	@Override
	public UncertainlyEvaluatedTraversalTree<EnhancedTTSPNode, String, Double> transform(EnhancedTTSP problem) {
		GraphGenerator<EnhancedTTSPNode, String> graphGenerator = problem.getGraphGenerator();
		IUncertaintyAnnotatingNodeEvaluator<EnhancedTTSPNode, Double> nodeEvaluator = new RandomCompletionBasedNodeEvaluator<>(random, samples, solutionEvaluator);
		nodeEvaluator.setUncertaintySource(uncertaintySource);
		return new UncertainlyEvaluatedTraversalTree<>(graphGenerator, nodeEvaluator);

	}
}
