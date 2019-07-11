package ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition;

import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.IPath;

import ai.libs.jaicore.planning.core.Plan;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import ai.libs.jaicore.planning.hierarchical.problems.htn.IHTNPlanningProblem;
import ai.libs.jaicore.search.probleminputs.GraphSearchInput;

public class SimpleForwardDecompositionReducer extends AForwardDecompositionReducer<IHTNPlanningProblem, Plan, GraphSearchInput<TFDNode, String>, IPath<TFDNode, String>> {

	@Override
	public GraphSearchInput<TFDNode, String> encodeProblem(final IHTNPlanningProblem problem) {
		return this.getGraphSearchInput(problem);
	}

	@Override
	public Plan decodeSolution(final IPath<TFDNode, String> solution) {
		return this.getPlanForSolution(solution);
	}
}
