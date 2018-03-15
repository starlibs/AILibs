package jaicore.search.gui;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import jaicore.graphvisualizer.SimpleGraphVisualizationWindow;
import jaicore.search.algorithms.standard.bestfirst.BestFirst;
import jaicore.search.graphgenerators.nqueens.NQueenGenerator;
import jaicore.search.graphgenerators.nqueens.QueenNode;
import jaicore.search.structure.core.Node;

public class ReplayTester {

	int[] numbersOfSolutions = { 2, 10, 4, 40, 92, 352};
	
	@Test
	public void test() {
		int i = 3;
		
		//prepare a simple Search with the 8 Queens problem
		System.out.print("Checking " + (i+4)+ "-Queens Problem ... ");
		NQueenGenerator gen = new NQueenGenerator(i+4);
		BestFirst<QueenNode, String> search = new BestFirst<>(gen, node-> (double)node.getPoint().getNumberOfAttackedCellsInNextRow());
		
		//Add a graphvisualization windows
//		SimpleGraphVisualizationWindow<Node<QueenNode, Double>> win = new SimpleGraphVisualizationWindow<>(search.getEventBus());
//		win.getPanel().setTooltipGenerator(n->n.getPoint().toString());
		
		//Prepare an replay object
		Recorder<Node<QueenNode, Double>> recorder = new Recorder<>(search.getEventBus());
		
		
		
		
		int solutions = 0;
		while (search.nextSolution() != null)
			solutions ++;
		assertEquals(numbersOfSolutions[i], solutions);
		
		//Test the recorder
		SimpleGraphVisualizationWindow<Node<QueenNode, Double>> recordedWin = new SimpleGraphVisualizationWindow<>(recorder.getEventBus());
		recordedWin.getPanel().setTooltipGenerator(n->n.getPoint().toString());
		recorder.play();
		
		System.out.println("done");
		
		
		
	}

}
