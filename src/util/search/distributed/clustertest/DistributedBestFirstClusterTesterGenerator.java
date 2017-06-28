package util.search.distributed.clustertest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import util.search.core.GraphGenerator;
import util.search.core.NodeExpansionDescription;
import util.search.core.NodeType;
import util.search.graphgenerator.GoalTester;
import util.search.graphgenerator.RootGenerator;
import util.search.graphgenerator.SuccessorGenerator;

public class DistributedBestFirstClusterTesterGenerator implements GraphGenerator<TestNode, String> {

	private int size, target;

	public DistributedBestFirstClusterTesterGenerator(int size, int target) {
		super();
		this.size = size;
		this.target = target;
		System.out.println("Trying to find " + target + " within a space of " + size + " items.");
	}

	public SuccessorGenerator<TestNode, String> getSuccessorGenerator() {
		return n -> {
			List<NodeExpansionDescription<TestNode, String>> l = new ArrayList<>();
			TestNode parent = n.getPoint();
			if (parent.min < parent.max) {
				int split = (int) Math.floor((parent.min + parent.max) / 2f);
				l.add(new NodeExpansionDescription<>(parent, new TestNode(parent.min, split), "edge label", NodeType.OR));
				l.add(new NodeExpansionDescription<>(parent, new TestNode(split + 1, parent.max), "edge label", NodeType.OR));
			}
			return l;
		};
	}

	public GoalTester<TestNode> getGoalTester() {
		return n -> (n.getPoint().min == n.getPoint().max && n.getPoint().min == target);
	}

	public RootGenerator<TestNode> getRootGenerator() {
		return () -> Arrays.asList(new TestNode[]{new TestNode(0, size)});
	}
}
