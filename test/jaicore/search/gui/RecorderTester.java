package jaicore.search.gui;

import jaicore.graphvisualizer.SimpleGraphVisualizationWindow;
import jaicore.search.algorithms.standard.bestfirst.BestFirst;
import jaicore.search.graphgenerators.nqueens.NQueenGenerator;
import jaicore.search.graphgenerators.nqueens.QueenNode;
import jaicore.search.structure.core.Node;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class RecorderTester {

	int[] numbersOfSolutions = { 2, 10, 4, 40, 92, 352};
	
	@Test
	public void test() {
		int i = 5;
		
		//prepare a simple Search with the 8 Queens problem
		System.out.print("Checking " + (i+4)+ "-Queens Problem ... ");
		NQueenGenerator gen = new NQueenGenerator(i+4);
		BestFirst<QueenNode, String> search = new BestFirst<>(gen, node-> (double)node.getPoint().getNumberOfAttackedCellsInNextRow());

		//Prepare an replay object
		Recorder<Node<QueenNode, Double>> recorder = new Recorder<>(search.getEventBus());

		SimpleGraphVisualizationWindow<Node<QueenNode, Double>> win = new SimpleGraphVisualizationWindow<>(search.getEventBus());
//		win.getPanel().setTooltipGenerator(n->n.getPoint().toString());

		search.nextSolution();
		
		System.out.println("Solution found.\n Starting the replay:");
		
		//Test the recorder
		String test = "/home/jkoepe/git/test.txt";
		try {
			recorder.writeEventsToFile(test);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("done");
		
		
		
	}

}
