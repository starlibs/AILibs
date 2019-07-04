package ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition;

import ai.libs.jaicore.planning.core.Plan;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import ai.libs.jaicore.planning.hierarchical.problems.htn.IHTNPlanningProblem;
import ai.libs.jaicore.search.model.other.SearchGraphPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchInput;

public class SimpleForwardDecompositionReducer extends AForwardDecompositionReducer<IHTNPlanningProblem, Plan, GraphSearchInput<TFDNode, String>, SearchGraphPath<TFDNode, String>> {

	@Override
	public GraphSearchInput<TFDNode, String> encodeProblem(final IHTNPlanningProblem problem) {
		return this.getGraphSearchInput(problem);
	}

	@Override
	public Plan decodeSolution(final SearchGraphPath<TFDNode, String> solution) {
		return this.getPlanForSolution(solution);
	}
}
