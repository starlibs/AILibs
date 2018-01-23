package jaicore.search.algorithms.standard.mcts;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Random;

import org.junit.Test;

import jaicore.search.graphgenerators.nqueens.NQueenGenerator;
import jaicore.search.graphgenerators.nqueens.QueenNode;

public class NQueenTester {
	
	
	@Test
	public void test(){
		final int x = 20;
				
		NQueenGenerator gen = new NQueenGenerator(x);
		
		IPolicy<QueenNode, String, Double> randomPolicy = new UniformRandomPolicy<>(new Random(1));
		IPolicy<QueenNode, String, Double> ucb = new UCBPolicy<>();
		
		
		MCTS<QueenNode, String, Double> search = new MCTS<>(gen, ucb, randomPolicy, n-> (double)n.getPoint().getNumberOfQueens());
		
//		new SimpleGraphVisualizationWindow<>(search.getEventBus()).getPanel().setTooltipGenerator(n->n.getPoint().toString());
//		SimpleGraphVisualizationWindow<Node<QueenNode,Double>> win = new SimpleGraphVisualizationWindow<>(search.getEventBus());
//		win.getPanel().setTooltipGenerator(n->n.getPoint().toString());
		
		/* find solution */
		List<QueenNode> solutionPath = search.nextSolution();
		assertNotNull(solutionPath);
//		System.out.println("Generated " + search.get+ " nodes.");
//		PerformanceLogger.printStatsAndClear(PerformanceMeasure.TIME);
	}
}


