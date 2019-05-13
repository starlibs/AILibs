package ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition;

import ai.libs.jaicore.planning.hierarchical.algorithms.GraphSearchBasedHTNPlanningAlgorithm;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import ai.libs.jaicore.planning.hierarchical.problems.htn.IHTNPlanningProblem;
import ai.libs.jaicore.search.core.interfaces.IOptimalPathInORGraphSearchFactory;
import ai.libs.jaicore.search.probleminputs.GraphSearchInput;
import ai.libs.jaicore.search.probleminputs.builders.SearchProblemInputBuilder;

/**
 * Hierarchically create an object of type T
 * 
 * @author fmohr
 *
 * @param <T>
 */
public class ForwardDecompositionHTNPlanner<IPlanning extends IHTNPlanningProblem, V extends Comparable<V>, ISearch extends GraphSearchInput<TFDNode, String>>
		extends GraphSearchBasedHTNPlanningAlgorithm<IPlanning, ISearch, TFDNode, String, V> {

	public ForwardDecompositionHTNPlanner(IPlanning problem, IOptimalPathInORGraphSearchFactory<ISearch, TFDNode, String, V> searchFactory,
			SearchProblemInputBuilder<TFDNode, String, ISearch> searchProblemBuilder) {
		super(problem, new ForwardDecompositionReducer<IPlanning>(), searchFactory, searchProblemBuilder);
	}
}
