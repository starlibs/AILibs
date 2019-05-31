package ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition;

import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import ai.libs.jaicore.planning.hierarchical.problems.htn.IHTNPlanningProblem;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.BestFirstFactory;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;
import ai.libs.jaicore.search.probleminputs.builders.GraphSearchWithSubpathEvaluationsInputBuilder;

public class ForwardDecompositionHTNPlannerBasedOnBestFirst<IPlanning extends IHTNPlanningProblem, V extends Comparable<V>> extends ForwardDecompositionHTNPlanner<IPlanning, V, GraphSearchWithSubpathEvaluationsInput<TFDNode, String, V>> {

	public ForwardDecompositionHTNPlannerBasedOnBestFirst(IPlanning problem, INodeEvaluator<TFDNode, V> nodeEvaluator) {
		super(problem, new BestFirstFactory<>(), new GraphSearchWithSubpathEvaluationsInputBuilder<>(nodeEvaluator));
	}

}
