package hasco.test;

import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;
import hasco.core.HASCO;
import hasco.core.RefinementConfiguredSoftwareConfigurationProblem;
import hasco.variants.forwarddecomposition.HASCOViaFDAndBestFirstFactory;

public class HASCOViaFDAndBlindBestFirstTester extends HASCOTester<GraphSearchWithSubpathEvaluationsInput<TFDNode, String, Double>, TFDNode, String> {

	@Override
	public HASCO<GraphSearchWithSubpathEvaluationsInput<TFDNode, String, Double>, TFDNode, String, Double> getAlgorithmForSoftwareConfigurationProblem(final RefinementConfiguredSoftwareConfigurationProblem<Double> problem) {
		HASCOViaFDAndBestFirstFactory<Double> factory = new HASCOViaFDAndBestFirstFactory<>();
		factory.setNodeEvaluator(n -> 0.0);
		factory.withDefaultAlgorithmConfig();
		return factory.getAlgorithm(problem);
	}
}
