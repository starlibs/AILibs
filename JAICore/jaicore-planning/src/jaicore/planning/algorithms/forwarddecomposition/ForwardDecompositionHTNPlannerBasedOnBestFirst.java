package jaicore.planning.algorithms.forwarddecomposition;

import jaicore.basic.algorithm.IAlgorithmListener;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.planning.model.core.Action;
import jaicore.planning.model.core.Operation;
import jaicore.planning.model.task.IHTNPlanningProblem;
import jaicore.planning.model.task.stn.Method;
import jaicore.search.algorithms.standard.bestfirst.BestFirstFactory;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.model.other.EvaluatedSearchGraphPath;
import jaicore.search.model.probleminputs.GeneralEvaluatedTraversalTree;
import jaicore.search.model.probleminputs.builders.GeneralEvaluatedTraversalTreeBuilder;
import jaicore.search.model.travesaltree.Node;

public class ForwardDecompositionHTNPlannerBasedOnBestFirst<PO extends Operation, PM extends Method, PA extends Action, IPlanningProblem extends IHTNPlanningProblem<PO,PM, PA>, V extends Comparable<V>> extends ForwardDecompositionHTNPlanner<PO,PM,PA,IPlanningProblem, V, GeneralEvaluatedTraversalTree<TFDNode, String, V>, EvaluatedSearchGraphPath<TFDNode, String, V>, Node<TFDNode,V>, String, IAlgorithmListener> {

	public ForwardDecompositionHTNPlannerBasedOnBestFirst(IPlanningProblem problem, INodeEvaluator<TFDNode, V> nodeEvaluator) {
		super(problem, new BestFirstFactory<>(), new GeneralEvaluatedTraversalTreeBuilder<>(nodeEvaluator));
	}

}
