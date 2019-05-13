package ai.libs.hasco.variants.forwarddecomposition;

import ai.libs.hasco.core.DefaultHASCOPlanningGraphGeneratorDeriver;
import ai.libs.hasco.core.HASCO;
import ai.libs.hasco.core.RefinementConfiguredSoftwareConfigurationProblem;
import ai.libs.jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.ForwardDecompositionReducer;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import ai.libs.jaicore.search.core.interfaces.IOptimalPathInORGraphSearchFactory;
import ai.libs.jaicore.search.model.other.EvaluatedSearchGraphPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchInput;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;

public class HASCOViaFD<ISearch extends GraphSearchInput<TFDNode, String>, V extends Comparable<V>> extends HASCO<ISearch, TFDNode, String, V> {

	public HASCOViaFD(final RefinementConfiguredSoftwareConfigurationProblem<V> configurationProblem,
			final IOptimalPathInORGraphSearchFactory<ISearch, TFDNode, String, V> searchFactory, final AlgorithmicProblemReduction<GraphSearchWithPathEvaluationsInput<TFDNode, String, V>, EvaluatedSearchGraphPath<TFDNode, String, V>, ISearch, EvaluatedSearchGraphPath<TFDNode, String, V>> searchProblemTransformer) {
		super(configurationProblem, new DefaultHASCOPlanningGraphGeneratorDeriver<>(new ForwardDecompositionReducer<>()), searchFactory, searchProblemTransformer);
	}

}
