package jaicore.search.algorithms.standard.npuzzle.parentDiscarding;


import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;

import jaicore.basic.PerformanceLogger;
import jaicore.basic.PerformanceLogger.PerformanceMeasure;
import jaicore.graphvisualizer.SimpleGraphVisualizationWindow;
import jaicore.search.algorithms.standard.astar.AStar;
import jaicore.search.algorithms.standard.core.ParentDiscarding;
import jaicore.search.structure.core.Node;

public class ParentDiscardingTest {

	
	@Test
	public void test() {

		
		int board[][] = {{0,1},{1,1}};
		PDPuzzleGenerator gen = new PDPuzzleGenerator(board,0,0);
		
		
		AStar<PDPuzzleNode,String> search = new AStar<>(gen,
				(n1,n2)->{
					double g = 0.0;
					if(n2.getPoint().getBoard()[0][0]== 2)
						return 3.0;
					if(n2.getPoint().getBoard()[1][1] == 0)
						return 1.0;
					else
						return 4.0;
				}, 
				n->{
					if(n.getPoint() instanceof PDPuzzleNode) {
						PDPuzzleNode n1 = (PDPuzzleNode) n.getPoint();
						return n1.getDistance();
					}
					else
						return n.getPoint().getDistance();
				}, ParentDiscarding.OPEN) ;
		
		SimpleGraphVisualizationWindow<Node<PDPuzzleNode,Double>> win = new SimpleGraphVisualizationWindow<>(search.getEventBus());
		win.getPanel().setTooltipGenerator(n->n.getPoint().toString());
		
		/*search for solution*/
		PerformanceLogger.logStart("search");
		
		List<PDPuzzleNode> solutionPath = search.nextSolution();
		List<PDPuzzleNode> solutionPath2 = search.nextSolution();
		solutionPath2 = search.nextSolution();
		solutionPath2 = search.nextSolution();
		solutionPath2 = search.nextSolution();
		solutionPath2 = search.nextSolution();
		
		PerformanceLogger.logEnd("search");
		assertNotNull(solutionPath);
		assert solutionPath.size() <=31;
		System.out.println(solutionPath.size());
		System.out.println("Generated " + search.getCreatedCounter() + " nodes.");
		PerformanceLogger.printStatsAndClear(PerformanceMeasure.TIME);
		while(true);
	}

}
