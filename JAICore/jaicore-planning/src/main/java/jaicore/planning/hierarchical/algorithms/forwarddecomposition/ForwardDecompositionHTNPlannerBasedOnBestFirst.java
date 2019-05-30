package jaicore.planning.hierarchical.algorithms.forwarddecomposition;

import jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import jaicore.planning.hierarchical.problems.htn.IHTNPlanningProblem;
import jaicore.search.algorithms.standard.bestfirst.BestFirstFactory;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;
import jaicore.search.probleminputs.builders.GraphSearchWithSubpathEvaluationsInputBuilder;

public class ForwardDecompositionHTNPlannerBasedOnBestFirst<IPlanning extends IHTNPlanningProblem, V extends Comparable<V>> extends ForwardDecompositionHTNPlanner<IPlanning, V, GraphSearchWithSubpathEvaluationsInput<TFDNode, String, V>> {

	public ForwardDecompositionHTNPlannerBasedOnBestFirst(IPlanning problem, INodeEvaluator<TFDNode, V> nodeEvaluator) {
		super(problem, new BestFirstFactory<>(), new GraphSearchWithSubpathEvaluationsInputBuilder<>(nodeEvaluator));
	}

}
