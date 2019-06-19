package ai.libs.hasco.test;

import ai.libs.hasco.core.HASCO;
import ai.libs.hasco.core.HASCOFactory;
import ai.libs.hasco.core.RefinementConfiguredSoftwareConfigurationProblem;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.SimpleForwardDecompositionReducer;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.StandardBestFirstFactory;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;
import ai.libs.jaicore.search.problemtransformers.GraphSearchProblemInputToGraphSearchWithSubpathEvaluationViaUninformedness;

public class HASCOViaFDAndBestFirstWithRandomCompletionsTester extends HASCOTester<GraphSearchWithSubpathEvaluationsInput<TFDNode, String, Double>, TFDNode, String> {

	@Override
	public HASCO<GraphSearchWithSubpathEvaluationsInput<TFDNode, String, Double>, TFDNode, String, Double> getAlgorithmForSoftwareConfigurationProblem(final RefinementConfiguredSoftwareConfigurationProblem<Double> problem) {
		HASCOFactory<GraphSearchWithSubpathEvaluationsInput<TFDNode, String, Double>, TFDNode, String, Double> factory = new HASCOFactory<>();
		factory.setPlanningGraphGeneratorDeriver(new SimpleForwardDecompositionReducer());
		factory.setSearchFactory(new StandardBestFirstFactory<>());
		factory.setSearchProblemTransformer(new GraphSearchProblemInputToGraphSearchWithSubpathEvaluationViaUninformedness<>());
		factory.withDefaultAlgorithmConfig();
		return factory.getAlgorithm(problem);
	}
}
