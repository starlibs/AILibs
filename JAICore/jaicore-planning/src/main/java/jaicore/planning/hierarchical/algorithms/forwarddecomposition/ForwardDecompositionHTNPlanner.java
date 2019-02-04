package jaicore.planning.hierarchical.algorithms.forwarddecomposition;

import jaicore.planning.classical.problems.strips.Operation;
import jaicore.planning.core.Action;
import jaicore.planning.hierarchical.algorithms.GraphSearchBasedHTNPlanningAlgorithm;
import jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import jaicore.planning.hierarchical.problems.htn.IHTNPlanningProblem;
import jaicore.planning.hierarchical.problems.stn.Method;
import jaicore.search.core.interfaces.IOptimalPathInORGraphSearchFactory;
import jaicore.search.probleminputs.GraphSearchInput;
import jaicore.search.probleminputs.builders.SearchProblemInputBuilder;

/**
 * Hierarchically create an object of type T
 * 
 * @author fmohr
 *
 * @param <T>
 */
public class ForwardDecompositionHTNPlanner<PO extends Operation, PM extends Method, PA extends Action, I extends IHTNPlanningProblem<PO,PM,PA>, V extends Comparable<V>, ISearch extends GraphSearchInput<TFDNode, String>, NSearch, ASearch>
		extends GraphSearchBasedHTNPlanningAlgorithm<PA, I, ISearch, TFDNode, String, V, NSearch, ASearch> {

	public ForwardDecompositionHTNPlanner(I problem, IOptimalPathInORGraphSearchFactory<ISearch, TFDNode, String, V, NSearch, ASearch> searchFactory,
			SearchProblemInputBuilder<TFDNode, String, ISearch> searchProblemBuilder) {
		super(problem, new ForwardDecompositionReducer<PO,PM,PA,I>(), searchFactory, searchProblemBuilder);
	}
}
