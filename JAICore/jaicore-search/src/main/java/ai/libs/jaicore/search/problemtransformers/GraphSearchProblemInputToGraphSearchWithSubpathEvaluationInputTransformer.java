package ai.libs.jaicore.search.problemtransformers;

import ai.libs.jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import ai.libs.jaicore.search.model.other.EvaluatedSearchGraphPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;

public class GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformer<N, A, V extends Comparable<V>>
implements AlgorithmicProblemReduction<GraphSearchWithPathEvaluationsInput<N, A, V>, EvaluatedSearchGraphPath<N, A, V>, GraphSearchWithSubpathEvaluationsInput<N, A, V>, EvaluatedSearchGraphPath<N, A, V>> {

	private INodeEvaluator<N, V> nodeEvaluator;

	public GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformer() {
		super();
	}

	public GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformer(final INodeEvaluator<N, V> nodeEvaluator) {
		super();
		this.nodeEvaluator = nodeEvaluator;
	}

	@Override
	public GraphSearchWithSubpathEvaluationsInput<N, A, V> encodeProblem(final GraphSearchWithPathEvaluationsInput<N, A, V> problem) {
		if (this.nodeEvaluator == null) {
			throw new IllegalStateException("Cannot create problem since node evaluator has not been set, yet.");
		}
		return new GraphSearchWithSubpathEvaluationsInput<>(problem.getGraphGenerator(), this.nodeEvaluator);
	}

	public void setNodeEvaluator(final INodeEvaluator<N, V> nodeEvaluator) {
		this.nodeEvaluator = nodeEvaluator;
	}


	public INodeEvaluator<N, V> getNodeEvaluator() {
		return this.nodeEvaluator;
	}

	@Override
	public EvaluatedSearchGraphPath<N, A, V> decodeSolution(final EvaluatedSearchGraphPath<N, A, V> solution) {
		return solution;
	}
}
