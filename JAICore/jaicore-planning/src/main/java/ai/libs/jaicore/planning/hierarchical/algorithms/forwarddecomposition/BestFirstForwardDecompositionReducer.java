package ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;

import ai.libs.jaicore.planning.core.EvaluatedSearchGraphBasedPlan;
import ai.libs.jaicore.planning.core.interfaces.IEvaluatedGraphSearchBasedPlan;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import ai.libs.jaicore.planning.hierarchical.problems.htn.CostSensitiveHTNPlanningProblem;
import ai.libs.jaicore.planning.hierarchical.problems.htn.IHTNPlanningProblem;
import ai.libs.jaicore.search.model.other.EvaluatedSearchGraphPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchInput;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;
import ai.libs.jaicore.search.problemtransformers.GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformer;

public class BestFirstForwardDecompositionReducer<V extends Comparable<V>> extends
AForwardDecompositionReducer<CostSensitiveHTNPlanningProblem<IHTNPlanningProblem, V>, IEvaluatedGraphSearchBasedPlan<TFDNode, String, V>, GraphSearchWithSubpathEvaluationsInput<TFDNode, String, V>, EvaluatedSearchGraphPath<TFDNode, String, V>> {
	private final SimpleForwardDecompositionReducer simpleReducer = new SimpleForwardDecompositionReducer();
	private GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformer<TFDNode, String, V> transformer;

	public BestFirstForwardDecompositionReducer() {
		this(new GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformer<>());
	}

	public BestFirstForwardDecompositionReducer(final IPathEvaluator<TFDNode, String, V> nodeEvaluator) {
		this();
		this.transformer.setNodeEvaluator(nodeEvaluator);
	}

	public BestFirstForwardDecompositionReducer(final GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformer<TFDNode, String, V> transformer) {
		super();
		this.transformer = transformer;
	}

	@Override
	public GraphSearchWithSubpathEvaluationsInput<TFDNode, String, V> encodeProblem(final CostSensitiveHTNPlanningProblem<IHTNPlanningProblem, V> problem) {
		GraphSearchInput<TFDNode, String> searchInput = this.simpleReducer.encodeProblem(problem.getCorePlanningProblem());
		if (this.transformer.getNodeEvaluator() == null) {
			throw new IllegalStateException("No node evaluator has been set in the transformer!");
		}
		return new GraphSearchWithSubpathEvaluationsInput<>(searchInput, this.transformer.getNodeEvaluator());
	}

	@Override
	public EvaluatedSearchGraphBasedPlan<TFDNode, String, V> decodeSolution(final EvaluatedSearchGraphPath<TFDNode, String, V> solution) {
		return new EvaluatedSearchGraphBasedPlan<>(this.getPlanForSolution(solution), solution);
	}

	public GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformer<TFDNode, String, V> getTransformer() {
		return this.transformer;
	}

	public void setTransformer(final GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformer<TFDNode, String, V> transformer) {
		this.transformer = transformer;
	}
}
