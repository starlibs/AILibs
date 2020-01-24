package ai.libs.hasco.variants.forwarddecomposition;

import org.api4.java.ai.graphsearch.problem.IOptimalPathInORGraphSearchFactory;

import ai.libs.hasco.core.DefaultHASCOPlanningReduction;
import ai.libs.hasco.core.HASCO;
import ai.libs.hasco.core.RefinementConfiguredSoftwareConfigurationProblem;
import ai.libs.jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.SimpleForwardDecompositionReducer;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import ai.libs.jaicore.search.model.other.EvaluatedSearchGraphPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;

public class HASCOViaFD<I extends GraphSearchWithPathEvaluationsInput<TFDNode, String, V>, V extends Comparable<V>> extends HASCO<I, TFDNode, String, V> {

	public HASCOViaFD(final RefinementConfiguredSoftwareConfigurationProblem<V> configurationProblem, final IOptimalPathInORGraphSearchFactory<I, EvaluatedSearchGraphPath<TFDNode, String, V>, TFDNode, String, V, ?> searchFactory,
			final AlgorithmicProblemReduction<? super GraphSearchWithPathEvaluationsInput<TFDNode, String, V>, ? super EvaluatedSearchGraphPath<TFDNode, String, V>, I, EvaluatedSearchGraphPath<TFDNode, String, V>> searchProblemTransformer) {
		super(configurationProblem, new DefaultHASCOPlanningReduction<>(new SimpleForwardDecompositionReducer()), searchFactory, searchProblemTransformer);
	}

}
