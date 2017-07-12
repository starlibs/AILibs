package jaicore.search.algorithms.parallel.parallelexploration.distributed.clustertest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jaicore.search.algorithms.parallel.parallelexploration.distributed.interfaces.DistributableGraphGenerator;
import jaicore.search.algorithms.parallel.parallelexploration.distributed.interfaces.SerializableRootGenerator;
import jaicore.search.structure.core.NodeExpansionDescription;
import jaicore.search.structure.core.NodeType;
import jaicore.search.structure.graphgenerator.GoalTester;
import jaicore.search.structure.graphgenerator.RootGenerator;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;

public class DistributedBestFirstClusterTesterGenerator implements DistributableGraphGenerator<TestNode, String> {

	private SerializableRootGenerator<TestNode> rootGenerator;
	private int size, target;

	public DistributedBestFirstClusterTesterGenerator(int size, int target) {
		super();
		this.size = size;
		this.target = target;
		rootGenerator =  () -> Arrays.asList(new TestNode[]{new TestNode(0, size)});
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
		return rootGenerator;
	}

	@Override
	public void setRootGenerator(SerializableRootGenerator<TestNode> generator) {
		rootGenerator = generator;
	}
}
