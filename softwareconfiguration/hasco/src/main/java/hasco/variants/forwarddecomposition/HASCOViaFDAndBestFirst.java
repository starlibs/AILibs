package hasco.variants.forwarddecomposition;


import hasco.core.HASCO;
import hasco.core.RefinementConfiguredSoftwareConfigurationProblem;
import jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import jaicore.search.core.interfaces.IOptimalPathInORGraphSearchFactory;
import jaicore.search.model.other.EvaluatedSearchGraphPath;
import jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;
import jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;

public class HASCOViaFDAndBestFirst<V extends Comparable<V>> extends HASCOViaFD<GraphSearchWithSubpathEvaluationsInput<TFDNode, String, V>, V> {

	public HASCOViaFDAndBestFirst(final RefinementConfiguredSoftwareConfigurationProblem<V> configurationProblem,
			final IOptimalPathInORGraphSearchFactory<GraphSearchWithSubpathEvaluationsInput<TFDNode, String, V>, TFDNode, String, V> searchFactory,
			final AlgorithmicProblemReduction<GraphSearchWithPathEvaluationsInput<TFDNode, String, V>, EvaluatedSearchGraphPath<TFDNode, String, V>, GraphSearchWithSubpathEvaluationsInput<TFDNode, String, V>, EvaluatedSearchGraphPath<TFDNode, String, V>> searchProblemTransformer) {
		super(configurationProblem, searchFactory, searchProblemTransformer);
	}

	public HASCOViaFDAndBestFirst(final HASCO<GraphSearchWithSubpathEvaluationsInput<TFDNode, String, V>, TFDNode, String, V> hasco) {
		super(hasco.getInput(), hasco.getSearchFactory(), hasco.getSearchProblemTransformer());
	}
}
