package jaicore.search.algorithms.standard.bestfirst.npuzzle.parentDiscarding;
//package jaicore.search.algorithms.standard.bestfirst.npuzzle.parentDiscarding;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertNotNull;
//
//import java.util.List;
//
//import org.junit.Test;
//
//import jaicore.basic.PerformanceLogger;
//import jaicore.basic.PerformanceLogger.PerformanceMeasure;
//import jaicore.graphvisualizer.SimpleGraphVisualizationWindow;
//import jaicore.search.algorithms.EvaluatedSearchAlgorithmSolution;
//import jaicore.search.algorithms.standard.astar.AStar;
//import jaicore.search.algorithms.standard.bestfirst.BestFirst.ParentDiscarding;
//import jaicore.search.algorithms.standard.bestfirst.model.Node;
//import jaicore.search.testproblems.npuzzle.parentDiscarding.PDPuzzleGenerator;
//import jaicore.search.testproblems.npuzzle.parentDiscarding.PDPuzzleNode;
//
//public class ParentDiscardingTest {
//
//	@Test
//	public void test() throws InterruptedException {
//
//		int board[][] = { { 1, 1, 1 }, { 0, 1, 1 }, { 1, 1, 1 } };
//		int board2[][] = { { 0, 1 }, { 1, 1 } };
//
//		PDPuzzleGenerator gen = new PDPuzzleGenerator(board, 0, 1);
//		PDPuzzleGenerator gen2 = new PDPuzzleGenerator(board2, 0, 0);
//
//		AStar<PDPuzzleNode, String> search = new AStar<>(gen, (n1, n2) -> {
//			if (n2.getPoint().getBoard()[0][0] == 0 || n2.getPoint().getBoard()[0][1] == 0 || n2.getPoint().getBoard()[0][2] == 0 || n2.getPoint().getBoard()[1][2] == 0)
//				return Double.MAX_VALUE;
//			if (n1.getPoint().getBoard()[0][0] == 0 || n1.getPoint().getBoard()[0][1] == 0 || n1.getPoint().getBoard()[0][2] == 0 || n1.getPoint().getBoard()[1][2] == 0)
//				return Double.MAX_VALUE;
//			if (n2.getPoint().getBoard()[1][1] == 0 || n1.getPoint().getBoard()[1][1] == 0)
//				return 3.0;
//			if (n2.getPoint().getBoard()[2][0] == 0)
//				return 4.0;
//			if (n2.getPoint().getBoard()[2][0] == 0 && n1.getPoint().getBoard()[1][0] == 0)
//				return Double.MAX_VALUE;
//			if (n2.getPoint().getBoard()[2][2] == 0)
//				return 10;
//			else
//				return 1.0;
//		}, n -> {
//			return 0.0;
//		}, ParentDiscarding.OPEN);
//
//		AStar<PDPuzzleNode, String> search2 = new AStar<>(gen2, (n1, n2) -> {
//			double g = 0.0;
//			if (n2.getPoint().getBoard()[0][1] == 0)
//				return 3.0;
//			if (n1.getPoint().getBoard()[0][1] == 0)
//				return 3.0;
//			if (n2.getPoint().getBoard()[1][1] == 0)
//				return 1.0;
//			else
//				return 4.0;
//		}, n -> {
//			return 0.0;
//		}, ParentDiscarding.ALL);
//
//		SimpleGraphVisualizationWindow<Node<PDPuzzleNode, Double>> win = new SimpleGraphVisualizationWindow<>(search);
//		win.getPanel().setTooltipGenerator(n -> n.getPoint().toString());
//		win.setTitle("Search");
//
//		SimpleGraphVisualizationWindow<Node<PDPuzzleNode, Double>> win2 = new SimpleGraphVisualizationWindow<>(search2);
//		win2.getPanel().setTooltipGenerator(n -> n.getPoint().toString());
//		win2.setTitle("Search2");
//
//		/*search for solution*/
//		PerformanceLogger.logStart("search");
//
//		EvaluatedSearchAlgorithmSolution<PDPuzzleNode, String, Double> solution1 = search.nextSolution();
//		EvaluatedSearchAlgorithmSolution<PDPuzzleNode, String, Double> solution2 = search2.nextSolution();
//		solution2 = search2.nextSolution();
//
//		PerformanceLogger.logEnd("search");
//		assertNotNull(solution1.getNodes());
//		assertNotNull(solution2.getNodes());
//
//		assertEquals(3.0, solution2.getNodes().size(), 0.0);
//
//		assert solution1.getNodes().size() <= 31;
//		System.out.println(solution1.getNodes().size());
//		System.out.println("Generated " + search2.getCreatedCounter() + " nodes.");
//		PerformanceLogger.printStatsAndClear(PerformanceMeasure.TIME);
//		while (true)
//			;
//	}
//
//}
