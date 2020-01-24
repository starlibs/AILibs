package ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition;

import org.api4.java.ai.graphsearch.problem.IOptimalPathInORGraphSearchFactory;

import ai.libs.jaicore.planning.core.interfaces.IEvaluatedGraphSearchBasedPlan;
import ai.libs.jaicore.planning.hierarchical.algorithms.CostSensitiveGraphSearchBasedHTNPlanningAlgorithm;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import ai.libs.jaicore.planning.hierarchical.problems.htn.IHTNPlanningProblem;
import ai.libs.jaicore.search.model.other.EvaluatedSearchGraphPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchInput;

/**
 * @author fmohr
 *
 */
public class ForwardDecompositionHTNPlanner<P extends IHTNPlanningProblem, V extends Comparable<V>, S extends GraphSearchInput<TFDNode, String>> extends CostSensitiveGraphSearchBasedHTNPlanningAlgorithm<P, S, TFDNode, String, V> {

	public ForwardDecompositionHTNPlanner(final P problem, final AForwardDecompositionReducer<P, IEvaluatedGraphSearchBasedPlan<TFDNode, String, V>, S, EvaluatedSearchGraphPath<TFDNode, String, V>> reducer,
			final IOptimalPathInORGraphSearchFactory<S, EvaluatedSearchGraphPath<TFDNode, String, V>, TFDNode, String, V, ?> searchFactory) {
		super(problem, reducer, searchFactory);
	}
}
