package ai.libs.hasco.test;

import ai.libs.hasco.core.HASCO;
import ai.libs.hasco.core.RefinementConfiguredSoftwareConfigurationProblem;
import ai.libs.hasco.variants.forwarddecomposition.HASCOViaFDAndBestFirstWithRandomCompletionsFactory;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;

public class HASCOViaFDAndBestFirstWithRandomCompletionsTester extends HASCOTester<GraphSearchWithSubpathEvaluationsInput<TFDNode, String, Double>, TFDNode, String> {

	@Override
	public HASCO<GraphSearchWithSubpathEvaluationsInput<TFDNode, String, Double>, TFDNode, String, Double> getAlgorithmForSoftwareConfigurationProblem(final RefinementConfiguredSoftwareConfigurationProblem<Double> problem) {
		HASCOViaFDAndBestFirstWithRandomCompletionsFactory factory = new HASCOViaFDAndBestFirstWithRandomCompletionsFactory(0, 3);
		factory.withDefaultAlgorithmConfig();
		return factory.getAlgorithm(problem);
	}
}
