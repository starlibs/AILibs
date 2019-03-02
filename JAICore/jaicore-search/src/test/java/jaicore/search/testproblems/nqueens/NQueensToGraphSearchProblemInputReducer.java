package jaicore.search.testproblems.nqueens;

import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.model.other.AgnosticPathEvaluator;
import jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;

public class NQueensToGraphSearchProblemInputReducer implements AlgorithmProblemTransformer<NQueensProblem, GraphSearchWithPathEvaluationsInput<QueenNode, String, Double>> {

	@Override
	public GraphSearchWithPathEvaluationsInput<QueenNode, String, Double> transform(NQueensProblem problem) {
		GraphGenerator<QueenNode, String> graphGenerator = new NQueenGenerator(problem.getN());
		return new GraphSearchWithPathEvaluationsInput<>(graphGenerator, new AgnosticPathEvaluator<>());

	}
}
