package jaicore.search.algorithms.standard.random;

import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.search.core.interfaces.IGraphSearchFactory;
import jaicore.search.model.probleminputs.GraphSearchInput;
import jaicore.search.testproblems.nqueens.NQueenGenerator;
import jaicore.search.testproblems.nqueens.NQueenTester;
import jaicore.search.testproblems.nqueens.QueenNode;

public class RandomSearchNQueensTester extends NQueenTester<GraphSearchInput<QueenNode, String>, Object, QueenNode, String> {

	

	@Override
	public IGraphSearchFactory<GraphSearchInput<QueenNode, String>, Object, QueenNode, String, Double, QueenNode, String> getFactory() {
		return new RandomSearchFactory<>();
	}

	@Override
	public AlgorithmProblemTransformer<Integer, GraphSearchInput<QueenNode, String>> getProblemReducer() {
		return new AlgorithmProblemTransformer<Integer, GraphSearchInput<QueenNode,String>>() {

			@Override
			public GraphSearchInput<QueenNode, String> transform(Integer problem) {
				return new GraphSearchInput<>(new NQueenGenerator(problem));
			}
		};
	}

}
