package jaicore.search.problemtransformers;

import jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;
import jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;

public class GraphSearchProblemInputToUninformedGeneralEvaluatedTraversalTree<N, A> implements AlgorithmicProblemReduction<GraphSearchWithPathEvaluationsInput<N, A, Double>, GraphSearchWithSubpathEvaluationsInput<N, A, Double>> {

	@Override
	public GraphSearchWithSubpathEvaluationsInput<N, A, Double> transform(GraphSearchWithPathEvaluationsInput<N, A, Double> problem) {
		return new GraphSearchWithSubpathEvaluationsInput<>(problem.getGraphGenerator(), n -> 0.0);
	}

}
