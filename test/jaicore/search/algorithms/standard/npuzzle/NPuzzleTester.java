package jaicore.search.algorithms.standard.npuzzle;

import static org.junit.Assert.assertNotNull;

import java.util.List;

import jaicore.basic.PerformanceLogger;
import jaicore.basic.PerformanceLogger.PerformanceMeasure;
import jaicore.graphvisualizer.SimpleGraphVisualizationWindow;
import jaicore.search.algorithms.standard.core.ORGraphSearch;


public class NPuzzleTester {

	public static void main(String[] args) {
		NPuzzleGenerator gen = new NPuzzleGenerator(3);
		
		ORGraphSearch search = new ORGraphSearch(gen, n-> (int)Math.round(Math.random() * 1000));
		
//		new SimpleGraphVisualizationWindow<>(search.getEventBus()).getPanel();
		
		/* find solution */
		PerformanceLogger.logStart("search");
		List<NPuzzleNode> solutionPath = search.nextSolution();
		PerformanceLogger.logEnd("search");
		assertNotNull(solutionPath);
		System.out.println("Generated " + search.getCreatedCounter() + " nodes.");
		PerformanceLogger.printStatsAndClear(PerformanceMeasure.TIME);
	}

}
