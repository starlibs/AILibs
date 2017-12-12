package jaicore.search.algorithms.standard.nqueens;

import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.Scanner;

import org.junit.Test;

import jaicore.graphvisualizer.SimpleGraphVisualizationWindow;
import jaicore.search.algorithms.standard.core.ORGraphSearch;
import jaicore.search.structure.core.Node;
import jaicore.search.structure.core.NodeExpansionDescription;

public class NQueenIterableTester{

	@Test
	public void test(){
		int x = 4;
		
		NQueenGenerator gen = new NQueenGenerator(x);
		
		ORGraphSearch<QueenNode, String, Double> search = new ORGraphSearch<>(gen, n->(double)n.getPoint().getNumberOfAttackedCells());
		SimpleGraphVisualizationWindow<Node<QueenNode, Double>> win = new SimpleGraphVisualizationWindow<>(search.getEventBus());
		win.getPanel().setTooltipGenerator(n->n.getPoint().toString());
		
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

}
