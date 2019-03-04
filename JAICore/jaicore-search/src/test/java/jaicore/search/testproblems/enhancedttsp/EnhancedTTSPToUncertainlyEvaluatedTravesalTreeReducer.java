package jaicore.search.testproblems.enhancedttsp;

import java.util.Random;

import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.IUncertaintyAnnotatingNodeEvaluator;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.RandomCompletionBasedNodeEvaluator;
import jaicore.search.algorithms.standard.uncertainty.IUncertaintySource;
import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.core.interfaces.ISolutionEvaluator;
import jaicore.search.model.other.AgnosticPathEvaluator;
import jaicore.search.probleminputs.GraphSearchWithUncertaintyBasedSubpathEvaluationInput;

public class EnhancedTTSPToUncertainlyEvaluatedTravesalTreeReducer implements AlgorithmProblemTransformer<EnhancedTTSP, GraphSearchWithUncertaintyBasedSubpathEvaluationInput<EnhancedTTSPNode, String, Double>> {

	private Random random = new Random(0);
	private int samples = 3;
	private ISolutionEvaluator<EnhancedTTSPNode, Double> solutionEvaluator = new AgnosticPathEvaluator<>();
	private IUncertaintySource<EnhancedTTSPNode, Double> uncertaintySource = (n, simulationPaths, simulationEvaluations) -> 0.5;


	@Override
	public GraphSearchWithUncertaintyBasedSubpathEvaluationInput<EnhancedTTSPNode, String, Double> transform(final EnhancedTTSP problem) {
		GraphGenerator<EnhancedTTSPNode, String> graphGenerator = new EnhancedTTSPGraphGenerator(problem);
		IUncertaintyAnnotatingNodeEvaluator<EnhancedTTSPNode, Double> nodeEvaluator = new RandomCompletionBasedNodeEvaluator<>(this.random, this.samples, this.solutionEvaluator);
		nodeEvaluator.setUncertaintySource(this.uncertaintySource);
		return new GraphSearchWithUncertaintyBasedSubpathEvaluationInput<>(graphGenerator, nodeEvaluator);

	}
}
