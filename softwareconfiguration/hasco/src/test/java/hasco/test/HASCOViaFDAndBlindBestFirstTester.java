package hasco.test;

import hasco.core.HASCO;
import hasco.core.RefinementConfiguredSoftwareConfigurationProblem;
import hasco.variants.forwarddecomposition.HASCOViaFDAndBestFirstFactory;
import jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;

public class HASCOViaFDAndBlindBestFirstTester extends HASCOTester<GraphSearchWithSubpathEvaluationsInput<TFDNode, String, Double>, TFDNode, String> {

	@Override
	public HASCO<GraphSearchWithSubpathEvaluationsInput<TFDNode, String, Double>, TFDNode, String, Double> getAlgorithmForSoftwareConfigurationProblem(final RefinementConfiguredSoftwareConfigurationProblem<Double> problem) {
		HASCOViaFDAndBestFirstFactory<Double> factory = new HASCOViaFDAndBestFirstFactory<>();
		factory.setNodeEvaluator(n -> 0.0);
		return factory.getAlgorithm(problem);
	}
}
