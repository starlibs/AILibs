package jaicore.search.algorithms.standard.astar;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;


import jaicore.search.structure.core.GraphGenerator;
import jaicore.search.structure.core.NodeExpansionDescription;
import jaicore.search.structure.core.NodeType;
import jaicore.search.structure.graphgenerator.NodeGoalTester;
import jaicore.search.structure.graphgenerator.SingleRootGenerator;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;

public class AStarTestMain {
	static class TestNode {
		static int size = 0;
		int value = size++;
		
		public String toString() { return "" + value; }
	}

	public static void main(String[] args) {
	//	test();

	}
	


//	@Test
	public static void test() {

		GraphGenerator<TestNode, String> gen = new GraphGenerator<AStarTestMain.TestNode, String>() {

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
				return l -> l.value == 10000;
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
		AStar<TestNode,String> astar = new AStar<>(gen, (n1, n2) -> n2.getPoint().value - n1.getPoint().value, n -> 0.0);
		
		
		/* find solution */
		List<TestNode> solutionPath = astar.nextSolution();
		assertNotNull(solutionPath);
		System.out.println(solutionPath);
	}

}
