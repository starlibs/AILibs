package ai.libs.jaicore.search.problemtransformers;

import org.api4.java.ai.graphsearch.problem.IPathSearchWithPathEvaluationsInput;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;

import ai.libs.jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import ai.libs.jaicore.search.model.other.EvaluatedSearchGraphPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;

public class GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformer<N, A, V extends Comparable<V>>
implements AlgorithmicProblemReduction<IPathSearchWithPathEvaluationsInput<N, A, V>, EvaluatedSearchGraphPath<N, A, V>, GraphSearchWithSubpathEvaluationsInput<N, A, V>, EvaluatedSearchGraphPath<N, A, V>> {

	private IPathEvaluator<N, A, V> nodeEvaluator;

	public GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformer() {
		super();
	}

	public GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformer(final IPathEvaluator<N, A, V> nodeEvaluator) {
		super();
		this.nodeEvaluator = nodeEvaluator;
	}

	@Override
	public GraphSearchWithSubpathEvaluationsInput<N, A, V> encodeProblem(final IPathSearchWithPathEvaluationsInput<N, A, V> problem) {
		if (this.nodeEvaluator == null) {
			throw new IllegalStateException("Cannot create problem since node evaluator has not been set, yet.");
		}
		return new GraphSearchWithSubpathEvaluationsInput<>(problem, this.nodeEvaluator);
	}

	public GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformer<N, A, V> setNodeEvaluator(final IPathEvaluator<N, A, V> nodeEvaluator) {
		this.nodeEvaluator = nodeEvaluator;
		return this;
	}

	public IPathEvaluator<N, A, V> getNodeEvaluator() {
		return this.nodeEvaluator;
	}

	@Override
	public EvaluatedSearchGraphPath<N, A, V> decodeSolution(final EvaluatedSearchGraphPath<N, A, V> solution) {
		return solution;
	}
}
