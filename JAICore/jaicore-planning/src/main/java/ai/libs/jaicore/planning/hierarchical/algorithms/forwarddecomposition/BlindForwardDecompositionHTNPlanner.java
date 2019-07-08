package ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition;

import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import ai.libs.jaicore.planning.hierarchical.problems.htn.CostSensitiveHTNPlanningProblem;
import ai.libs.jaicore.planning.hierarchical.problems.htn.IHTNPlanningProblem;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.BestFirstFactory;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;

public class BlindForwardDecompositionHTNPlanner<V extends Comparable<V>> extends ForwardDecompositionHTNPlanner<CostSensitiveHTNPlanningProblem<IHTNPlanningProblem, V>, V, GraphSearchWithSubpathEvaluationsInput<TFDNode, String, V>> {

	public BlindForwardDecompositionHTNPlanner(final CostSensitiveHTNPlanningProblem<IHTNPlanningProblem, V> problem, final INodeEvaluator<TFDNode, V> nodeEvaluator) {
		super(problem, new BestFirstForwardDecompositionReducer<V>(), new BestFirstFactory<>());
		BestFirstForwardDecompositionReducer<V> reducer = (BestFirstForwardDecompositionReducer<V>)this.getProblemTransformer();
		reducer.getTransformer().setNodeEvaluator(nodeEvaluator);
	}
}
