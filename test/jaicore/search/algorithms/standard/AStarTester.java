package jaicore.search.algorithms.standard;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import jaicore.search.algorithms.standard.astar.AStar;
import jaicore.search.structure.core.GraphGenerator;
import jaicore.search.structure.core.NodeExpansionDescription;
import jaicore.search.structure.core.NodeType;
import jaicore.search.structure.graphgenerator.NodeGoalTester;
import jaicore.search.structure.graphgenerator.PathGoalTester;
import jaicore.search.structure.graphgenerator.RootGenerator;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;

public class AStarTester {

	static class TestNode {
		static int size = 0;
		int value = size++;
		
		public String toString() { return "" + value; }
	}

	@Test
	public void test() {

		GraphGenerator<TestNode, String> gen = new GraphGenerator<AStarTester.TestNode, String>() {

			@Override
			public RootGenerator<TestNode> getRootGenerator() {
				return () -> Arrays.asList(new TestNode[]{new TestNode()});
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
			public PathGoalTester<TestNode> getPathGoalTester() {
				return l -> l.get(l.size()-1).value == 10000;
			}
			
			@Override
			public NodeGoalTester<TestNode> getNodeGoalTester(){
				return null;
			}
		};
		AStar<TestNode,String> astar = new AStar<>(gen, (n1, n2) -> n2.getPoint().value - n1.getPoint().value, n -> 0);
		
		
		/* find solution */
		List<TestNode> solutionPath = astar.nextSolution();
		assertNotNull(solutionPath);
		System.out.println(solutionPath);
	}

}
