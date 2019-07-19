package ai.libs.jaicore.search.algorithms.parallel.parallelexploration.distributed.clustertest;

import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.NodeGoalTester;

public class ClusterTestGoalTester implements NodeGoalTester<TestNode, String> {

	private final int target;

	public ClusterTestGoalTester(final int target) {
		super();
		this.target = target;
	}

	@Override
	public boolean isGoal(final TestNode n) {
		return n.min == n.max && n.min == this.target;
	}
}
