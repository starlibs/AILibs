package jaicore.search.algorithms.standard.lds.nqueens;

import java.util.List;

import org.junit.Test;

import jaicore.graphvisualizer.SimpleGraphVisualizationWindow;
import jaicore.search.algorithms.standard.lds.BestFirstLimitedDiscrepancySearch;
import jaicore.search.algorithms.standard.lds.NodeOrderList;
import jaicore.search.graphgenerators.nqueens.NQueenGenerator;
import jaicore.search.graphgenerators.nqueens.QueenNode;
import jaicore.search.structure.core.Node;
import junit.framework.Assert;

public class LDSNQueenTester {

	int[] numbersOfSolutions = { 2, 10, 4, 40, 92, 352, 724 };
	
	final static boolean SHOW_GRAPH = false;
	final static boolean PRINT_SOLUTIONS = false;

	@Test
	public void testSequential() {

		for (int i = 0; i < numbersOfSolutions.length; i++) {

			int n = i + 4;
			System.out.println("Checking solutions for " + n +" x " + n + " board.");

			/* initialize LDS */
			BestFirstLimitedDiscrepancySearch<QueenNode, String> lds = new BestFirstLimitedDiscrepancySearch<>(
					new NQueenGenerator(n), (n1, n2) -> {
						List<Integer> p1 = n1.getPositions();
						List<Integer> p2 = n2.getPositions();
						if (p1.size() != p2.size())
							throw new IllegalArgumentException(
									"Cannot compare nodes with different numbers of queens!");
						return p1.get(p1.size() - 1).compareTo(p2.get(p2.size() - 1));
					});

			/* run lds */
			if (SHOW_GRAPH)
				new SimpleGraphVisualizationWindow<Node<QueenNode, NodeOrderList>>(lds).getPanel().setTooltipGenerator(node -> node.getInternalLabel() + "<br />" + node.getPoint().toString());
			List<QueenNode> solution;
			int solutionCounter = 0;
			int currentDeviations = 0;
			while ((solution = lds.nextSolution()) != null) {
				if (PRINT_SOLUTIONS)
					System.out.println("\t" + solution);
				solutionCounter++;
				
				/* checking that deviations increase monotonically */
				int deviations = lds.getFOfReturnedSolution(solution).stream().mapToInt(x -> x).sum();
				Assert.assertTrue(deviations >= currentDeviations);
				currentDeviations = deviations;
			}

			Assert.assertEquals(numbersOfSolutions[i], solutionCounter);
		}
	}
}
