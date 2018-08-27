package jaicore.search.algorithms.standard.bestfirst;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import jaicore.search.gui.dataSupplier.TooltipSupplier;
import org.junit.Test;

import jaicore.basic.PerformanceLogger;
import jaicore.basic.PerformanceLogger.PerformanceMeasure;
import jaicore.search.structure.core.GraphGenerator;
import jaicore.search.structure.core.Node;
import jaicore.search.structure.core.NodeExpansionDescription;
import jaicore.search.structure.core.NodeType;
import jaicore.search.structure.graphgenerator.NodeGoalTester;
import jaicore.search.structure.graphgenerator.SingleRootGenerator;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;
import jaicore.graphvisualizer.gui.VisualizationWindow;

public class BestFirstTester {

	static class TestNode {
		static int size = 0;
		int value = size++;
		
		public String toString() { return "" + value; }
	}

	@Test
	public void test() {
		
		GraphGenerator<TestNode, String> gen = new GraphGenerator<BestFirstTester.TestNode, String>() {

			@Override
			public SingleRootGenerator<TestNode> getRootGenerator() {
				return () -> new TestNode();
			}

			@Override
			public SuccessorGenerator<TestNode, String> getSuccessorGenerator() {
				return n -> {
					List<NodeExpansionDescription<TestNode,String>> l = new ArrayList<>(3);
					for (int i = 0; i < 3; i++) {
						l.add(new NodeExpansionDescription<>(n, new TestNode(), "edge label", NodeType.OR));
					}
					return l;
				};
			}

			@Override
			public NodeGoalTester<TestNode> getGoalTester() {
				return l -> l.value == 1000;
			}
			
			@Override
			public boolean isSelfContained() {
				return false;
			}

			@Override
			public void setNodeNumbering(boolean nodenumbering) {
				// TODO Auto-generated method stub
				
			}
			
		};
		
		BestFirst<TestNode,String> bf = new BestFirst<>(gen, n -> (double)Math.round(Math.random() * 1000));
//		new SimpleGraphVisualizationWindow<Node<TestNode,Double>>(bf).getPanel().setTooltipGenerator(n -> String.valueOf(n.getInternalLabel()));
		
		VisualizationWindow win = new VisualizationWindow<Node<TestNode, Double>>(bf, "BestFirst");
		TooltipSupplier tooltipSupplier = new TooltipSupplier();
		tooltipSupplier.setGenerator(node ->{
			Node<?, ?> n = (Node<?, ?>) node;
			String s = String.valueOf(n.getInternalLabel());
			return s;
		});
		win.addDataSupplier(tooltipSupplier);
		new VisualizationWindow<Node<TestNode,Double>>(bf, "BestFirst2");
		
		
		/* find solution */
		PerformanceLogger.logStart("search");
		List<TestNode> solutionPath = bf.nextSolution();
		PerformanceLogger.logEnd("search");
		assertNotNull(solutionPath);
		System.out.println("Generated " + bf.getCreatedCounter() + " nodes.");
		PerformanceLogger.printStatsAndClear(PerformanceMeasure.TIME);
		while (true);
	}

}
