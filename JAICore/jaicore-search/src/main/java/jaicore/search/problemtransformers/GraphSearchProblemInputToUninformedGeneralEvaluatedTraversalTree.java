package jaicore.search.problemtransformers;

import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;
import jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;

public class GraphSearchProblemInputToUninformedGeneralEvaluatedTraversalTree<N, A> implements AlgorithmProblemTransformer<GraphSearchWithPathEvaluationsInput<N, A, Double>, GraphSearchWithSubpathEvaluationsInput<N, A, Double>> {

	@Override
	public GraphSearchWithSubpathEvaluationsInput<N, A, Double> transform(GraphSearchWithPathEvaluationsInput<N, A, Double> problem) {
		return new GraphSearchWithSubpathEvaluationsInput<>(problem.getGraphGenerator(), n -> 0.0);
	}

}
