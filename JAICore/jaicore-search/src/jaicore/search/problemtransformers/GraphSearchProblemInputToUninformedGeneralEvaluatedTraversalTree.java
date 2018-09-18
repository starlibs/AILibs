package jaicore.search.problemtransformers;

import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.search.model.probleminputs.GeneralEvaluatedTraversalTree;
import jaicore.search.model.probleminputs.GraphSearchProblemInput;

public class GraphSearchProblemInputToUninformedGeneralEvaluatedTraversalTree<N, A> implements AlgorithmProblemTransformer<GraphSearchProblemInput<N, A, Double>, GeneralEvaluatedTraversalTree<N, A, Double>> {

	@Override
	public GeneralEvaluatedTraversalTree<N, A, Double> transform(GraphSearchProblemInput<N, A, Double> problem) {
		return new GeneralEvaluatedTraversalTree<>(problem.getGraphGenerator(), n -> 0.0);
	}

}
