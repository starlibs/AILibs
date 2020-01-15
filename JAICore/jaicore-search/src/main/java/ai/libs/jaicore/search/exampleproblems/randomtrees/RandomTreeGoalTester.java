package ai.libs.jaicore.search.exampleproblems.randomtrees;

import java.util.List;

import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.INodeGoalTester;

public class RandomTreeGoalTester implements INodeGoalTester<List<Integer>, Integer> {

	private final int d;

	public RandomTreeGoalTester(final int d) {
		super();
		this.d = d;
	}

	@Override
	public boolean isGoal(final List<Integer> node) {
		return node.size() == this.d;
	}
}
