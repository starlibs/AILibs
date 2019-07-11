package ai.libs.jaicore.search.testproblems.enhancedttsp;

import java.util.Random;

import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.IPath;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPotentiallyUncertaintyAnnotatingPathEvaluator;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IUncertaintySource;
import org.api4.java.common.attributedobjects.IObjectEvaluator;

import ai.libs.jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.nodeevaluation.RandomCompletionBasedNodeEvaluator;
import ai.libs.jaicore.search.model.other.AgnosticPathEvaluator;
import ai.libs.jaicore.search.model.other.EvaluatedSearchGraphPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithUncertaintyBasedSubpathEvaluationInput;
import ai.libs.jaicore.testproblems.enhancedttsp.EnhancedTTSPNode;

public class EnhancedTTSPGraphSearchToUncertaintyBasedGraphSearchReducer implements AlgorithmicProblemReduction<GraphSearchWithSubpathEvaluationsInput<EnhancedTTSPNode, String, Double>, EvaluatedSearchGraphPath<EnhancedTTSPNode, String, Double>, GraphSearchWithUncertaintyBasedSubpathEvaluationInput<EnhancedTTSPNode, String, Double>, EvaluatedSearchGraphPath<EnhancedTTSPNode, String, Double>> {

	private Random random = new Random(0);
	private int samples = 3;
	private IObjectEvaluator<IPath<EnhancedTTSPNode, String>, Double> solutionEvaluator = new AgnosticPathEvaluator<>();
	private IUncertaintySource<EnhancedTTSPNode, String, Double> uncertaintySource = (n, simulationPaths, simulationEvaluations) -> 0.5;


	@Override
	public GraphSearchWithUncertaintyBasedSubpathEvaluationInput<EnhancedTTSPNode, String, Double> encodeProblem(final GraphSearchWithSubpathEvaluationsInput<EnhancedTTSPNode, String, Double> problem) {
		IPotentiallyUncertaintyAnnotatingPathEvaluator<EnhancedTTSPNode, String, Double> nodeEvaluator = new RandomCompletionBasedNodeEvaluator<>(this.random, this.samples, this.solutionEvaluator);
		nodeEvaluator.setUncertaintySource(this.uncertaintySource);
		return new GraphSearchWithUncertaintyBasedSubpathEvaluationInput<>(problem.getGraphGenerator(), nodeEvaluator);

	}


	@Override
	public EvaluatedSearchGraphPath<EnhancedTTSPNode, String, Double> decodeSolution(final EvaluatedSearchGraphPath<EnhancedTTSPNode, String, Double> solution) {
		return solution;
	}
}
