package jaicore.search.algorithms.standard.random;

import org.junit.Test;

import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.search.core.interfaces.IGraphSearchFactory;
import jaicore.search.model.other.SearchGraphPath;
import jaicore.search.probleminputs.GraphSearchInput;
import jaicore.search.testproblems.nqueens.NQueenGenerator;
import jaicore.search.testproblems.nqueens.NQueenTester;
import jaicore.search.testproblems.nqueens.QueenNode;

public class RandomSearchNQueensTester extends NQueenTester<GraphSearchInput<QueenNode, String>, SearchGraphPath<QueenNode, String>> {

	

	@Override
	public IGraphSearchFactory<GraphSearchInput<QueenNode, String>, SearchGraphPath<QueenNode, String>, QueenNode, String> getFactory() {
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

	@Test
	public void testQuickTimeout() throws Exception {
		
	}
	
	@Test
	public void testCancel() throws Exception {
		
	}
	
	@Test
	public void testInterrupt() throws Exception {
		
		/* we cannot create sufficiently difficult instances of this problem in general for this algorithm to require at least 5 seconds */
	}
}
