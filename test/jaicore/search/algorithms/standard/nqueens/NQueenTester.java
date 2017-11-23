package jaicore.search.algorithms.standard.nqueens;

import static org.junit.Assert.assertNotNull;

import java.util.List;

import jaicore.basic.PerformanceLogger;
import jaicore.basic.PerformanceLogger.PerformanceMeasure;
import jaicore.graphvisualizer.SimpleGraphVisualizationWindow;
import jaicore.search.algorithms.standard.bestfirst.BestFirst;
import jaicore.search.structure.core.Node;

public class NQueenTester {
	
	
	
	public static void main(String [] args) {
		int x = 0;
		if(args.length != 0)
			x = Integer.parseInt(args[0]);
		
		NQueenGenerator gen = new NQueenGenerator(8);
		
		BestFirst<QueenNode, String> search = new BestFirst<>(gen, n-> (int)Math.round(Math.random() * 1000));
		
//		new SimpleGraphVisualizationWindow<>(search.getEventBus()).getPanel().setTooltipGenerator(n->n.getPoint().toString());
		SimpleGraphVisualizationWindow<Node<QueenNode,Integer>> win = new SimpleGraphVisualizationWindow<>(search.getEventBus());
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


