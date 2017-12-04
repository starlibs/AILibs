package jaicore.search.algorithms.standard.npuzzle;

import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;

import jaicore.basic.PerformanceLogger;
import jaicore.basic.PerformanceLogger.PerformanceMeasure;
import jaicore.graphvisualizer.SimpleGraphVisualizationWindow;
import jaicore.search.algorithms.standard.bestfirst.BestFirst;
import jaicore.search.structure.core.Node;

public class NPuzzleGeneratorTester {

	@Test
	public void test() {
		NPuzzleGenerator gen = new NPuzzleGenerator(3,4);
		BestFirst<NPuzzleNode,String> search = new BestFirst<>(gen, n-> (double)n.getPoint().getNumberOfWrongTiles());
		
		SimpleGraphVisualizationWindow<Node<NPuzzleNode,Double>> win = new SimpleGraphVisualizationWindow<>(search.getEventBus());
		win.getPanel().setTooltipGenerator(n->n.getPoint().toString());
		
		/*search for solution*/
		PerformanceLogger.logStart("search");
		
		List<NPuzzleNode> solutionPath = search.nextSolution();
		
		PerformanceLogger.logEnd("search");
		assertNotNull(solutionPath);
		System.out.println("Generated " + search.getCreatedCounter() + " nodes.");
		PerformanceLogger.printStatsAndClear(PerformanceMeasure.TIME);
		while(true);
	}

}
