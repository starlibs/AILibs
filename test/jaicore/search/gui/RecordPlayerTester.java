package jaicore.search.gui;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import jaicore.graphvisualizer.SimpleGraphVisualizationWindow;
import jaicore.search.algorithms.standard.bestfirst.BestFirst;
import jaicore.search.graphgenerators.nqueens.NQueenGenerator;
import jaicore.search.graphgenerators.nqueens.QueenNode;
import jaicore.search.structure.core.Node;

public class RecordPlayerTester {

	int[] numbersOfSolutions = { 2, 10, 4, 40, 92, 352};
	
	@Test
	public void test() {
		int i = 2;
		
		//prepare a simple Search with the 8 Queens problem
		System.out.print("Checking " + (i+4)+ "-Queens Problem ... ");
		NQueenGenerator gen = new NQueenGenerator(i+4);
		BestFirst<QueenNode, String> search = new BestFirst<>(gen, node-> (double)node.getPoint().getNumberOfAttackedCellsInNextRow());
		
		//Add a graphvisualization windows
//		SimpleGraphVisualizationWindow<Node<QueenNode, Double>> win = new SimpleGraphVisualizationWindow<>(search.getEventBus());
//		win.getPanel().setTooltipGenerator(n->n.getPoint().toString());
		
		//Prepare an replay object
		RecordPlayer<Node<QueenNode, Double>> recorder = new RecordPlayer<>(search.getEventBus());
		search.nextSolution();
		
		System.out.println("Solution found.\n Starting the replay:");
		
		//Test the recorder
		SimpleGraphVisualizationWindow<Node<QueenNode, Double>> recordedWin = new SimpleGraphVisualizationWindow<>(recorder.getEventBus());
//		recordedWin.getPanel().setTooltipGenerator(n->n.getPoint().toString());
//		recorder.play();
		
		recordedWin.getPanel().setTooltipGenerator(n->n.getPoint().toString());
		for(int s =0; s < 100; s++) {
			recorder.step();
			try {
				TimeUnit.MILLISECONDS.sleep(10);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		System.out.println("done");

		
		
	}

}
