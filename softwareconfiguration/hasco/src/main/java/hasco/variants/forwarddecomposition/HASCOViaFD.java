package hasco.variants.forwarddecomposition;

import hasco.core.DefaultHASCOPlanningGraphGeneratorDeriver;
import hasco.core.HASCO;
import hasco.core.RefinementConfiguredSoftwareConfigurationProblem;
import jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import jaicore.planning.hierarchical.algorithms.forwarddecomposition.ForwardDecompositionReducer;
import jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import jaicore.search.core.interfaces.IOptimalPathInORGraphSearchFactory;
import jaicore.search.model.other.EvaluatedSearchGraphPath;
import jaicore.search.probleminputs.GraphSearchInput;
import jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;

public class HASCOViaFD<ISearch extends GraphSearchInput<TFDNode, String>, V extends Comparable<V>> extends HASCO<ISearch, TFDNode, String, V> {

	public HASCOViaFD(final RefinementConfiguredSoftwareConfigurationProblem<V> configurationProblem,
			final IOptimalPathInORGraphSearchFactory<ISearch, TFDNode, String, V> searchFactory, final AlgorithmicProblemReduction<GraphSearchWithPathEvaluationsInput<TFDNode, String, V>, EvaluatedSearchGraphPath<TFDNode, String, V>, ISearch, EvaluatedSearchGraphPath<TFDNode, String, V>> searchProblemTransformer) {
		super(configurationProblem, new DefaultHASCOPlanningGraphGeneratorDeriver<>(new ForwardDecompositionReducer<>()), searchFactory, searchProblemTransformer);
	}

}
