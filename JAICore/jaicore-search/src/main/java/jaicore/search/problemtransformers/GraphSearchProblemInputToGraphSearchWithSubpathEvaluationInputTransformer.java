package jaicore.search.problemtransformers;

import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;
import jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;

public class GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformer<N, A, V extends Comparable<V>>
		implements AlgorithmProblemTransformer<GraphSearchWithPathEvaluationsInput<N, A, V>, GraphSearchWithSubpathEvaluationsInput<N, A, V>> {

	private final INodeEvaluator<N, V> nodeEvaluator;

	public GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformer(INodeEvaluator<N, V> nodeEvaluator) {
		super();
		this.nodeEvaluator = nodeEvaluator;
	}

	@Override
	public GraphSearchWithSubpathEvaluationsInput<N, A, V> transform(GraphSearchWithPathEvaluationsInput<N, A, V> problem) {
		return new GraphSearchWithSubpathEvaluationsInput<>(problem.getGraphGenerator(), nodeEvaluator);
	}

}
