package jaicore.planning.hierarchical.algorithms.forwarddecomposition;

import jaicore.planning.classical.problems.strips.Operation;
import jaicore.planning.core.Action;
import jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import jaicore.planning.hierarchical.problems.htn.IHTNPlanningProblem;
import jaicore.planning.hierarchical.problems.stn.Method;
import jaicore.search.algorithms.standard.bestfirst.BestFirstFactory;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.model.travesaltree.Node;
import jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;
import jaicore.search.probleminputs.builders.GraphSearchWithSubpathEvaluationsInputBuilder;

public class ForwardDecompositionHTNPlannerBasedOnBestFirst<PO extends Operation, PM extends Method, PA extends Action, IPlanningProblem extends IHTNPlanningProblem<PO,PM, PA>, V extends Comparable<V>> extends ForwardDecompositionHTNPlanner<PO,PM,PA,IPlanningProblem, V, GraphSearchWithSubpathEvaluationsInput<TFDNode, String, V>, Node<TFDNode,V>, String> {

	public ForwardDecompositionHTNPlannerBasedOnBestFirst(IPlanningProblem problem, INodeEvaluator<TFDNode, V> nodeEvaluator) {
		super(problem, new BestFirstFactory<>(), new GraphSearchWithSubpathEvaluationsInputBuilder<>(nodeEvaluator));
	}

}
