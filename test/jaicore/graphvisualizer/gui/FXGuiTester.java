package jaicore.graphvisualizer.gui;

import jaicore.graphvisualizer.SimpleGraphVisualizationWindow;
import jaicore.graphvisualizer.gui.FXController;
import jaicore.graphvisualizer.gui.FXGui;
import jaicore.graphvisualizer.gui.Recorder;
import jaicore.search.algorithms.standard.bestfirst.BestFirst;
import jaicore.search.graphgenerators.nqueens.NQueenGenerator;
import jaicore.search.graphgenerators.nqueens.QueenNode;
import jaicore.search.structure.core.Node;
import org.junit.Test;

public class FXGuiTester {
	
	int[] numbersOfSolutions = { 2, 10, 4, 40, 92, 352};
	
	@Test
	public void test() {
		int i = 0;
//		prepare a simple Search with the 8 Queens problem
		System.out.print("Checking " + (i+4)+ "-Queens Problem ... ");
		NQueenGenerator gen = new NQueenGenerator(i+4);
		BestFirst<QueenNode, String> search = new BestFirst<>(gen, node-> (double)node.getPoint().getNumberOfAttackedCellsInNextRow());
		
		//Add a graphvisualization windows
//		SimpleGraphVisualizationWindow<Node<QueenNode, Double>> win = new SimpleGraphVisualizationWindow<>(search);
//		win.getPanel().setTooltipGenerator(n->n.getPoint().toString());
		
		//Prepare an replay object
		Recorder<Node<QueenNode, Double>> recorder = new Recorder(search);



		
		int solutions = 0;
		while (search.nextSolution() != null)
			solutions ++;
		

		
		//Test the recordPlayer
//		recordPlayer.play();
		
		
		
		
		//SimpleGraphVisualizationWindow<Node<QueenNode, Double>> recordedWin = new SimpleGraphVisualizationWindow<>(recorder.getEventBus());
		
		String [] args = new String[0];
		//FXGui2.setRec(recorder);
		FXController.setRec(recorder);
		System.out.println("Start the GUI");
		javafx.application.Application.launch(FXGui.class, args);



		
	}

}
