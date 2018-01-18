package jaicore.search.algorithms.parallelized.bestfirst.nqueens;

import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;

import jaicore.search.algorithms.standard.bestfirst.BestFirst;
import jaicore.search.graphgenerators.nqueens.NQueenGenerator;
import jaicore.search.graphgenerators.nqueens.QueenNode;

public class NQueenTester {
	
	
	@Test
	public void test(){
		int x = 10;
		NQueenGenerator gen = new NQueenGenerator(x);
		BestFirst<QueenNode, String> search = new BestFirst<>(gen, n-> (double)n.getPoint().getNumberOfAttackedCellsInNextRow());
		search.parallelizeNodeExpansion(2);
		search.setTimeoutForComputationOfF(350, n -> 100.0);
		List<QueenNode> solutionPath = search.nextSolution();
		assertNotNull(solutionPath);
	}
}


