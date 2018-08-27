package jaicore.search.gui;

import jaicore.graphvisualizer.gui.VisualizationWindow;
import jaicore.search.gui.dataSupplier.TooltipSupplier;
import jaicore.search.structure.core.GraphGenerator;
import jaicore.search.structure.core.Node;
import jaicore.search.structure.core.NodeExpansionDescription;
import jaicore.search.structure.core.NodeType;
import jaicore.search.structure.graphgenerator.NodeGoalTester;
import jaicore.search.structure.graphgenerator.SingleRootGenerator;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ControlSearchTester {

	static class TestNode {
		static int size = 0;
		int value = size++;
		
		public String toString() { return "" + value; }
	}

	@Test
	public void test() {
		Random random = new Random(0);
		GraphGenerator<TestNode, String> gen = new GraphGenerator<TestNode, String>() {

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
		
		//ControllableBestFirst<TestNode,String> bf = new ControllableBestFirst<>(gen, n -> (double)Math.round(Math.random() * 1000));
		ControllableBestFirst<TestNode,String> bf = new ControllableBestFirst<>(gen, n -> random.nextDouble()*1000);
		VisualizationWindow win = new VisualizationWindow(bf, "test");

		TooltipSupplier tooltipSupplier = new TooltipSupplier();
		tooltipSupplier.setGenerator(node ->{
			Node<?, ?> n = (Node<?, ?>) node;
			String s = String.valueOf(n.getInternalLabel());
			return s;
		});


		win.addDataSupplier(tooltipSupplier);
//		bf.nextSolution();


		while(true);

	}

}
