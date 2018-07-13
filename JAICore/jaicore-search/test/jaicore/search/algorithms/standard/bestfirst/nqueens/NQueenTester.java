package jaicore.search.algorithms.standard.bestfirst.nqueens;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.Scanner;

import org.junit.Test;

import jaicore.graphvisualizer.SimpleGraphVisualizationWindow;
import jaicore.search.algorithms.standard.ORGraphSearchTester;
import jaicore.search.algorithms.standard.bestfirst.BestFirst;
import jaicore.search.algorithms.standard.core.ORGraphSearch;
import jaicore.search.graphgenerators.nqueens.NQueenGenerator;
import jaicore.search.graphgenerators.nqueens.QueenNode;
import jaicore.search.structure.core.Node;
import jaicore.search.structure.core.NodeExpansionDescription;

public class NQueenTester extends ORGraphSearchTester {
	
	int[] numbersOfSolutions = { 2, 10, 4, 40, 92, 352, 724};
	
	@Test
	public void testIterable(){
		int x = 4;
		
		NQueenGenerator gen = new NQueenGenerator(x);
		
		ORGraphSearch<QueenNode, String, Double> search = new ORGraphSearch<>(gen, n->(double)n.getPoint().getNumberOfAttackedCells());
//		SimpleGraphVisualizationWindow<Node<QueenNode, Double>> win = new SimpleGraphVisualizationWindow<>(search.getEventBus());
//		win.getPanel().setTooltipGenerator(n->n.getPoint().toString());
		
		/*
		 * find solution
		 */
		Scanner key= new Scanner(System.in);
		
		List<QueenNode> solution = null;
		while(solution == null) {
			//if(key.nextLine()=="n")
			List<NodeExpansionDescription<QueenNode,String>> expansion = search.nextExpansion();
			try {
				assertNotNull(expansion);
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
		
	}
	
	@Test
	public void testSequential(){
		for (int i = 0; i < numbersOfSolutions.length; i++) {
			int n = i + 4;
			System.out.print("Checking " + n + "-Queens Problem ... ");
			NQueenGenerator gen = new NQueenGenerator(n);
			BestFirst<QueenNode, String> search = new BestFirst<>(gen, node-> (double)node.getPoint().getNumberOfAttackedCellsInNextRow());
			int solutions = 0;
			while (search.nextSolution() != null)
				solutions ++;
			assertEquals(numbersOfSolutions[i], solutions);
			System.out.println("done");
		}
	}
	
	@Test
	public void testParallelized(){
		for (int i = 0; i < numbersOfSolutions.length; i++) {
			int n = i + 4;
			System.out.print("Checking " + n + "-Queens Problem ... ");
			NQueenGenerator gen = new NQueenGenerator(n);
			BestFirst<QueenNode, String> search = new BestFirst<>(gen, node-> (double)node.getPoint().getNumberOfAttackedCellsInNextRow());
			search.parallelizeNodeExpansion(2);
			search.setTimeoutForComputationOfF(350, node -> 100.0);
			int solutions = 0;
			while (search.nextSolution() != null)
				solutions ++;
			assertEquals(numbersOfSolutions[i], solutions);
			System.out.println("done");
		}
	}
}


