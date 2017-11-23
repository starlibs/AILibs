package jaicore.search.algorithms.standard.npuzzle;

import static org.junit.Assert.assertNotNull;

import java.util.List;

import jaicore.basic.PerformanceLogger;
import jaicore.basic.PerformanceLogger.PerformanceMeasure;
import jaicore.graphvisualizer.SimpleGraphVisualizationWindow;
import jaicore.search.algorithms.standard.bestfirst.BestFirst;
import jaicore.search.structure.core.Node;


public class NPuzzleTester {

	public static void main(String[] args) {
		/*
		 * Every block with two comments contains the code for a different type of the npuzzle problem.
		 * It should always the same line be used.
		 */
		
		
//		NPuzzleGenerator gen = new NPuzzleGenerator(3,1000);
		NPuzzleRedundantGenerator gen = new NPuzzleRedundantGenerator(3,1000);
//		NPuzzleStarGenerator gen = new NPuzzleGenerator(3,1000);
		
		
		
//		BestFirst<NPuzzleNode, String> search = new BestFirst<>(gen, n-> (int)Math.round(Math.random() * 1000));
		BestFirst<NPuzzleRedundantNode, String> search = new BestFirst<>(gen, n-> (int)Math.round(Math.random() * 1000));
//		BestFirst<NPuzzleStarNode, String> search = new BestFirst<>(gen, n-> (int)Math.round(Math.random() * 1000));

		
		
//		SimpleGraphVisualizationWindow<Node<NPuzzleNode,Integer>> win = new SimpleGraphVisualizationWindow<>(search.getEventBus());
		SimpleGraphVisualizationWindow<Node<NPuzzleRedundantNode,Integer>> win = new SimpleGraphVisualizationWindow<>(search.getEventBus());
//		SimpleGraphVisualizationWindow<Node<NPuzzleStarNode,Integer>> win = new SimpleGraphVisualizationWindow<>(search.getEventBus());
	
		win.getPanel().setTooltipGenerator(n->n.getPoint().toString());
		
		/* find solution */
		PerformanceLogger.logStart("search");
		
		
//		List<NPuzzleNode> solutionPath = search.nextSolution();
		List<NPuzzleRedundantNode> solutionPath = search.nextSolution();
//		List<NPuzzleStarNode> solutionPath = search.nextSolution();
		
		
		PerformanceLogger.logEnd("search");
		assertNotNull(solutionPath);
		System.out.println("Generated " + search.getCreatedCounter() + " nodes.");
		PerformanceLogger.printStatsAndClear(PerformanceMeasure.TIME);
		while(true);
	}

}
