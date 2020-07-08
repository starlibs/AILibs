package ai.libs.hasco.builder.forwarddecomposition;

import org.api4.java.ai.graphsearch.problem.IOptimalPathInORGraphSearchFactory;
import org.api4.java.ai.graphsearch.problem.IPathSearchWithPathEvaluationsInput;

import ai.libs.hasco.core.HASCO;
import ai.libs.hasco.core.reduction.planning2search.DefaultHASCOPlanningReduction;
import ai.libs.jaicore.components.model.RefinementConfiguredSoftwareConfigurationProblem;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.SimpleForwardDecompositionReducer;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import ai.libs.jaicore.search.model.other.EvaluatedSearchGraphPath;

public class HASCOViaFD<V extends Comparable<V>> extends HASCO<TFDNode, String, V> {

	public HASCOViaFD(final RefinementConfiguredSoftwareConfigurationProblem<V> configurationProblem, final IOptimalPathInORGraphSearchFactory<IPathSearchWithPathEvaluationsInput<TFDNode, String, V>, EvaluatedSearchGraphPath<TFDNode, String, V>, TFDNode, String, V, ?> searchFactory) {
		super(configurationProblem, new DefaultHASCOPlanningReduction<>(new SimpleForwardDecompositionReducer()), searchFactory);
	}

}
