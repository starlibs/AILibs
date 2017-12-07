package jaicore.search.algorithms.standard.nqueens;

import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;

import jaicore.basic.PerformanceLogger;
import jaicore.basic.PerformanceLogger.PerformanceMeasure;
import jaicore.graphvisualizer.SimpleGraphVisualizationWindow;
import jaicore.search.algorithms.standard.bestfirst.BestFirst;
import jaicore.search.structure.core.Node;

public class NQueenTester {
	
	
	@Test
	public void test(){
		int x = 4;
				
		NQueenGenerator gen = new NQueenGenerator(x);
		
		BestFirst<QueenNode, String> search = new BestFirst<QueenNode,String>(gen, n-> (double)n.getPoint().getNumberOfAttackedCellsInNextRow());
		
//		new SimpleGraphVisualizationWindow<>(search.getEventBus()).getPanel().setTooltipGenerator(n->n.getPoint().toString());
		SimpleGraphVisualizationWindow<Node<QueenNode,Double>> win = new SimpleGraphVisualizationWindow<>(search.getEventBus());
		win.getPanel().setTooltipGenerator(n->n.getPoint().toString());
		
		/* find solution */
		PerformanceLogger.logStart("search");
		List<QueenNode> solutionPath = search.nextSolution();
		PerformanceLogger.logEnd("search");
		assertNotNull(solutionPath);
		System.out.println("Generated " + search.getCreatedCounter() + " nodes.");
		PerformanceLogger.printStatsAndClear(PerformanceMeasure.TIME);
		while(true);
	}
}


