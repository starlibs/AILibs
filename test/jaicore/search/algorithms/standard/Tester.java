package jaicore.search.algorithms.standard;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import jaicore.graphvisualizer.SimpleGraphVisualizationWindow;
import jaicore.search.algorithms.standard.bestfirst.BestFirst;
import jaicore.search.structure.core.GraphGenerator;
import jaicore.search.structure.core.NodeExpansionDescription;
import jaicore.search.structure.core.NodeType;
import jaicore.search.structure.graphgenerator.GoalTester;
import jaicore.search.structure.graphgenerator.RootGenerator;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;

public class Tester {

	static class TestNode{
		static int size = 0;
		int value = size++;
		
		public String toString() {
			return ""+value;
		}
	}
	
	@Test
	public void test() {
		TestNode t = new TestNode();
		GraphGenerator<TestNode, String> gen = new GraphGenerator<Tester.TestNode,String>() {

			@Override
			public RootGenerator<TestNode> getRootGenerator() {
				return()-> Arrays.asList(new TestNode[] {new TestNode()});
			}

			@Override
			public SuccessorGenerator<TestNode, String> getSuccessorGenerator() {
				return n-> {
					List<NodeExpansionDescription<TestNode, String>> l = new ArrayList<>();
					l.add(new NodeExpansionDescription<>(n.getPoint(), new TestNode(), "edge label", NodeType.OR));
					if (n.getPoint().value == 2 || n.getPoint().value == 5) {
						l.add(new NodeExpansionDescription<>(n.getPoint(), t, "edege label", NodeType.OR));
					}
					return l;
				};
			}

			@Override
			public GoalTester<TestNode> getGoalTester() {
				return n-> n.getPoint().value == 10;
			}
			
		};
		
		BestFirst<TestNode,String> test = new BestFirst<>(gen, n-> n.getPoint().value);
		new SimpleGraphVisualizationWindow<>(test.getEventBus()).getPanel().setTooltipGenerator(n-> String.valueOf(n.getInternalLabel()));
		
		List<TestNode> list = test.nextSolution();
		assertNotNull(list);
		System.out.println(list);
		
		while(true);
		
	}

}
