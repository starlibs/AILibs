package jaicore.search.algorithms.standard.bestfirst.npuzzle.parentDiscarding;

import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;

import jaicore.basic.PerformanceLogger;
import jaicore.basic.PerformanceLogger.PerformanceMeasure;
import jaicore.graphvisualizer.SimpleGraphVisualizationWindow;
import jaicore.search.algorithms.standard.astar.AStar;
import jaicore.search.algorithms.standard.core.ParentDiscarding;
import jaicore.search.structure.core.Node;
import jaicore.search.structure.core.NodeExpansionDescription;

public class ParentDiscardingClosedNodeTest {
	
	
	@Test
	public void test() {
		
		int board[][] = {{1,1,1,1},{1,1,1,1},{0,1,1,1},{1,1,1,1}};
		int board2[][] = {{1,1,1,1},{1,1,1,1},{1,0,1,1},{1,1,1,1}};
		int board3[][] = {{1,1,1,1},{1,1,1,1},{1,1,1,1},{0,1,1,1}};
		int board4[][] = {{1,1,1,1},{1,1,1,1},{1,1,1,1},{1,0,1,1}};
		int board5[][] = {{1,1,1,1},{1,1,1,1},{1,1,1,1},{1,1,0,1}};
		int board6[][]= {{1,1,1,1},{1,1,1,1},{1,1,1,1},{1,1,1,0}};
		
		
		PDPuzzleNode p1 = new PDPuzzleNode(board,0,2,0);
		PDPuzzleNode p2 = new PDPuzzleNode(board2,1,2,0);
		PDPuzzleNode p3 = new PDPuzzleNode(board3, 0,3,0);
		PDPuzzleNode p4 = new PDPuzzleNode(board4, 1,3,0);
		PDPuzzleNode p5 = new PDPuzzleNode (board5,2,3,0);
		PDPuzzleNode p6 = new PDPuzzleNode(board6, 3,3,0);
		
		PDPuzzleGenerator gen = new PDPuzzleGenerator(board,0,2);
		
		AStar<PDPuzzleNode,String> search = new AStar<>(gen,
					(n1,n2)->{
						if(n1.getPoint().equals(p1) && n2.getPoint().equals(p2))
							return 4.0;
						if(n1.getPoint().equals(p1)&& n2.getPoint().equals(p3))
							return 6.0;
						if(n1.getPoint().equals(p2) && n2.getPoint().equals(p4))
							return 4.0;
						if(n1.getPoint().equals(p3) && n2.getPoint().equals(p4))
							return 1.0;
						if(n1.getPoint().equals(p4) && n2.getPoint().equals(p5))
							return 1.0;
						if(n1.getPoint().equals(p5) && n2.getPoint().equals(p6))
							return 100.0;
						else
							return Double.MAX_VALUE;
						
					}, 
					n->{
						if(n.getPoint().equals(p2))
							return 2.0;
						if(n.getPoint().equals(p3))
							return 30.0; 
						if(n.getPoint().equals(p4))
							return 2.0;
						if(n.getPoint().equals(p4))
							return 1.0;
						if(n.getPoint().equals(p5))
							return 1.0;
						if(n.getPoint().equals(p6))
							return 50.0;
						else
							return Double.MAX_VALUE;
						
					}, ParentDiscarding.ALL) ;

		
		SimpleGraphVisualizationWindow<Node<PDPuzzleNode,Double>> win = new SimpleGraphVisualizationWindow<>(search.getEventBus());
		win.getPanel().setTooltipGenerator(n->n.getPoint().toString());

		List<PDPuzzleNode> solutionPath = search.nextSolution();
		
		solutionPath = search.nextSolution();
		assert(solutionPath.contains(p3));

		assertNotNull(solutionPath);

		
			
		PerformanceLogger.printStatsAndClear(PerformanceMeasure.TIME);
//		while(true);
	}

}
