package jaicore.search.algorithms.parallel.parallelexploration.distributed;

import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import jaicore.graphvisualizer.SimpleGraphVisualizationWindow;
import jaicore.search.algorithms.standard.core.NodeEvaluator;
import jaicore.search.structure.core.GraphGenerator;
import jaicore.search.structure.core.NodeExpansionDescription;
import jaicore.search.structure.core.NodeType;
import jaicore.search.structure.graphgenerator.GoalTester;
import jaicore.search.structure.graphgenerator.RootGenerator;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;

public class DistributedBestFirstTester {

	static class TestNode implements Serializable {
		private static final long serialVersionUID = 793618120417152627L;
		final int min, max;

		public TestNode(int min, int max) {
			super();
			this.min = min;
			this.max = max;
		}

		public String toString() {
			return min + "/" + max;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + max;
			result = prime * result + min;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TestNode other = (TestNode) obj;
			if (max != other.max)
				return false;
			if (min != other.min)
				return false;
			return true;
		}
	}

	@Test
	public void test() {
		
		Random rand = new Random(0);
		int size = (int)Math.pow(2, 25);
		int target = (int)Math.round(rand.nextDouble() * size);
		System.out.println("Trying to find " + target + " within a space of " + size + " items.");

		GraphGenerator<TestNode, String> gen = new GraphGenerator<TestNode, String>() {

			@Override
			public RootGenerator<TestNode> getRootGenerator() {
				return () -> Arrays.asList(new TestNode[]{new TestNode(0, size)});
			}

			@Override
			public SuccessorGenerator<TestNode, String> getSuccessorGenerator() {
				return n -> {
					List<NodeExpansionDescription<TestNode, String>> l = new ArrayList<>();
					TestNode parent = n.getPoint();
					if (parent.min < parent.max) {
						int split = (int)Math.floor((parent.min + parent.max) / 2f);
						l.add(new NodeExpansionDescription<>(parent, new TestNode(parent.min, split), "edge label", NodeType.OR));
						l.add(new NodeExpansionDescription<>(parent, new TestNode(split + 1, parent.max), "edge label", NodeType.OR));
					}
					return l;
				};
			}

			@Override
			public GoalTester<TestNode> getGoalTester() {
				return n -> (n.getPoint().min == n.getPoint().max && n.getPoint().min == target);
			}
		};
		
		Path folder = Paths.get("Z:/pc2/distsearch/testrsc/comm");
		DistributedSearchMaintainer<TestNode,Integer> communicationLayer = new CommunicationFolderBasedDistributionProcessor<>(folder);
		
		NodeEvaluator<TestNode,Integer> evaluator = n -> -1 * n.externalPath().size();
		DistributedOrSearchMaster<TestNode,String,Integer> master = new DistributedOrSearchMaster<>(gen, evaluator, communicationLayer, 1);
		
		/* setup coworkers */
		int coworkers = 10;
		for (int i = 1; i <= coworkers; i++) {
			final String name = "cw" + i; 
			new Thread(() -> { new DistributedOrSearchCoworker<>(gen, evaluator, communicationLayer, name, 10000, 1000, 1).cowork(); }).start();
		}
		
//		ORGraphSearch<TestNode, String> master = new ORGraphSearch<>(gen, new BestFirstFactory<>(evaluator));

		/* run master in separate thread */
		new SimpleGraphVisualizationWindow<>(master.getEventBus());

		long start = System.currentTimeMillis();
		List<TestNode> solution = master.nextSolution();
		long end = System.currentTimeMillis();
		org.junit.Assert.assertNotNull(solution);
		System.out.println(solution);
		System.out.println("Found after " + (end-start) / 1000f + " seconds.");
	}

}
