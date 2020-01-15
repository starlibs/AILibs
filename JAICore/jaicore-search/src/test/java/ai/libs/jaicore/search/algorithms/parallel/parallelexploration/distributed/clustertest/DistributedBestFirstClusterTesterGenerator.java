package ai.libs.jaicore.search.algorithms.parallel.parallelexploration.distributed.clustertest;

import java.util.ArrayList;
import java.util.List;

import org.api4.java.datastructure.graph.implicit.IGraphGenerator;
import org.api4.java.datastructure.graph.implicit.INewNodeDescription;
import org.api4.java.datastructure.graph.implicit.IRootGenerator;
import org.api4.java.datastructure.graph.implicit.ISingleRootGenerator;
import org.api4.java.datastructure.graph.implicit.ISuccessorGenerator;

import ai.libs.jaicore.search.model.NodeExpansionDescription;

public class DistributedBestFirstClusterTesterGenerator implements IGraphGenerator<TestNode, String> {

	private ISingleRootGenerator<TestNode> rootGenerator;

	public DistributedBestFirstClusterTesterGenerator(final int size) {
		super();
		this.rootGenerator =  () -> new TestNode(0, size);
	}

	@Override
	public ISuccessorGenerator<TestNode, String> getSuccessorGenerator() {
		return n -> {
			List<INewNodeDescription<TestNode, String>> l = new ArrayList<>();
			TestNode parent = n;
			if (parent.min < parent.max) {
				int split = (int) Math.floor((parent.min + parent.max) / 2f);
				l.add(new NodeExpansionDescription<>(new TestNode(parent.min, split), "edge label"));
				l.add(new NodeExpansionDescription<>(new TestNode(split + 1, parent.max), "edge label"));
			}
			return l;
		};
	}

	@Override
	public IRootGenerator<TestNode> getRootGenerator() {
		return this.rootGenerator;
	}
}
