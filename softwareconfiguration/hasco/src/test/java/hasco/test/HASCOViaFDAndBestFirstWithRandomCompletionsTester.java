package hasco.test;

import hasco.core.HASCOFactory;
import hasco.variants.forwarddecomposition.HASCOViaFDAndBestFirstWithRandomCompletionsFactory;
import jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;

public class HASCOViaFDAndBestFirstWithRandomCompletionsTester extends HASCOTester<GraphSearchWithSubpathEvaluationsInput<TFDNode, String, Double>, TFDNode, String> {

	@Override
	public HASCOFactory<GraphSearchWithSubpathEvaluationsInput<TFDNode, String, Double>, TFDNode, String, Double> getFactory() {
		return new HASCOViaFDAndBestFirstWithRandomCompletionsFactory(0, 3);
	}
}
