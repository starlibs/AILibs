package jaicore.search.algorithms.standard.nqueens;

import java.util.List;
import java.util.Scanner;

import jaicore.graphvisualizer.SimpleGraphVisualizationWindow;
import jaicore.search.algorithms.standard.core.ORGraphSearch;
import jaicore.search.structure.core.Node;

public class NQueenIterableTester{

	public static void main(String[] args) {
		int x = 4;
		if (args.length != 0)
			x = Integer.parseInt(args[0]);
		
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
				solution = search.nextExpansion();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		
	}

}
