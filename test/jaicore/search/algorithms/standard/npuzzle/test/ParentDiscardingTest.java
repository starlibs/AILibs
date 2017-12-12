package jaicore.search.algorithms.standard.npuzzle.test;


import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;

import jaicore.basic.PerformanceLogger;
import jaicore.basic.PerformanceLogger.PerformanceMeasure;
import jaicore.graphvisualizer.SimpleGraphVisualizationWindow;
import jaicore.search.algorithms.standard.astar.AStar;
import jaicore.search.algorithms.standard.core.ParentDiscarding;
import jaicore.search.algorithms.standard.npuzzle.NPuzzleGenerator;
import jaicore.search.algorithms.standard.npuzzle.NPuzzleNode;
import jaicore.search.algorithms.standard.npuzzle.NPuzzleRedundantGenerator;
import jaicore.search.algorithms.standard.npuzzle.NPuzzleRedundantNode;
import jaicore.search.structure.core.Node;
import jaicore.search.structure.graphgenerator.NodeGoalTester;

public class ParentDiscardingTest {

	class PDPuzzleNode extends NPuzzleNode{

		public PDPuzzleNode(int dim, int perm) {
			super(dim, perm);
			for(int i = 0; i < dim; i++) {
				for(int j = 0; j < dim; j++) {
					if(this.board[i][j] != 0)
						board[i][j] = 1;
				}
			}
		}

		public PDPuzzleNode(int dim) {
			super(dim);
			for(int i = 0; i < dim; i++) {
				for(int j = 0; j < dim; j++) {
					if(this.board[i][j] != 0)
						board[i][j] = 1;
				}
			}
		}

		public PDPuzzleNode(int[][] board, int emptyX, int emptyY, int numberOfMoves) {
			super(board, emptyX, emptyY, numberOfMoves);
		}

		/* (non-Javadoc)
		 * @see jaicore.search.algorithms.standard.npuzzle.NPuzzleNode#toString()
		 */
		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return super.toString();
		}

		/* (non-Javadoc)
		 * @see jaicore.search.algorithms.standard.npuzzle.NPuzzleNode#getDistance()
		 */
		@Override
		public double getDistance() {
			return Math.abs(board.length-this.getEmptyX()+board.length-this.getEmptyY());
		}
		
	}
	
	class PDPuzzleGenerator extends NPuzzleGenerator{

		public PDPuzzleGenerator(int[][] board, int emptyX, int emptyY) {
			super(board, emptyX, emptyY);
		}

		/* (non-Javadoc)
		 * @see jaicore.search.algorithms.standard.npuzzle.NPuzzleGenerator#getGoalTester()
		 */
		@Override
		public NodeGoalTester<NPuzzleNode> getGoalTester() {
			return n->{
				int[][] board = n.getBoard();
				if(board[board.length-1][board.length-1] == 0)
					return true;
				else
					return false;
			};
		}
		
		
		
	}
	
	@Test
	public void test() {

		
		int board[][] = {{0,1},{2,3}};
		NPuzzleGenerator gen = new PDPuzzleGenerator(board,0,0);
		
		
		AStar<NPuzzleNode,String> search = new AStar<>(gen,
				(n1,n2)->{
					double g = 0.0;
					if(n2.getPoint().getBoard()[0][0]== 2)
						return 3.0;
					if(n2.getPoint().getBoard()[1][1] == 0)
						return 1.0;
					else
						return 4.0;
				}, 
				n->n.getPoint().getDistance(), ParentDiscarding.OPEN) ;
		
		SimpleGraphVisualizationWindow<Node<NPuzzleNode,Double>> win = new SimpleGraphVisualizationWindow<>(search.getEventBus());
		win.getPanel().setTooltipGenerator(n->n.getPoint().toString());
		
		/*search for solution*/
		PerformanceLogger.logStart("search");
		
		List<NPuzzleNode> solutionPath = search.nextSolution();
		List<NPuzzleNode> solutionPath2 = search.nextSolution();
		
		PerformanceLogger.logEnd("search");
		assertNotNull(solutionPath);
		assert solutionPath.size() <=31;
		System.out.println(solutionPath.size());
		System.out.println("Generated " + search.getCreatedCounter() + " nodes.");
		PerformanceLogger.printStatsAndClear(PerformanceMeasure.TIME);
		while(true);
	}

}
