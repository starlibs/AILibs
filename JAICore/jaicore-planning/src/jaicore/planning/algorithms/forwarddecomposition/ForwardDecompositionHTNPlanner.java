package jaicore.planning.algorithms.forwarddecomposition;

import jaicore.basic.algorithm.IAlgorithmListener;
import jaicore.planning.algorithms.GraphSearchBasedHTNPlanningAlgorithm;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.planning.model.core.Action;
import jaicore.planning.model.core.Operation;
import jaicore.planning.model.task.IHTNPlanningProblem;
import jaicore.planning.model.task.stn.Method;
import jaicore.search.core.interfaces.IGraphSearchFactory;
import jaicore.search.model.probleminputs.builders.SearchProblemInputBuilder;

/**
 * Hierarchically create an object of type T
 * 
 * @author fmohr
 *
 * @param <T>
 */
public class ForwardDecompositionHTNPlanner<PO extends Operation, PM extends Method, PA extends Action, I extends IHTNPlanningProblem<PO,PM,PA>, V extends Comparable<V>, ISearch, OSearch, NSearch, ASearch, L extends IAlgorithmListener>
		extends GraphSearchBasedHTNPlanningAlgorithm<PA, I, ISearch, OSearch, TFDNode, String, V, NSearch, ASearch, IAlgorithmListener> {

	public ForwardDecompositionHTNPlanner(I problem, IGraphSearchFactory<ISearch, OSearch, TFDNode, String, V, NSearch, ASearch> searchFactory,
			SearchProblemInputBuilder<TFDNode, String, ISearch> searchProblemBuilder) {
		super(problem, new ForwardDecompositionReducer<PO,PM,PA,I>(), searchFactory, searchProblemBuilder);
	}
}
