package hasco.variants.forwarddecomposition;


import hasco.core.HASCO;
import hasco.core.RefinementConfiguredSoftwareConfigurationProblem;
import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
//github.com/fmohr/AILibs.git
import jaicore.search.core.interfaces.IOptimalPathInORGraphSearchFactory;
import jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;
import jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;

public class HASCOViaFDAndBestFirst<V extends Comparable<V>> extends HASCOViaFD<GraphSearchWithSubpathEvaluationsInput<TFDNode, String, V>, V> {

	public HASCOViaFDAndBestFirst(RefinementConfiguredSoftwareConfigurationProblem<V> configurationProblem,
			IOptimalPathInORGraphSearchFactory<GraphSearchWithSubpathEvaluationsInput<TFDNode, String, V>, TFDNode, String, V> searchFactory,
			AlgorithmProblemTransformer<GraphSearchWithPathEvaluationsInput<TFDNode, String, V>, GraphSearchWithSubpathEvaluationsInput<TFDNode, String, V>> searchProblemTransformer) {
		super(configurationProblem, searchFactory, searchProblemTransformer);
	}
	
	public HASCOViaFDAndBestFirst(HASCO<GraphSearchWithSubpathEvaluationsInput<TFDNode, String, V>, TFDNode, String, V> hasco) {
		super(hasco.getInput(), hasco.getSearchFactory(), hasco.getSearchProblemTransformer());
	}
}
