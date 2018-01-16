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
		final int x = 8;
				
		NQueenGenerator gen = new NQueenGenerator(x);
		
		Random r = new Random();
		
		IPolicy<QueenNode, String, Double> randomPolicy = new IPolicy<QueenNode, String, Double>() {
			
			private Random r = new Random(0);
			
			@Override
			public String getAction(QueenNode node, List<String> actions) {
				if (actions.isEmpty())
					throw new IllegalArgumentException("Cannot determine action if no actions are given!");
				if (actions.size() == 1)
					return actions.get(0);
				return actions.get(r.nextInt(actions.size() - 1));
			}
		};
		MCTS<QueenNode, String, Double> search = new MCTS<>(gen, randomPolicy, randomPolicy, n-> x - (double)n.getPoint().getNumberOfQueens());
		
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


