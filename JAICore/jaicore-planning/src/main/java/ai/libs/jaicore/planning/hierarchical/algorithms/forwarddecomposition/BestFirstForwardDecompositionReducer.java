package ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition;

import ai.libs.jaicore.planning.core.EvaluatedSearchGraphBasedPlan;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import ai.libs.jaicore.planning.hierarchical.problems.htn.CostSensitiveHTNPlanningProblem;
import ai.libs.jaicore.planning.hierarchical.problems.htn.IHTNPlanningProblem;
import ai.libs.jaicore.search.model.other.EvaluatedSearchGraphPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchInput;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;
import ai.libs.jaicore.search.problemtransformers.GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformer;

public class BestFirstForwardDecompositionReducer<V extends Comparable<V>> extends
AForwardDecompositionReducer<CostSensitiveHTNPlanningProblem<IHTNPlanningProblem, V>, EvaluatedSearchGraphBasedPlan<V, TFDNode>, GraphSearchWithSubpathEvaluationsInput<TFDNode, String, V>, EvaluatedSearchGraphPath<TFDNode, String, V>> {
	private final SimpleForwardDecompositionReducer simpleReducer = new SimpleForwardDecompositionReducer();
	private GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformer<TFDNode, String, V> transformer = new GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformer<>();

	@Override
	public GraphSearchWithSubpathEvaluationsInput<TFDNode, String, V> encodeProblem(final CostSensitiveHTNPlanningProblem<IHTNPlanningProblem, V> problem) {
		GraphSearchInput<TFDNode, String> searchInput = this.simpleReducer.encodeProblem(problem.getCorePlanningProblem());
		return new GraphSearchWithSubpathEvaluationsInput<>(searchInput.getGraphGenerator(), this.transformer.getNodeEvaluator());
	}

	@Override
	public EvaluatedSearchGraphBasedPlan<V, TFDNode> decodeSolution(final EvaluatedSearchGraphPath<TFDNode, String, V> solution) {
		return new EvaluatedSearchGraphBasedPlan<>(this.getPlanForSolution(solution), solution);
	}

	public GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformer<TFDNode, String, V> getTransformer() {
		return this.transformer;
	}

	public void setTransformer(final GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformer<TFDNode, String, V> transformer) {
		this.transformer = transformer;
	}
}
