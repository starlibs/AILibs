package ai.libs.hasco.variants.forwarddecomposition;


import org.api4.java.ai.graphsearch.problem.IOptimalPathInORGraphSearchFactory;
import org.api4.java.ai.graphsearch.problem.IPathSearchWithPathEvaluationsInput;

import ai.libs.hasco.core.HASCO;
import ai.libs.hasco.core.RefinementConfiguredSoftwareConfigurationProblem;
import ai.libs.jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import ai.libs.jaicore.search.model.other.EvaluatedSearchGraphPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;

public class HASCOViaFDAndBestFirst<V extends Comparable<V>> extends HASCOViaFD<GraphSearchWithSubpathEvaluationsInput<TFDNode, String, V>, V> {

	public HASCOViaFDAndBestFirst(final RefinementConfiguredSoftwareConfigurationProblem<V> configurationProblem,
			final IOptimalPathInORGraphSearchFactory<GraphSearchWithSubpathEvaluationsInput<TFDNode, String, V>, EvaluatedSearchGraphPath<TFDNode, String, V>, TFDNode, String, V, ?> searchFactory,
			final AlgorithmicProblemReduction<IPathSearchWithPathEvaluationsInput<TFDNode, String, V>, EvaluatedSearchGraphPath<TFDNode, String, V>, GraphSearchWithSubpathEvaluationsInput<TFDNode, String, V>, EvaluatedSearchGraphPath<TFDNode, String, V>> searchProblemTransformer) {
		super(configurationProblem, searchFactory, searchProblemTransformer);
	}

	public HASCOViaFDAndBestFirst(final HASCO<GraphSearchWithSubpathEvaluationsInput<TFDNode, String, V>, TFDNode, String, V> hasco) {
		super(hasco.getInput(), hasco.getSearchFactory(), hasco.getSearchProblemTransformer());
	}
}
