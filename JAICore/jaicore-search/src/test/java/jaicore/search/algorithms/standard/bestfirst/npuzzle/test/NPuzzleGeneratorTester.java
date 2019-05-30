//package jaicore.search.algorithms.standard.bestfirst.npuzzle.test;
//
//import static org.junit.Assert.assertNotNull;
//
//import org.junit.Test;
//
//import jaicore.basic.PerformanceLogger;
//import jaicore.search.algorithms.standard.astar.AStar;
//import jaicore.search.model.other.SearchAlgorithmSolution;
//import jaicore.search.testproblems.npuzzle.standard.NPuzzleNode;
//import jaicore.search.testproblems.npuzzle.standard.NPuzzleProblem;
//
//public class NPuzzleGeneratorTester {
//
//	@Test
//	public void test() throws InterruptedException {
//		// NPuzzleGenerator gen = new NPuzzleGenerator(4,10);
//		// int board[][] = {{1,5,2},{7,4,3},{0,8,6}};
//		// NPuzzleGenerator gen = new NPuzzleGenerator(board, 0,2);
//		// int board[][] = {{8,6,7},{2,5,4},{3,0,1}};
//		// NPuzzleGenerator gen = new NPuzzleGenerator(board,1,2);
//		// int board[][] = {{0,1,3},{4,2,5},{7,8,6}};
//		// NPuzzleGenerator gen = new NPuzzleGenerator(board,0,0);
//
//
//		AStar<NPuzzleNode, String> search = new AStar<>(new NPuzzleProblem(3, 50), (n1, n2) -> 1.0, n -> n.getPoint().getDistance());
//
////		SimpleGraphVisualizationWindow<Node<NPuzzleNode, Double>> win = new SimpleGraphVisualizationWindow<>(search);
////		win.getPanel().setTooltipGenerator(n -> n.getPoint().toString());
//
//		/*search for solution*/
//		PerformanceLogger.logStart("search");
//
//		SearchAlgorithmSolution<NPuzzleNode, String, Double> solution = search.nextSolution();
//
//		assertNotNull(solution);
//		System.out.println("Generated " + search.getCreatedCounter() + " nodes.");
//	}
//
//}
