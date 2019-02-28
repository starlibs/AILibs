package jaicore.search.testproblems.nqueens;

import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.search.probleminputs.GraphSearchWithNodeRecommenderInput;

public class NQueensToNodeRecommendedTreeReducer implements AlgorithmProblemTransformer<Integer, GraphSearchWithNodeRecommenderInput<QueenNode, String>> {

	@Override
	public GraphSearchWithNodeRecommenderInput<QueenNode, String> transform(Integer problem) {
		return new GraphSearchWithNodeRecommenderInput<>(new NQueenGenerator(problem), (n1,n2) -> Integer.valueOf(n1.getNumberOfAttackedCells()).compareTo(n2.getNumberOfAttackedCells()));
	}

}
