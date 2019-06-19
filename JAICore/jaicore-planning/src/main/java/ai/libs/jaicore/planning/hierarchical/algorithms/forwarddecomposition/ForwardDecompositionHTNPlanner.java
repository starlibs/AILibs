package ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition;

import ai.libs.jaicore.planning.core.EvaluatedSearchGraphBasedPlan;
import ai.libs.jaicore.planning.hierarchical.algorithms.GraphSearchBasedHTNPlanningAlgorithm;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import ai.libs.jaicore.planning.hierarchical.problems.htn.IHTNPlanningProblem;
import ai.libs.jaicore.search.core.interfaces.IOptimalPathInORGraphSearchFactory;
import ai.libs.jaicore.search.model.other.EvaluatedSearchGraphPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchInput;

/**
 * @author fmohr
 *
 */
public class ForwardDecompositionHTNPlanner<P extends IHTNPlanningProblem, V extends Comparable<V>, S extends GraphSearchInput<TFDNode, String>>
extends GraphSearchBasedHTNPlanningAlgorithm<P, S, TFDNode, String, V> {

	public ForwardDecompositionHTNPlanner(final P problem, final AForwardDecompositionReducer<P, EvaluatedSearchGraphBasedPlan<V, TFDNode>, S, EvaluatedSearchGraphPath<TFDNode, String, V>> reducer, final IOptimalPathInORGraphSearchFactory<S, TFDNode, String, V> searchFactory) {
		super(problem, reducer, searchFactory);
	}
}
