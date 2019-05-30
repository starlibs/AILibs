package jaicore.search.problemtransformers;

import jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import jaicore.search.model.other.EvaluatedSearchGraphPath;
import jaicore.search.model.other.SearchGraphPath;
import jaicore.search.probleminputs.GraphSearchInput;
import jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;

public class GraphSearchProblemInputToGraphSearchWithSubpathEvaluationViaUninformedness<N, A> implements AlgorithmicProblemReduction<GraphSearchInput<N, A>, SearchGraphPath<N, A>, GraphSearchWithSubpathEvaluationsInput<N, A, Double>, EvaluatedSearchGraphPath<N, A, Double>> {

	@Override
	public GraphSearchWithSubpathEvaluationsInput<N, A, Double> encodeProblem(final GraphSearchInput<N, A> problem) {
		return new GraphSearchWithSubpathEvaluationsInput<>(problem.getGraphGenerator(), n -> 0.0);
	}

	@Override
	public SearchGraphPath<N, A> decodeSolution(final EvaluatedSearchGraphPath<N, A, Double> solution) {
		return solution;
	}

}
