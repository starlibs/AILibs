package hasco.variants.forwarddecomposition;

import hasco.core.DefaultHASCOPlanningGraphGeneratorDeriver;
import hasco.core.HASCO;
import hasco.core.RefinementConfiguredSoftwareConfigurationProblem;
import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.planning.hierarchical.algorithms.forwarddecomposition.ForwardDecompositionReducer;
import jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import jaicore.search.core.interfaces.IOptimalPathInORGraphSearchFactory;
import jaicore.search.probleminputs.GraphSearchInput;
import jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;

public class HASCOViaFD<ISearch extends GraphSearchInput<TFDNode, String>, V extends Comparable<V>> extends HASCO<ISearch, TFDNode, String, V> {

	public HASCOViaFD(RefinementConfiguredSoftwareConfigurationProblem<V> configurationProblem,
			IOptimalPathInORGraphSearchFactory<ISearch, TFDNode, String, V> searchFactory, AlgorithmProblemTransformer<GraphSearchWithPathEvaluationsInput<TFDNode, String, V>, ISearch> searchProblemTransformer) {
		super(configurationProblem, new DefaultHASCOPlanningGraphGeneratorDeriver<>(new ForwardDecompositionReducer<>()), searchFactory, searchProblemTransformer);
	}
	
}
