package jaicore.search.algorithms.parallel.parallelexploration.distributed.clustertest;

import java.util.ArrayList;
import java.util.List;

import jaicore.search.algorithms.parallel.parallelexploration.distributed.interfaces.SerializableGraphGenerator;
import jaicore.search.algorithms.parallel.parallelexploration.distributed.interfaces.SerializableRootGenerator;
import jaicore.search.model.travesaltree.NodeExpansionDescription;
import jaicore.search.model.travesaltree.NodeType;
import jaicore.search.structure.graphgenerator.NodeGoalTester;
import jaicore.search.structure.graphgenerator.RootGenerator;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;

@SuppressWarnings("serial")
public class DistributedBestFirstClusterTesterGenerator implements SerializableGraphGenerator<TestNode, String> {

	private SerializableRootGenerator<TestNode> rootGenerator;
	private int target;

	public DistributedBestFirstClusterTesterGenerator(int size, int target) {
		super();
		this.target = target;
		rootGenerator =  () -> new TestNode(0, size);
		System.out.println("Trying to find " + target + " within a space of " + size + " items.");
	}

	public SuccessorGenerator<TestNode, String> getSuccessorGenerator() {
		return n -> {
			List<NodeExpansionDescription<TestNode, String>> l = new ArrayList<>();
			TestNode parent = n;
			if (parent.min < parent.max) {
				int split = (int) Math.floor((parent.min + parent.max) / 2f);
				l.add(new NodeExpansionDescription<>(parent, new TestNode(parent.min, split), "edge label", NodeType.OR));
				l.add(new NodeExpansionDescription<>(parent, new TestNode(split + 1, parent.max), "edge label", NodeType.OR));
			}
			return l;
		};
	}

	public NodeGoalTester<TestNode> getGoalTester() {
		return n -> (n.min == n.max && n.min == target);
	}

	public RootGenerator<TestNode> getRootGenerator() {
		return rootGenerator;
	}
	
	@Override
	public boolean isSelfContained() {
		return false;
	}

	@Override
	public void setNodeNumbering(boolean nodenumbering) {
		// TODO Auto-generated method stub
		
	}
}
