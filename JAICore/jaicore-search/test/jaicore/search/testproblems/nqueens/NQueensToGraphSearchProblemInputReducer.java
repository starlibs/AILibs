package jaicore.search.testproblems.nqueens;

import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.model.other.AgnosticPathEvaluator;
import jaicore.search.model.probleminputs.GraphSearchProblemInput;

public class NQueensToGraphSearchProblemInputReducer implements AlgorithmProblemTransformer<Integer, GraphSearchProblemInput<QueenNode, String, Double>> {

	@Override
	public GraphSearchProblemInput<QueenNode, String, Double> transform(Integer problem) {
		GraphGenerator<QueenNode, String> graphGenerator = new NQueenGenerator(problem);
		return new GraphSearchProblemInput<>(graphGenerator, new AgnosticPathEvaluator<>());

	}
}
