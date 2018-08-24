package jaicore.search.algorithms.parallelized.bestfirst.nqueens;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import jaicore.search.algorithms.standard.bestfirst.BestFirst;
import jaicore.search.graphgenerators.nqueens.NQueenGenerator;
import jaicore.search.graphgenerators.nqueens.QueenNode;

public class NQueenTester {

	@Test
	public void test() throws InterruptedException {
		int[] numberOfSolutions = { 2, 10, 4, 40, 92, 352, 724, 2680 };
		for (int i = 0; i < numberOfSolutions.length; i++) {
			int n = i + 4;
			System.out.print("Checking " + n + "-Queens Problem ... ");
			NQueenGenerator gen = new NQueenGenerator(n);
			BestFirst<QueenNode, String, Double> search = new BestFirst<>(gen, node -> (double) node.getPoint().getNumberOfAttackedCellsInNextRow());
			search.parallelizeNodeExpansion(2);
			search.setTimeoutForComputationOfF(350, node -> 100.0);
			int solutions = 0;
			while (search.nextSolution() != null)
				solutions++;
			assertEquals(numberOfSolutions[i], solutions);
			System.out.println("done");
		}
	}
}
