package ai.libs.jaicore.search.exampleproblems.enhancedttsp;

import java.util.Random;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPotentiallyUncertaintyAnnotatingPathEvaluator;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IUncertaintySource;
import org.api4.java.common.attributedobjects.IObjectEvaluator;
import org.api4.java.datastructure.graph.ILabeledPath;

import ai.libs.jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import ai.libs.jaicore.problems.enhancedttsp.EnhancedTTSPState;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.nodeevaluation.RandomCompletionBasedNodeEvaluator;
import ai.libs.jaicore.search.model.other.AgnosticPathEvaluator;
import ai.libs.jaicore.search.model.other.EvaluatedSearchGraphPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithUncertaintyBasedSubpathEvaluationInput;

public class EnhancedTTSPGraphSearchToUncertaintyBasedGraphSearchReducer implements AlgorithmicProblemReduction<GraphSearchWithSubpathEvaluationsInput<EnhancedTTSPState, String, Double>, EvaluatedSearchGraphPath<EnhancedTTSPState, String, Double>, GraphSearchWithUncertaintyBasedSubpathEvaluationInput<EnhancedTTSPState, String, Double>, EvaluatedSearchGraphPath<EnhancedTTSPState, String, Double>> {

	private Random random = new Random(0);
	private int samples = 3;
	private IObjectEvaluator<ILabeledPath<EnhancedTTSPState, String>, Double> solutionEvaluator = new AgnosticPathEvaluator<>();
	private IUncertaintySource<EnhancedTTSPState, String, Double> uncertaintySource = (n, simulationPaths, simulationEvaluations) -> 0.5;


	@Override
	public GraphSearchWithUncertaintyBasedSubpathEvaluationInput<EnhancedTTSPState, String, Double> encodeProblem(final GraphSearchWithSubpathEvaluationsInput<EnhancedTTSPState, String, Double> problem) {
		IPotentiallyUncertaintyAnnotatingPathEvaluator<EnhancedTTSPState, String, Double> nodeEvaluator = new RandomCompletionBasedNodeEvaluator<>(this.random, this.samples, this.solutionEvaluator);
		nodeEvaluator.setUncertaintySource(this.uncertaintySource);
		return new GraphSearchWithUncertaintyBasedSubpathEvaluationInput<>(problem, nodeEvaluator);

	}


	@Override
	public EvaluatedSearchGraphPath<EnhancedTTSPState, String, Double> decodeSolution(final EvaluatedSearchGraphPath<EnhancedTTSPState, String, Double> solution) {
		return solution;
	}
}
