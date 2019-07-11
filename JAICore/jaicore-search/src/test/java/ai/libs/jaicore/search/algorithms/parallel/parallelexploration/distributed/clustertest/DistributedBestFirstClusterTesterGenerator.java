package ai.libs.jaicore.search.algorithms.parallel.parallelexploration.distributed.clustertest;

import java.util.ArrayList;
import java.util.List;

import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.NodeExpansionDescription;
import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.NodeGoalTester;
import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.NodeType;
import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.RootGenerator;
import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.SerializableGraphGenerator;
import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.SerializableRootGenerator;
import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.SuccessorGenerator;

@SuppressWarnings("serial")
public class DistributedBestFirstClusterTesterGenerator implements SerializableGraphGenerator<TestNode, String> {

	private SerializableRootGenerator<TestNode> rootGenerator;
	private int target;

	public DistributedBestFirstClusterTesterGenerator(final int size, final int target) {
		super();
		this.target = target;
		this.rootGenerator =  () -> new TestNode(0, size);
		System.out.println("Trying to find " + target + " within a space of " + size + " items.");
	}

	@Override
	public SuccessorGenerator<TestNode, String> getSuccessorGenerator() {
		return n -> {
			List<NodeExpansionDescription<TestNode, String>> l = new ArrayList<>();
			TestNode parent = n;
			if (parent.min < parent.max) {
				int split = (int) Math.floor((parent.min + parent.max) / 2f);
				l.add(new NodeExpansionDescription<>(new TestNode(parent.min, split), "edge label", NodeType.OR));
				l.add(new NodeExpansionDescription<>(new TestNode(split + 1, parent.max), "edge label", NodeType.OR));
			}
			return l;
		};
	}

	@Override
	public NodeGoalTester<TestNode, String> getGoalTester() {
		return n -> (n.min == n.max && n.min == this.target);
	}

	@Override
	public RootGenerator<TestNode> getRootGenerator() {
		return this.rootGenerator;
	}
}
