package hasco.test;

import hasco.core.HASCOFactory;
import hasco.variants.HASCOViaFDAndBestFirstWithRandomCompletionsFactory;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.search.model.probleminputs.GeneralEvaluatedTraversalTree;

public class HASCOViaFDAndBestFirstWithRandomCompletionsTester extends HASCOTester<GeneralEvaluatedTraversalTree<TFDNode, String, Double>, TFDNode, String> {

	@Override
	public HASCOFactory<GeneralEvaluatedTraversalTree<TFDNode, String, Double>, TFDNode, String, Double> getFactory() {
		return new HASCOViaFDAndBestFirstWithRandomCompletionsFactory();
	}

}
