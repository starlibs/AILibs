package jaicore.planning.algorithms.forwarddecomposition;

import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.planning.model.core.Action;
import jaicore.planning.model.core.Operation;
import jaicore.planning.model.task.IHTNPlanningProblem;
import jaicore.planning.model.task.stn.Method;
import jaicore.search.algorithms.standard.bestfirst.BestFirstFactory;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.model.other.EvaluatedSearchGraphPath;
import jaicore.search.model.travesaltree.Node;
import jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;
import jaicore.search.probleminputs.builders.GeneralEvaluatedTraversalTreeBuilder;

public class ForwardDecompositionHTNPlannerBasedOnBestFirst<PO extends Operation, PM extends Method, PA extends Action, IPlanningProblem extends IHTNPlanningProblem<PO,PM, PA>, V extends Comparable<V>> extends ForwardDecompositionHTNPlanner<PO,PM,PA,IPlanningProblem, V, GraphSearchWithSubpathEvaluationsInput<TFDNode, String, V>, Node<TFDNode,V>, String> {

	public ForwardDecompositionHTNPlannerBasedOnBestFirst(IPlanningProblem problem, INodeEvaluator<TFDNode, V> nodeEvaluator) {
		super(problem, new BestFirstFactory<>(), new GeneralEvaluatedTraversalTreeBuilder<>(nodeEvaluator));
	}

}
