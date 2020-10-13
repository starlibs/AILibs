package ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;

import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import ai.libs.jaicore.planning.hierarchical.problems.htn.CostSensitiveHTNPlanningProblem;
import ai.libs.jaicore.planning.hierarchical.problems.htn.IHTNPlanningProblem;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.BestFirstFactory;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;

public class ForwardDecompositionHTNPlannerBasedOnBestFirst<V extends Comparable<V>> extends ForwardDecompositionHTNPlanner<CostSensitiveHTNPlanningProblem<? extends IHTNPlanningProblem, V>, V, GraphSearchWithSubpathEvaluationsInput<TFDNode, String, V>> {

	public ForwardDecompositionHTNPlannerBasedOnBestFirst(final CostSensitiveHTNPlanningProblem<? extends IHTNPlanningProblem, V> problem, final IPathEvaluator<TFDNode, String, V> nodeEvaluator) {
		super(problem, new BestFirstForwardDecompositionReducer<>(nodeEvaluator), new BestFirstFactory<>());
	}
}
