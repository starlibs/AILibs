package jaicore.search.gui;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import jaicore.search.algorithms.standard.bestfirst.BestFirst;
import jaicore.search.graphgenerators.nqueens.NQueenGenerator;
import jaicore.search.graphgenerators.nqueens.QueenNode;
import jaicore.search.structure.core.Node;

public class FXGuiTester {
	
	int[] numbersOfSolutions = { 2, 10, 4, 40, 92, 352};
	
	@Test
	public void test() {
		int i  =1;
//		prepare a simple Search with the 8 Queens problem
		System.out.print("Checking " + (i+4)+ "-Queens Problem ... ");
		NQueenGenerator gen = new NQueenGenerator(i+4);
		BestFirst<QueenNode, String> search = new BestFirst<>(gen, node-> (double)node.getPoint().getNumberOfAttackedCellsInNextRow());
		
		//Add a graphvisualization windows
//		SimpleGraphVisualizationWindow<Node<QueenNode, Double>> win = new SimpleGraphVisualizationWindow<>(search.getEventBus());
//		win.getPanel().setTooltipGenerator(n->n.getPoint().toString());
		
		//Prepare an replay object
		RecordPlayer<Node<QueenNode, Double>> recordPlayer = new RecordPlayer<>(search.getEventBus());
		
		
		
		
		int solutions = 0;
		while (search.nextSolution() != null)
			solutions ++;
		
		System.out.println("Solutions found.\n Starting the replay:");
		
		//Test the recordPlayer
//		recordPlayer.play();
		
		
		
		
		
		String [] args = new String[0];

		//javafx.application.Application.launch(FXGui, args);

		System.out.println("Gui created");
		recordPlayer.play();
		
	}

}
