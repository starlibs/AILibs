package ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition;

import org.api4.java.ai.graphsearch.problem.IPathSearchInput;
import org.api4.java.ai.graphsearch.problem.IPathSearchWithPathEvaluationsInput;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;

import ai.libs.jaicore.planning.core.EvaluatedSearchGraphBasedPlan;
import ai.libs.jaicore.planning.core.interfaces.IEvaluatedGraphSearchBasedPlan;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import ai.libs.jaicore.planning.hierarchical.problems.htn.CostSensitiveHTNPlanningProblem;
import ai.libs.jaicore.planning.hierarchical.problems.htn.IHTNPlanningProblem;
import ai.libs.jaicore.planning.hierarchical.problems.htn.PlanEvaluationBasedSearchEvaluator;
import ai.libs.jaicore.search.model.other.EvaluatedSearchGraphPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;
import ai.libs.jaicore.search.problemtransformers.GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformer;
import ai.libs.jaicore.search.problemtransformers.GraphSearchProblemInputToGraphSearchWithSubpathEvaluationViaUninformedness;

public class BestFirstForwardDecompositionReducer<V extends Comparable<V>> extends
AForwardDecompositionReducer<CostSensitiveHTNPlanningProblem<? extends IHTNPlanningProblem, V>, IEvaluatedGraphSearchBasedPlan<TFDNode, String, V>, GraphSearchWithSubpathEvaluationsInput<TFDNode, String, V>, EvaluatedSearchGraphPath<TFDNode, String, V>> {
	private final SimpleForwardDecompositionReducer planProblemToGraphSearchReducer = new SimpleForwardDecompositionReducer(); // this reduces only to graph search with (solution) path evaluations but not sub-path evaluations
	private GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformer<TFDNode, String, V> toSubPathEvaluationTransformer; // conducts the transformation inside graph search

	public BestFirstForwardDecompositionReducer() {
		this(new GraphSearchProblemInputToGraphSearchWithSubpathEvaluationViaUninformedness()); // this assumes that the type is double
	}

	public BestFirstForwardDecompositionReducer(final GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformer<TFDNode, String, V> transformer) {
		super();
		this.toSubPathEvaluationTransformer = transformer;
	}

	public BestFirstForwardDecompositionReducer(final IPathEvaluator<TFDNode, String, V> nodeEvaluator) {
		this();
		this.toSubPathEvaluationTransformer.setNodeEvaluator(nodeEvaluator);
	}

	@Override
	public GraphSearchWithSubpathEvaluationsInput<TFDNode, String, V> encodeProblem(final CostSensitiveHTNPlanningProblem<? extends IHTNPlanningProblem, V> problem) {

		/* first derive a problem with path evaluations */
		IPathSearchInput<TFDNode, String> ordinarySearchProblem = this.planProblemToGraphSearchReducer.encodeProblem(problem.getCorePlanningProblem());
		IPathEvaluator<TFDNode, String, V> pathEvaluator = new PlanEvaluationBasedSearchEvaluator<>(problem.getPlanEvaluator(), this.planProblemToGraphSearchReducer);
		IPathSearchWithPathEvaluationsInput<TFDNode, String, V> searchProblem = new GraphSearchWithPathEvaluationsInput<>(ordinarySearchProblem, pathEvaluator);

		/* now reduce the search problem to one with sub-path evaluation */
		return this.toSubPathEvaluationTransformer.encodeProblem(searchProblem);
	}

	@Override
	public EvaluatedSearchGraphBasedPlan<TFDNode, String, V> decodeSolution(final EvaluatedSearchGraphPath<TFDNode, String, V> solution) {
		return new EvaluatedSearchGraphBasedPlan<>(this.getPlanForSolution(solution), solution);
	}

	public GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformer<TFDNode, String, V> getTransformer() {
		return this.toSubPathEvaluationTransformer;
	}

	public void setTransformer(final GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformer<TFDNode, String, V> transformer) {
		this.toSubPathEvaluationTransformer = transformer;
	}
}
