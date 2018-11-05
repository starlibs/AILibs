package jaicore.search.testproblems.nqueens;

import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.search.model.probleminputs.NodeRecommendedTree;

public class NQueensToNodeRecommendedTreeReducer implements AlgorithmProblemTransformer<Integer, NodeRecommendedTree<QueenNode, String>> {

	@Override
	public NodeRecommendedTree<QueenNode, String> transform(Integer problem) {
		return new NodeRecommendedTree<>(new NQueenGenerator(problem), (n1,n2) -> Integer.valueOf(n1.getNumberOfAttackedCells()).compareTo(n2.getNumberOfAttackedCells()));
	}

}
